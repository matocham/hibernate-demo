package com.example.hibernatedemo.manytomany.jointable

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name=BookAuthor.TABLE_NAME)
class BookAuthor(
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("bookId") var book: Book?,
    @ManyToOne(fetch = FetchType.LAZY) @MapsId("authorId") var author: Author?
) {
    companion object {
        const val TABLE_NAME = "books_authors"
    }

    @EmbeddedId
    var bookAuthorId: BookAuthorId? = BookAuthorId(book?.id, author?.id)

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
        return Objects.hash(book, author)
    }
}

@Embeddable
data class BookAuthorId(
    @Column(name="book_id")
    val bookId: Long? = null,
    @Column(name="author_id")
    val authorId: Long? = null
): Serializable