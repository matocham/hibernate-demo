package com.example.hibernatedemo.manytomany.jointable

import com.example.hibernatedemo.base.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = Book.TABLE_NAME)
class Book(var title: String): BaseEntity() {
    companion object {
        const val TABLE_NAME = "books"
    }
    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val authors: MutableList<BookAuthor> = mutableListOf()
}