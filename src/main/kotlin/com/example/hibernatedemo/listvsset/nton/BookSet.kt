package com.example.hibernatedemo.listvsset.nton

import jakarta.persistence.*

@Entity
@Table(name = "book_sets")
class BookSet() {

    constructor(title: String) : this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    lateinit var title: String

    @ManyToMany(fetch = FetchType.LAZY,
         cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "books")
    val authors: MutableSet<AuthorSet> = mutableSetOf()

    fun addAuthor(author: AuthorSet) {
        authors.add(author)
        author.books.add(this)
    }

    fun removeAuthor(author: AuthorSet) {
        authors.remove(author)
        author.books.remove(this)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is BookSet || id == null) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}