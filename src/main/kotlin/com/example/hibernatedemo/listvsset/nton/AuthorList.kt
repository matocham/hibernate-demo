package com.example.hibernatedemo.listvsset.nton

import jakarta.persistence.*

@Entity
@Table(name="author_lists")
class AuthorList() {

    constructor(name: String): this() {
        this.name = name
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    lateinit var name: String

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(name = "author_book_set",
        joinColumns = [JoinColumn(name = "author_id")],
        inverseJoinColumns = [JoinColumn(name = "book_id")]
    )
    val books: MutableSet<BookList> = mutableSetOf()

    fun addBook(book: BookList) {
        books.add(book)
        book.authors.add(this)
    }

    fun removeBook(book: BookList) {
        books.remove(book)
        book.authors.remove(this)
    }

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is AuthorList || id == null) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}