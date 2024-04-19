package com.example.hibernatedemo.utils

class HibernateStatistics {

    private val selectCountMap: MutableMap<String, Int> = mutableMapOf()
    private val updateCountMap: MutableMap<String, Int> = mutableMapOf()
    private val deleteCountMap: MutableMap<String, Int> = mutableMapOf()
    private val insertCountMap: MutableMap<String, Int> = mutableMapOf()

    fun clearStatistics() {
        selectCountMap.clear()
        updateCountMap.clear()
        deleteCountMap.clear()
        insertCountMap.clear()
    }

    fun insert(name: String, count: Int = 1) {
        insertCountMap[name] = insertCountMap.getOrDefault(name, 0) + count
    }

    fun update(name: String, count: Int = 1) {
        updateCountMap[name] = updateCountMap.getOrDefault(name, 0) + count
    }

    fun select(name: String, count: Int = 1) {
        selectCountMap[name] = selectCountMap.getOrDefault(name, 0) + count
    }

    fun delete(name: String, count: Int = 1) {
        deleteCountMap[name] = deleteCountMap.getOrDefault(name, 0) + count
    }

    fun insertCount(name: String?) = getForTableOrAll(name, insertCountMap)
    fun updateCount(name: String?) = getForTableOrAll(name, updateCountMap)
    fun selectCount(name: String?) = getForTableOrAll(name, selectCountMap)
    fun deleteCount(name: String?) = getForTableOrAll(name, deleteCountMap)

    private fun getForTableOrAll(name: String?, map: Map<String, Int>) =
        if(name == null) {
            map.values.sum()
        } else {
            map.getOrDefault(name, 0)
        }
}