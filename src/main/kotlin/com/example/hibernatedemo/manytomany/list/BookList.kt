package com.example.hibernatedemo.manytomany.list

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = BookList.TABLE_NAME)
class BookList(var title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "book_lists"
    }
    @ManyToMany(fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE], mappedBy = "books")
    val authors: MutableList<AuthorList> = mutableListOf()

    fun addAuthor(author: AuthorList) {
        authors.add(author)
        author.books.add(this)
    }

    fun removeAuthor(author: AuthorList) {
        authors.remove(author)
        author.books.remove(this)
    }
}