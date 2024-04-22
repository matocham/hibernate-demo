package com.example.hibernatedemo.builders

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.manytomany.jointable.Author
import com.example.hibernatedemo.manytomany.jointable.Book
import com.example.hibernatedemo.manytomany.jointable.BookAuthor
import com.example.hibernatedemo.manytomany.list.AuthorList
import com.example.hibernatedemo.manytomany.list.BookList
import com.example.hibernatedemo.manytomany.set.AuthorSet
import com.example.hibernatedemo.manytomany.set.BookSet
import com.example.hibernatedemo.onetomany.elementcollection.ElementColPost
import com.example.hibernatedemo.onetomany.elementcollection.ElementColPostComment
import com.example.hibernatedemo.onetomany.fetchtypes.EagerPost
import com.example.hibernatedemo.onetomany.fetchtypes.EagerPostComment
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import com.example.hibernatedemo.onetomany.list.ListPost
import com.example.hibernatedemo.onetomany.list.ListPostComment
import com.example.hibernatedemo.onetomany.nocascade.NoCascadePost
import com.example.hibernatedemo.onetomany.nocascade.NoCascadePostComment
import com.example.hibernatedemo.onetomany.orphanremoval.OrphanedPostComment
import com.example.hibernatedemo.onetomany.orphanremoval.PostWithOrphanRemoval
import com.example.hibernatedemo.onetomany.sequenceid.SequencePost
import com.example.hibernatedemo.onetomany.sequenceid.SequencePostComment
import com.example.hibernatedemo.onetomany.set.SetPost
import com.example.hibernatedemo.onetomany.set.SetPostComment
import com.example.hibernatedemo.onetomany.sortcolumn.OrderColPost
import com.example.hibernatedemo.onetomany.sortcolumn.OrderColPostComment
import com.example.hibernatedemo.onetomany.unidirectional.*
import com.example.hibernatedemo.onetomany.version.VersionIdentityPost
import com.example.hibernatedemo.onetomany.version.VersionIdentityPostComment
import com.example.hibernatedemo.onetomany.version.VersionSeqPost
import com.example.hibernatedemo.onetomany.version.VersionSeqPostComment

object Builders {

    fun BaseDbTest.createLazyPostWith2Comments(): LazyPost = transaction {
        val post = LazyPost("post 1")
        post.addComment(LazyPostComment("comment 1"))
        post.addComment(LazyPostComment("comment 2"))

        entityManager.persist(post)
        // entities are saved straight away
        BaseDbTest.db.checkInsertCount(1, LazyPost.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, LazyPostComment.TABLE_NAME)

        post
    }!!

    fun BaseDbTest.createEagerPostWith2Comments(): EagerPost = transaction {
        val post = EagerPost("test 1")
        post.addComment(EagerPostComment("comment 1"))
        post.addComment(EagerPostComment("comment 2"))
        entityManager.persist(post)

        BaseDbTest.db.checkInsertCount(1, EagerPost.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, EagerPostComment.TABLE_NAME)

        post
    }!!

    fun BaseDbTest.createNoCascadePostWith2Comments(): NoCascadePost = transaction {
        val post = NoCascadePost("test 1")
        post.addComment(NoCascadePostComment("comment 1"))
        post.addComment(NoCascadePostComment("comment 2"))
        entityManager.persist(post)

        BaseDbTest.db.checkInsertCount(1, NoCascadePost.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, NoCascadePostComment.TABLE_NAME)

        post
    }!!

    fun BaseDbTest.createPostWithOrphanRemovalWith2Comments(): PostWithOrphanRemoval = transaction {
        val post = PostWithOrphanRemoval("test 1")
        post.addComment(OrphanedPostComment("comment 1"))
        post.addComment(OrphanedPostComment("comment 2"))
        entityManager.persist(post)

        BaseDbTest.db.checkInsertCount(1, PostWithOrphanRemoval.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, OrphanedPostComment.TABLE_NAME)

        post
    }!!

