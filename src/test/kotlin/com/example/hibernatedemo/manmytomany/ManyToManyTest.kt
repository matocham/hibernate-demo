package com.example.hibernatedemo.manmytomany

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.builders.Builders.createAuthorsAndBooksWithJoinTable
import com.example.hibernatedemo.builders.Builders.createAuthorsAndBooksWithList
import com.example.hibernatedemo.builders.Builders.createAuthorsAndBooksWithSet
import com.example.hibernatedemo.manytomany.jointable.Author
import com.example.hibernatedemo.manytomany.jointable.Book
import com.example.hibernatedemo.manytomany.jointable.BookAuthor
import com.example.hibernatedemo.manytomany.list.AuthorList
import com.example.hibernatedemo.manytomany.list.BookList
import com.example.hibernatedemo.manytomany.set.AuthorSet
import com.example.hibernatedemo.manytomany.set.BookSet
import org.junit.jupiter.api.Test

class ManyToManyTest: BaseDbTest() {

    @Test
    fun `should issue 1 insert request per author, book and 3 inserts for join tables when creating new many-to-many entities with list`() {
        // when
        transaction {
            val author = AuthorList("author 1")
            val author2 = AuthorList("author 2")
            val book = BookList("book 1")
            val book2 = BookList("book 2")

            author.addBook(book)
            author.addBook(book2)
            author2.addBook(book2)

            // second author gets persisted because of cascade settings on book entity
            entityManager.persist(author)
        }

        // then
        db.checkInsertCount(2, AuthorList.TABLE_NAME)
        db.checkInsertCount(2, BookList.TABLE_NAME)
        db.checkInsertCount(3, AuthorList.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue 1 insert request per author, book and 3 inserts for join tables when creating new many-to-many entities with set`() {
        // when
        transaction {
            val author = AuthorSet("author 1")
            val author2 = AuthorSet("author 2")
            val book = BookSet("book 1")
            val book2 = BookSet("book 2")

            author.addBook(book)
            author.addBook(book2)
            author2.addBook(book2)

            // second author gets persisted because of cascade settings on book entity
            entityManager.persist(author)
        }

        // then
        db.checkInsertCount(2, AuthorSet.TABLE_NAME)
        db.checkInsertCount(2, BookSet.TABLE_NAME)
        db.checkInsertCount(3, AuthorSet.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue 1 insert request per author, book and 3 inserts for join tables when creating new many-to-many entities with join table`() {
        // when
        transaction {
            val author = Author("author 1")
            val author2 = Author("author 2")
            val book = Book("book 1")
            val book2 = Book("book 2")

            // entities have to be persisted first. Otherwise hibernate tries to set
            // IdentifierGeneratorHelper.SHORT_CIRCUIT_INDICATOR as book id value
            // the same happens even when sequence generator is used. May be solved in the future
            // as {@link org.hibernate.id.IdentifierGeneratorHelper.SHORT_CIRCUIT_INDICATOR}
            // is marked for removal
            entityManager.persist(author)
            entityManager.persist(author2)
            entityManager.persist(book)
            entityManager.persist(book2)

            author.addBook(book)
            author.addBook(book2)
            author2.addBook(book2)
        }

        // then
        db.checkInsertCount(2, Author.TABLE_NAME)
        db.checkInsertCount(2, Book.TABLE_NAME)
        db.checkInsertCount(3, BookAuthor.TABLE_NAME)
    }

    @Test
    fun `should issue 1 insert for new entity and 1 insert for join table when adding book to existing author using lists`() {
        // given
        var (author) = createAuthorsAndBooksWithList()

        // when
        transaction {
            // find is more efficient than merge because Cascade.MERGE causes selects waterfall
            author = entityManager.find(AuthorList::class.java, author.id)

            author.addBook(BookList("new book"))
        }

        // then
        // only entity is fetched because of lazy (can be more efficient by using jpql or query graphs)
        db.checkQueryCount(1, AuthorList.TABLE_NAME)
        // additional select to fetch associated books
        db.checkQueryCount(1, AuthorList.JOIN_TABLE_NAME, joins = listOf(BookList.TABLE_NAME))
        // insert association
        db.checkInsertCount(1, BookList.TABLE_NAME)
        // clear all relationships for author
        db.checkDeleteCount(1, AuthorList.JOIN_TABLE_NAME)
        // then insert all values again
        db.checkInsertCount(3, AuthorList.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue 1 insert for new entity and 1 insert for join table when adding book to existing author using sets`() {
        // given
        var (author) = createAuthorsAndBooksWithSet()

        // when
        transaction {
            // find is more efficient than merge because Cascade.MERGE causes selects waterfall
            author = entityManager.find(AuthorSet::class.java, author.id)

            author.addBook(BookSet("new book"))
        }

        // then
        // only entity is fetched because of lazy (can be more efficient by using jpql or query graphs)
        db.checkQueryCount(1, AuthorSet.TABLE_NAME)
        // additional select to fetch associated books
        db.checkQueryCount(1, AuthorSet.JOIN_TABLE_NAME, joins = listOf(BookSet.TABLE_NAME))
        // insert association
        db.checkInsertCount(1, BookSet.TABLE_NAME)
        // only one insert for set, more efficient than list
        db.checkInsertCount(1, AuthorSet.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue 1 insert for new entity and 1 insert for join table when adding book to existing author using join table`() {
        // given
        var (author) = createAuthorsAndBooksWithJoinTable()

        // when
        transaction {
            author = entityManager.createQuery("select a from Author a join fetch a.books b join fetch b.book where a.id = :aid", Author::class.java)
                .setParameter("aid", author.id)
                .singleResult
            val book = Book("new book")
            entityManager.persist(book)
            author.addBook(book)
        }

        // then
        // single select for authors with other tables to make it efficient. Otherwise a lot of lazy queries are executed
        db.checkQueryCount(1, Author.TABLE_NAME, joins = listOf(BookAuthor.TABLE_NAME, Book.TABLE_NAME))
        // insert other side of relationship
        db.checkInsertCount(1, Book.TABLE_NAME)
        // only one insert for set, more efficient than list
        db.checkInsertCount(1, BookAuthor.TABLE_NAME)
    }

    @Test
    fun `should issue one delete statement for association when removing association between books and authors with list`() {
        // given
        var (author) = createAuthorsAndBooksWithList()

        // when
        transaction {
            // find is more efficient than merge because Cascade.MERGE causes selects waterfall
            author = entityManager.find(AuthorList::class.java, author.id)

            val book = author.books.first()
            // remove from relationship
            author.removeBook(book)
        }

        // then
        // only entity is fetched because of lazy (can be more efficient by using jpql or query graphs)
        db.checkQueryCount(1, AuthorList.TABLE_NAME)
        // additional select to fetch associated books
        db.checkQueryCount(1, AuthorList.JOIN_TABLE_NAME, joins = listOf(BookList.TABLE_NAME))
        // and second to fetch all associations for book that we are removing
        db.checkQueryCount(1, AuthorList.JOIN_TABLE_NAME, joins = listOf(AuthorList.TABLE_NAME))
        // clear all relationships for author
        db.checkDeleteCount(1, AuthorList.JOIN_TABLE_NAME)
        // then insert all values again
        db.checkInsertCount(1, AuthorList.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue one delete statement for book and clear and re-insert remaining records for join table when removing a book from authors with list`() {
        // given
        var (author) = createAuthorsAndBooksWithList()

        // when
        transaction {
            // find is more efficient than merge because Cascade.MERGE causes selects waterfall
            author = entityManager.find(AuthorList::class.java, author.id)

            val book = author.books.first()
            // first remove from relationship
            author.removeBook(book)
            // then delete - only when you'd like to delete entity
            entityManager.remove(book)
        }

        // then
        // only entity is fetched because of lazy (can be more efficient by using jpql or query graphs)
        db.checkQueryCount(1, AuthorList.TABLE_NAME)
        // additional select to fetch associated books for author
        db.checkQueryCount(1, AuthorList.JOIN_TABLE_NAME, joins = listOf(BookList.TABLE_NAME))
        // and second to fetch all associations for book that we are removing
        db.checkQueryCount(1, AuthorList.JOIN_TABLE_NAME, joins = listOf(AuthorList.TABLE_NAME))
        // delete book
        db.checkDeleteCount(1, BookList.TABLE_NAME)
        // clear all relationships for author
        db.checkDeleteCount(1, AuthorList.JOIN_TABLE_NAME)
        // then insert all values again
        db.checkInsertCount(1, AuthorList.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue one delete statement for association when removing relationship between book and author with set`() {
        // given
        var (author) = createAuthorsAndBooksWithSet()

        // when
        transaction {
            // find is more efficient than merge because Cascade.MERGE causes selects waterfall
            author = entityManager.find(AuthorSet::class.java, author.id)

            val book = author.books.first()
            // remove from relationship
            author.removeBook(book)
        }

        // then
        // only entity is fetched because of lazy (can be more efficient by using jpql or query graphs)
        db.checkQueryCount(1, AuthorSet.TABLE_NAME)
        // additional select to fetch associated books
        db.checkQueryCount(1, AuthorSet.JOIN_TABLE_NAME, joins = listOf(BookSet.TABLE_NAME))
        // and second to fetch all associations for book that we are removing
        db.checkQueryCount(1, AuthorSet.JOIN_TABLE_NAME, joins = listOf(AuthorSet.TABLE_NAME))
        // remove only association between book and author
        db.checkDeleteCount(1, AuthorSet.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue one delete statement for book and one for association when removing a book from authors with set`() {
        // given
        var (author) = createAuthorsAndBooksWithSet()

        // when
        transaction {
            // find is more efficient than merge because Cascade.MERGE causes selects waterfall
            author = entityManager.find(AuthorSet::class.java, author.id)

            val book = author.books.first()
            // first remove from relationship
            author.removeBook(book)
            // then delete
            entityManager.remove(book)
        }

        // then
        // only entity is fetched because of lazy (can be more efficient by using jpql or query graphs)
        db.checkQueryCount(1, AuthorSet.TABLE_NAME)
        // additional select to fetch associated books for author
        db.checkQueryCount(1, AuthorSet.JOIN_TABLE_NAME, joins = listOf(BookSet.TABLE_NAME))
        // and second to fetch all associations for book that we are removing
        db.checkQueryCount(1, AuthorSet.JOIN_TABLE_NAME, joins = listOf(AuthorSet.TABLE_NAME))
        // delete book
        db.checkDeleteCount(1, BookSet.TABLE_NAME)
        // remove only association between book and author
        db.checkDeleteCount(1, AuthorSet.JOIN_TABLE_NAME)
    }

    @Test
    fun `should issue one delete statement for association when removing relationship between book and author with join table`() {
        // given
        var (author) = createAuthorsAndBooksWithJoinTable()

        // when
        transaction {
            author = entityManager.createQuery("select a from Author a join fetch a.books b join fetch b.book where a.id = :aid", Author::class.java)
                .setParameter("aid", author.id)
                .singleResult
            val bookAuthor = author.books.first()
            // orphan removal issues delete
            // additional select because removeBook accesses authors
            author.removeBook(bookAuthor.book!!)
        }

        // then
        // single select for authors with other tables to make it efficient. Otherwise a lot of lazy queries are executed
        db.checkQueryCount(1, Author.TABLE_NAME, joins = listOf(BookAuthor.TABLE_NAME, Book.TABLE_NAME))
        // get authors as we are removing relationship from books side too
        db.checkQueryCount(1, BookAuthor.TABLE_NAME)
        // single remove for the relationship
        db.checkDeleteCount(1, BookAuthor.TABLE_NAME)
    }

    @Test
    fun `should issue one delete statement for books and one for association when removing a book from authors with join table`() {
        // given
        var (author) = createAuthorsAndBooksWithJoinTable()

        // when
        transaction {
            author = entityManager.createQuery("select a from Author a join fetch a.books b join fetch b.book where a.id = :aid", Author::class.java)
                .setParameter("aid", author.id)
                .singleResult
            val bookAuthor = author.books.first()
            val book = bookAuthor.book
            // orphan removal issues delete
            // additional select because removeBook accesses authors
            author.removeBook(bookAuthor.book!!)
            // remove the book itself
            entityManager.remove(book)
        }

        // then
        // single select for authors with other tables to make it efficient. Otherwise a lot of lazy queries are executed
        db.checkQueryCount(1, Author.TABLE_NAME, joins = listOf(BookAuthor.TABLE_NAME, Book.TABLE_NAME))
        // get authors as we are removing relationship from books side too
        db.checkQueryCount(1, BookAuthor.TABLE_NAME)
        // single remove for the relationship
        db.checkDeleteCount(1, BookAuthor.TABLE_NAME)
        // additional delete that is not really related to the stuff above
        db.checkDeleteCount(1, Book.TABLE_NAME)
    }
}