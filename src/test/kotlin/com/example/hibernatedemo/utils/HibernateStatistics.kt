package com.example.hibernatedemo.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Increments should be thread-safe. Reading values is not thread safe,
 * but it is fine if assertions are performed only in the main test thread
 * after all spawned threads have joined
 */
class HibernateStatistics {
    data class DmlSpec(
        val mainTableName: String,
        val joinedTables: List<String> = listOf()
    )

    private val queryCountMap: ConcurrentHashMap<DmlSpec, AtomicLong> = ConcurrentHashMap()
    private val updateCountMap: ConcurrentHashMap<DmlSpec, AtomicLong> = ConcurrentHashMap()
    private val deleteCountMap: ConcurrentHashMap<DmlSpec, AtomicLong> = ConcurrentHashMap()
    private val insertCountMap: ConcurrentHashMap<DmlSpec, AtomicLong> = ConcurrentHashMap()
    private val seqCountMap: ConcurrentHashMap<DmlSpec, AtomicLong> = ConcurrentHashMap()

    fun clearStatistics() {
        queryCountMap.clear()
        updateCountMap.clear()
        deleteCountMap.clear()
        insertCountMap.clear()
        seqCountMap.clear()
    }

    fun insert(spec: DmlSpec) {
        insertCountMap.increment(spec)
    }

    fun update(spec: DmlSpec) {
        updateCountMap.increment(spec)
    }

    fun query(spec: DmlSpec) {
        queryCountMap.increment(spec)
    }

    fun delete(spec: DmlSpec) {
        deleteCountMap.increment(spec)
    }

    fun nextSeqValue(spec: DmlSpec) {
        seqCountMap.increment(spec)
    }

    fun insertCount(name: String?, clear: Boolean = true, strict: Boolean = true): Long {
        val count = insertCountMap.sumForTable(name, strict = strict)
        if (clear) insertCountMap.clearCounter(name)
        return count
    }

    fun updateCount(name: String?, clear: Boolean = true, strict: Boolean = true): Long {
        val count = updateCountMap.sumForTable(name, strict = strict)
        if (clear) updateCountMap.clearCounter(name)
        return count
    }

    fun queryCount(name: String?, clear: Boolean = true, joins: List<String> = listOf(), strict: Boolean = true): Long {
        val count = queryCountMap.sumForTable(name, joins, strict)
        if (clear) queryCountMap.clearCounter(name, joins)
        return count
    }

    fun deleteCount(name: String?, clear: Boolean = true, strict: Boolean = true): Long {
        val count = deleteCountMap.sumForTable(name, strict = strict)
        if (clear) deleteCountMap.clearCounter(name)
        return count
    }

    fun seqNextValCount(name: String?, clear: Boolean = true, strict: Boolean = true): Long {
        val count = seqCountMap.sumForTable(name, strict = strict)
        if (clear) seqCountMap.clearCounter(name)
        return count
    }

    fun summary(tableName: String?): String {
        return summaryForTable(tableName) + "\n" + summaryForSeq(tableName)
    }

    fun summaryForTable(tableName: String?): String {
        val allSpecs = mutableSetOf<DmlSpec>()
        if (tableName != null) {
            allSpecs += insertCountMap.specsForTable(tableName)
            allSpecs += updateCountMap.specsForTable(tableName)
            allSpecs += deleteCountMap.specsForTable(tableName)
            allSpecs += queryCountMap.specsForTable(tableName)
            if (allSpecs.isEmpty()) allSpecs.add(DmlSpec(tableName))
        } else {
            allSpecs += insertCountMap.keys
            allSpecs += updateCountMap.keys
            allSpecs += deleteCountMap.keys
            allSpecs += queryCountMap.keys
        }
        if (allSpecs.isEmpty()) {
            return "No tables operations recorded"
        }
        return summarizeTableOperations(allSpecs)
    }

    private fun summarizeTableOperations(allSpecs: Set<DmlSpec>): String {
        var summary = ""
        val byTableName = allSpecs.groupBy { it.mainTableName }

        for ((tn, specs) in byTableName) {
            val queriesBreakdown = queriesWithJoinsSummary(specs)
            val queriesBreakdownWithMargin = queriesBreakdown.replace("\n", "\n|${" ".repeat(19)}")
            summary += """
                    |Operations summary for table ${tn}:
                    |    - inserts: ${insertCount(tn, clear = false, strict = false)},
                    |    - queries: ${queryCount(tn, clear = false, strict = false)},
                    |        including: $queriesBreakdownWithMargin,
                    |    - updates: ${updateCount(tn, clear = false, strict = false)},
                    |    - deletes: ${deleteCount(tn, clear = false, strict = false)}
                """.trimMargin() + "\n"
        }
        return summary
    }

    private fun queriesWithJoinsSummary(specs: List<DmlSpec>): String {
        val specWithJoins = specs.filter { it.joinedTables.isNotEmpty() }
        return if (specWithJoins.isEmpty()) {
            "no queries with joins"
        } else {
            specWithJoins.joinToString("\n") { spec ->
                val count = queryCount(spec.mainTableName, false, spec.joinedTables)
                "join(s) ${spec.joinedTables.joinToString(", ")}: $count"
            }
        }
    }

    fun summaryForSeq(seqName: String?): String {
        val allSpecs = mutableSetOf<DmlSpec>()
        if (seqName != null) {
            allSpecs += seqCountMap.specsForTable(seqName)
            if (allSpecs.isEmpty()) allSpecs.add(DmlSpec(seqName))
        } else {
            allSpecs += seqCountMap.keys
        }
        if (allSpecs.isEmpty()) {
            return "No sequence calls recorded"
        }

        var summary = ""
        for ((sn) in allSpecs) {
            summary += "Sequence $sn calls : ${seqNextValCount(sn, clear = false, strict = false)}\n"
        }
        return summary
    }
}

private fun Map<HibernateStatistics.DmlSpec, AtomicLong>.sumForTable(
    tableName: String?,
    joins: List<String> = listOf(),
    strict: Boolean = true
): Long {
    if (tableName == null) {
        return values.sumOf { it.get() }
    }
    return if (joins.isEmpty() && !strict) {
        entries.filter { it.key.mainTableName == tableName }.sumOf { it.value.get() }
    } else {
        return entries.filter { it.key.mainTableName == tableName && joins == it.key.joinedTables }
            .sumOf { it.value.get() }
    }
}

private fun MutableMap<HibernateStatistics.DmlSpec, AtomicLong>.clearCounter(
    tableName: String?,
    joins: List<String> = listOf()
) {
    if (tableName == null) {
        clear()
    }
    if (joins.isEmpty()) {
        specsForTable(tableName).forEach { remove(it) }
    } else {
        specsForTable(tableName).filter { joins.containsAll(it.joinedTables) }.forEach { remove(it) }
    }
}

private fun Map<HibernateStatistics.DmlSpec, AtomicLong>.specsForTable(tableName: String?): List<HibernateStatistics.DmlSpec> {
    if (tableName == null) {
        return listOf()
    }
    return keys.filter { it.mainTableName == tableName }
}

private fun ConcurrentHashMap<HibernateStatistics.DmlSpec, AtomicLong>.increment(spec: HibernateStatistics.DmlSpec) {
    getOrPut(spec) { AtomicLong(0) }.incrementAndGet()
}