package com.example.hibernatedemo.manytomany.set

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = BookSet.TABLE_NAME)
class BookSet(var title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "book_sets"
    }
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
}