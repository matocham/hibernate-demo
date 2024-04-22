package com.example.hibernatedemo.manytomany.set

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name=AuthorSet.TABLE_NAME)
class AuthorSet(var name: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "author_sets"
        const val JOIN_TABLE_NAME = "author_book_set"
    }
    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(name = JOIN_TABLE_NAME,
        joinColumns = [JoinColumn(name = "author_id")],
        inverseJoinColumns = [JoinColumn(name = "book_id")]
    )
    val books: MutableSet<BookSet> = mutableSetOf()

    fun addBook(book: BookSet) {
        books.add(book)
        book.authors.add(this)
    }

    fun removeBook(book: BookSet) {
        books.remove(book)
        book.authors.remove(this)
    }
}