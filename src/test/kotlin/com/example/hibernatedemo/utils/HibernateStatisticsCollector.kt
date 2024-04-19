package com.example.hibernatedemo.utils

import mu.KotlinLogging
import org.hibernate.resource.jdbc.spi.StatementInspector
import java.util.regex.Matcher
import java.util.regex.Pattern


private val logger = KotlinLogging.logger {}

class HibernateStatisticsCollector: StatementInspector {
    companion object {
        private val tableNamePattern = "[a-zA-Z0-9_]+"
        private val insertPattern = Pattern.compile("^insert into ($tableNamePattern).*$")
        private val selectPattern = Pattern.compile("^select .*? from ($tableNamePattern).*$")
        private val updatePattern = Pattern.compile("^update ($tableNamePattern).*$")
        private val deletePattern = Pattern.compile("^delete from ($tableNamePattern).*$")
        @JvmStatic
        val statistics: HibernateStatistics = HibernateStatistics()
    }

    override fun inspect(sql: String): String {
        logger.info { sql }

        selectPattern.doOnMatch(sql) {
            statistics.select(it)
        }
        insertPattern.doOnMatch(sql) {
            statistics.insert(it)
        }
        updatePattern.doOnMatch(sql) {
            statistics.update(it)
        }
        deletePattern.doOnMatch(sql) {
            statistics.delete(it)
        }
        return sql
    }

    private fun Pattern.doOnMatch(sql: String, block: (name: String) -> Unit) {
        val matcher = matcher(sql)
        if(matcher.matches()) {
            block(matcher.group(1))
        }
    }
}