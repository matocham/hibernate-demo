package com.example.hibernatedemo.utils

import org.amshove.kluent.internal.ComparisonFailedException
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class DbAssertions: BeforeEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        HibernateStatisticsCollector.statistics.clearStatistics()
    }

    fun checkQueryCount(times: Int, tableName: String? = null) = withSummary(tableName) {
        HibernateStatisticsCollector.statistics.selectCount(tableName) shouldBeEqualTo times
    }

    fun checkInsertCount(times: Int, tableName: String? = null) = withSummary(tableName) {
        HibernateStatisticsCollector.statistics.insertCount(tableName) shouldBeEqualTo times
    }

    fun checkUpdateCount(times: Int, tableName: String? = null) = withSummary(tableName) {
        HibernateStatisticsCollector.statistics.updateCount(tableName) shouldBeEqualTo times
    }

    fun checkDeleteCount(times: Int, tableName: String? = null) = withSummary(tableName) {
        HibernateStatisticsCollector.statistics.deleteCount(tableName) shouldBeEqualTo times
    }

    fun withSummary(tableName: String?, block: () -> Unit) {
        try {
            block()
        } catch (e: ComparisonFailedException) {
            val message = """
                ${e.message}
                Operations summary ${if(tableName == null) "" else "for table $tableName"}:
                    - inserts: ${HibernateStatisticsCollector.statistics.insertCount(tableName)},
                    - queries: ${HibernateStatisticsCollector.statistics.selectCount(tableName)},
                    - updates: ${HibernateStatisticsCollector.statistics.updateCount(tableName)},
                    - deletes: ${HibernateStatisticsCollector.statistics.deleteCount(tableName)}
            """.trimIndent()
            throw DbAssertionException(message)
        }
    }
}

class DbAssertionException(override val message: String): RuntimeException() {

}
