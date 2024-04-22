package com.example.hibernatedemo.manytomany.jointable

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name=Author.TABLE_NAME)
class Author(var name: String): BaseEntity() {

    companion object {
        const val TABLE_NAME = "authors"
    }

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
}