package com.example.hibernatedemo.nton

import jakarta.persistence.*

@Entity
@Table(name = "books")
class Book() {

    constructor(title: String): this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    lateinit var title: String

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val authors: MutableList<BookAuthor> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is Book || id == null) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}