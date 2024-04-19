package com.example.hibernatedemo.nton

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name="books_authors")
class BookAuthor() {

    constructor(book: Book, author: Author): this() {
        this.book = book
        this.author = author
        this.bookAuthorId = BookAuthorId(book.id, author.id)
    }

    @EmbeddedId
    var bookAuthorId: BookAuthorId? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    @MapsId("bookId")
    var book: Book? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @MapsId("authorId")
    var author: Author? = null

    override fun equals(other: Any?): Boolean {
        if(this === other) {
            return true
        }
        if(other == null || other !is BookAuthor || bookAuthorId == null) {
            return false
        }
        return Objects.equals(book, other.book) && Objects.equals(author, other.author)
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

@Embeddable
data class BookAuthorId(
    @Column(name="book_id")
    val bookId: Long? = null,
    @Column(name="author_id")
    val authorId: Long? = null
): Serializable