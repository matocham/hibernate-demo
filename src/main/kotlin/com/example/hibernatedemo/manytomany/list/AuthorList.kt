package com.example.hibernatedemo.manytomany.list

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name=AuthorList.TABLE_NAME)
class AuthorList(var name: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "author_lists"
        const val JOIN_TABLE_NAME = "author_book_list"
    }
    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(name = JOIN_TABLE_NAME,
        joinColumns = [JoinColumn(name = "author_id")],
        inverseJoinColumns = [JoinColumn(name = "book_id")]
    )
    val books: MutableList<BookList> = mutableListOf()

    fun addBook(book: BookList) {
        books.add(book)
        book.authors.add(this)
    }

    fun removeBook(book: BookList) {
        books.remove(book)
        book.authors.remove(this)
    }
}