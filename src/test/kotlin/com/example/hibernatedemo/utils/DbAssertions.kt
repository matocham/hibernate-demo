package com.example.hibernatedemo.utils

import com.example.hibernatedemo.BaseDbTest
import org.amshove.kluent.internal.ComparisonFailedException
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class DbAssertions : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        HibernateStatisticsCollector.statistics.clearStatistics()
    }

    override fun afterEach(context: ExtensionContext?) {
        if (context?.executionException?.isEmpty == true) {
            BaseDbTest.db.checkNoInteractions()
        }
    }

    fun checkQueryCount(times: Long, tableName: String? = null, joins: List<String> = listOf()) = withSummary(tableName, joins, OperationType.QUERY) {
        HibernateStatisticsCollector.statistics.queryCount(tableName, true, joins) shouldBeEqualTo times
    }

    fun checkInsertCount(times: Long, tableName: String? = null) = withSummary(tableName, listOf(), OperationType.INSERT) {
        HibernateStatisticsCollector.statistics.insertCount(tableName) shouldBeEqualTo times
    }

    fun checkUpdateCount(times: Long, tableName: String? = null) = withSummary(tableName, listOf(), OperationType.UPDATE) {
        HibernateStatisticsCollector.statistics.updateCount(tableName) shouldBeEqualTo times
    }

    fun checkDeleteCount(times: Long, tableName: String? = null) = withSummary(tableName, listOf(), OperationType.DELETE) {
        HibernateStatisticsCollector.statistics.deleteCount(tableName) shouldBeEqualTo times
    }

    fun checkNextValCount(times: Long, seqName: String? = null) = withSummary(seqName, listOf(), OperationType.NEXT_VAL) {
        HibernateStatisticsCollector.statistics.seqNextValCount(seqName) shouldBeEqualTo times
    }

    private fun checkNoInteractions(tableName: String? = null) = withSummary(tableName, listOf(), OperationType.INTERACTION) {
        HibernateStatisticsCollector.statistics.queryCount(tableName) shouldBeEqualTo 0
        HibernateStatisticsCollector.statistics.updateCount(tableName) shouldBeEqualTo 0
        HibernateStatisticsCollector.statistics.insertCount(tableName) shouldBeEqualTo 0
        HibernateStatisticsCollector.statistics.deleteCount(tableName) shouldBeEqualTo 0
        HibernateStatisticsCollector.statistics.seqNextValCount(tableName) shouldBeEqualTo 0
    }

    private fun withSummary(tableName: String?, joins: List<String>, operationType: OperationType, block: () -> Unit) {
        val summary = summaryForOperation(operationType, tableName)
        try {
            block()
        } catch (e: ComparisonFailedException) {
            val joinsSummary = if(joins.isEmpty()) {
                if(operationType == OperationType.QUERY) " with no joins" else ""
            } else {
                " join(s) ${joins.joinToString(", ")}"
            }
            val tableWithJoins = tableName?.let { " in $it${joinsSummary}" } ?: ""
            val message = "Expected ${e.expected} ${operationType.opName}(s)$tableWithJoins but was: ${e.actual}\n$summary"
            throw DbAssertionException(message)
        }
    }

    private fun summaryForOperation(operationType: OperationType, tableName: String?) =
        when (operationType) {
            OperationType.INTERACTION -> HibernateStatisticsCollector.statistics.summary(tableName)
            OperationType.NEXT_VAL -> HibernateStatisticsCollector.statistics.summaryForSeq(tableName)
            else -> HibernateStatisticsCollector.statistics.summaryForTable(tableName)
        }
}

class DbAssertionException(override val message: String) : RuntimeException()

enum class OperationType(val opName: String) {
    QUERY("query"),
    INSERT("insert"),
    DELETE("delete"),
    UPDATE("update"),
    INTERACTION("interaction"),
    NEXT_VAL("next val")
}