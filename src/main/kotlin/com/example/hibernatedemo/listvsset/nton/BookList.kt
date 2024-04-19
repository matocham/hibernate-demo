package com.example.hibernatedemo.listvsset.nton

import jakarta.persistence.*

@Entity
@Table(name = "book_lists")
class BookList() {

    constructor(title: String) : this() {
        this.title = title
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    lateinit var title: String

    @ManyToMany(fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "books")
    val authors: MutableSet<AuthorList> = mutableSetOf()

    fun addAuthor(author: AuthorList) {
        authors.add(author)
        author.books.add(this)
    }

    fun removeAuthor(author: AuthorList) {
        authors.remove(author)
        author.books.remove(this)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is BookList || id == null) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}