    fun BaseDbTest.createSeqPostWith2Comments(): SequencePost {
        val post = transaction {
            val post = SequencePost("post 1")
            post.addComment(SequencePostComment("comment 1"))
            post.addComment(SequencePostComment("comment 2"))

            entityManager.persist(post)
            // immediately get sequence values
            BaseDbTest.db.checkNextValCount(1, SequencePost.SEQUENCE_NAME)
            BaseDbTest.db.checkNextValCount(1, SequencePostComment.SEQUENCE_NAME)
            BaseDbTest.db.checkInsertCount(0, SequencePost.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(0, SequencePostComment.TABLE_NAME)

            post
        }

        // entities are persisted only after transaction
        BaseDbTest.db.checkInsertCount(1, SequencePost.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, SequencePostComment.TABLE_NAME)

        return post!!
    }

    fun BaseDbTest.createListPostWith2Comments(): ListPost = transaction {
        val post = ListPost("test 1")
        post.addComment(ListPostComment("comment 1"))
        post.addComment(ListPostComment("comment 2"))
        entityManager.persist(post)

        BaseDbTest.db.checkInsertCount(1, ListPost.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, ListPostComment.TABLE_NAME)

        post
    }!!

    fun BaseDbTest.createSetPostWith2Comments(): SetPost = transaction {
        val post = SetPost("test 1")
        post.addComment(SetPostComment("comment 1"))
        post.addComment(SetPostComment("comment 2"))
        entityManager.persist(post)

        BaseDbTest.db.checkInsertCount(1, SetPost.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, SetPostComment.TABLE_NAME)

        post
    }!!


    fun BaseDbTest.createOrderColPostWith2Comments(): OrderColPost {
        val post = transaction {
            val post = OrderColPost("test 1")
            post.addComment(OrderColPostComment("comment 1"))
            post.addComment(OrderColPostComment("comment 2"))
            entityManager.persist(post)

            BaseDbTest.db.checkInsertCount(1, OrderColPost.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(2, OrderColPostComment.TABLE_NAME)

            post
        }!!

        // additional updates to set up ordering
        BaseDbTest.db.checkUpdateCount(2, OrderColPostComment.TABLE_NAME)
        return post
    }

    fun BaseDbTest.createElementColPostWith2CommentsAndTags(): ElementColPost {
        val post = transaction {
            val post = ElementColPost("test 1")
            post.comments.add(ElementColPostComment("comment 1", "a 1"))
            post.comments.add(ElementColPostComment("comment 2", "a 2"))

            post.tags.add("TAG_1")
            post.tags.add("TAG_2")
            entityManager.persist(post)

            BaseDbTest.db.checkInsertCount(1, ElementColPost.TABLE_NAME)

            post
        }!!

        BaseDbTest.db.checkInsertCount(2, ElementColPost.CHILD_TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, ElementColPost.TAGS_TABLE_NAME)

        return post
    }

    fun BaseDbTest.createUniListPostWith2Comments(): UnidirectionalListPost {
        val post = transaction {
            val post = UnidirectionalListPost("test 1")
            post.addComment(UnidirectionalListPostComment("comment 1"))
            post.addComment(UnidirectionalListPostComment("comment 2"))
            entityManager.persist(post)

            BaseDbTest.db.checkInsertCount(1, UnidirectionalListPost.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(2, UnidirectionalListPostComment.TABLE_NAME)

            post
        }!!

        BaseDbTest.db.checkUpdateCount(2, UnidirectionalListPostComment.TABLE_NAME)
        return post
    }


    fun BaseDbTest.createUniSetPostWith2Comments(): UnidirectionalSetPost {
        val post = transaction {
            val post = UnidirectionalSetPost("test 1")
            post.addComment(UnidirectionalSetPostComment("comment 1"))
            post.addComment(UnidirectionalSetPostComment("comment 2"))
            entityManager.persist(post)

            BaseDbTest.db.checkInsertCount(1, UnidirectionalSetPost.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(2, UnidirectionalSetPostComment.TABLE_NAME)

            post
        }!!

        BaseDbTest.db.checkUpdateCount(2, UnidirectionalSetPostComment.TABLE_NAME)
        return post
    }

    fun BaseDbTest.createNoJoinColPostWith2Comments(): UniNoJoinColumnPost {
        val post = transaction {
            val post = UniNoJoinColumnPost("test 1")
            post.addComment(UniNoJoinColumnPostComment("comment 1"))
            post.addComment(UniNoJoinColumnPostComment("comment 2"))
            entityManager.persist(post)

            BaseDbTest.db.checkInsertCount(1, UniNoJoinColumnPost.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(2, UniNoJoinColumnPostComment.TABLE_NAME)

            post
        }!!

        BaseDbTest.db.checkInsertCount(2, UniNoJoinColumnPost.JOIN_TABLE_NAME)
        return post
    }

    fun BaseDbTest.createAuthorsAndBooksWithList(): List<AuthorList> {
        val authors = transaction {
            val author = AuthorList("author 1")
            val author2 = AuthorList("author 2")
            val book = BookList("book 1")
            val book2 = BookList("book 2")

            author.addBook(book)
            author.addBook(book2)
            author2.addBook(book2)

            // second author gets persisted because of cascade settings on book entity
            entityManager.persist(author)

            BaseDbTest.db.checkInsertCount(2, AuthorList.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(2, BookList.TABLE_NAME)

            listOf(author, author2)
        }

        // join table is persisted at the end as there is no need to do it earlier
        BaseDbTest.db.checkInsertCount(3, AuthorList.JOIN_TABLE_NAME)

        return authors!!
    }

    fun BaseDbTest.createAuthorsAndBooksWithSet(): List<AuthorSet> {
        val authors = transaction {
            val author = AuthorSet("author 1")
            val author2 = AuthorSet("author 2")
            val book = BookSet("book 1")
            val book2 = BookSet("book 2")

            author.addBook(book)
            author.addBook(book2)
            author2.addBook(book2)

            // second author gets persisted because of cascade settings on book entity
            entityManager.persist(author)

            BaseDbTest.db.checkInsertCount(2, AuthorSet.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(2, BookSet.TABLE_NAME)
            listOf(author, author2)
        }

        BaseDbTest.db.checkInsertCount(3, AuthorSet.JOIN_TABLE_NAME)
        return authors!!
    }

    fun BaseDbTest.createAuthorsAndBooksWithJoinTable(): List<Author> {
        // when
        val authors = transaction {
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

            BaseDbTest.db.checkInsertCount(2, Author.TABLE_NAME)
            BaseDbTest.db.checkInsertCount(2, Book.TABLE_NAME)

            listOf(author, author2)
        }

        // join table is persisted at the end as there is no need to do it earlier
        BaseDbTest.db.checkInsertCount(3, BookAuthor.TABLE_NAME)
        return authors!!
    }

    fun getVerIdentityPostWith2Comments(): VersionIdentityPost {
        val post = VersionIdentityPost("post 1")
        post.addComment(VersionIdentityPostComment("comment 1"))
        post.addComment(VersionIdentityPostComment("comment 2"))

        return post
    }

    fun BaseDbTest.createVerIdentityPostWith2Comments(): VersionIdentityPost {
        val post = getVerIdentityPostWith2Comments()

        transaction {
            entityManager.persist(post)
        }

        BaseDbTest.db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
        BaseDbTest.db.checkInsertCount(2, VersionIdentityPostComment.TABLE_NAME)

        return post
    }

    fun getSeqIdentityPostWith2Comments(): VersionSeqPost {
        val post = VersionSeqPost("post 1")
        post.addComment(VersionSeqPostComment("comment 1"))
        post.addComment(VersionSeqPostComment("comment 2"))

        return post
    }
}