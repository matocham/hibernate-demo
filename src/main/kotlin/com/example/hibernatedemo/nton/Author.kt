package com.example.hibernatedemo.nton

import jakarta.persistence.*

@Entity
@Table(name="authors")
class Author() {

    constructor(name: String): this() {
        this.name = name
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    lateinit var name: String

    @OneToMany(mappedBy = "author", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val books: MutableSet<BookAuthor> = mutableSetOf()

    fun addBook(book: Book) {
        val bookAuthor = BookAuthor(book, this)
        books.add(bookAuthor)
        book.authors.add(bookAuthor)
    }

    fun removeBook(book: Book) {
        val iterator: MutableIterator<BookAuthor> = books.iterator()
        while (iterator.hasNext()) {
            val bookAuthor: BookAuthor = iterator.next()
            if (bookAuthor.author == this &&
                bookAuthor.book == book
            ) {
                iterator.remove()
                bookAuthor.book?.authors?.remove(bookAuthor)
                bookAuthor.author = null
                bookAuthor.book = null
            }
        }
    }
    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is Author || id == null) {
            return false
        }
        return id == other.id
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}