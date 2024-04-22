package com.example.hibernatedemo.base

import org.hibernate.Hibernate

abstract class ComparableEntity {

    abstract var id: Long?

    /**
     * Return false when
     * - if objects are equal by reference then return true
     * - other object is null
     * - other object is not the same class
     * - id is null on the current object (this way two transient entities are not equal)
     * - ids don't match
     */
    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false
        }
        return id != null && id == ((other as BaseEntity).id)
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}