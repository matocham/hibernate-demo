package com.example.hibernatedemo.utils

import mu.KotlinLogging
import org.hibernate.resource.jdbc.spi.StatementInspector
import java.util.regex.Matcher
import java.util.regex.Pattern


private val logger = KotlinLogging.logger {}

class HibernateStatisticsCollector : StatementInspector {
    companion object {
        private const val tableNamePattern = "[a-zA-Z0-9_]+"
        private val insertPattern = Pattern.compile("^insert into ($tableNamePattern)")
        private val updatePattern = Pattern.compile("^update ($tableNamePattern)")
        private val deletePattern = Pattern.compile("^delete from ($tableNamePattern)")
        private val selectPattern = Pattern.compile("^select .*? from ($tableNamePattern)")
        private val joinPattern = Pattern.compile("join ($tableNamePattern)")
        private val sequencePattern = Pattern.compile("select nextval\\('($tableNamePattern)'\\)")

        @JvmStatic
        val statistics: HibernateStatistics = HibernateStatistics()
    }

    override fun inspect(sql: String): String {
        // this cooperates with proxy datasource logging
        logger.info { "\n\nNew query" }

        selectPattern.doOnMatch(sql) { tn, joins ->
            statistics.query(HibernateStatistics.DmlSpec(tn, joins))
        }
        insertPattern.doOnMatch(sql) { tn, _ ->
            statistics.insert(HibernateStatistics.DmlSpec(tn))
        }
        updatePattern.doOnMatch(sql) { tn, _ ->
            statistics.update(HibernateStatistics.DmlSpec(tn))
        }
        deletePattern.doOnMatch(sql) { tn, _ ->
            statistics.delete(HibernateStatistics.DmlSpec(tn))
        }
        sequencePattern.doOnMatch(sql) { seqName, _ ->
            statistics.nextSeqValue(HibernateStatistics.DmlSpec(seqName))
        }
        return sql
    }

    private fun Pattern.doOnMatch(sql: String, block: (name: String, joins: List<String>) -> Unit) {
        val matcher = matcher(sql)
        if (matcher.find()) {
            val mainMatch = matcher.group(1)
            val joinsMatcher = joinPattern.matcher(sql)
            block(mainMatch, joinsMatcher.getAllMatches())
        }
    }

    private fun Matcher.getAllMatches(): List<String> {
        val matches = mutableSetOf<String>()
        while (find()) {
            matches.add(group(1))
        }
        return matches.toList()
    }
}