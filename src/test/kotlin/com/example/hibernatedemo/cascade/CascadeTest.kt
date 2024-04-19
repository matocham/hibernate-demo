package com.example.hibernatedemo.cascade

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.fetchtypes.*
import com.example.hibernatedemo.nocascade.NoCascadePost
import com.example.hibernatedemo.nocascade.NoCascadePostComment
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException

class CascadeTest : BaseDbTest() {
    @Test
    fun `should remove parent with it's children when parent is removed and cascade is set to ALL`() {
        val postId = createLazyPostWith2Comments()

        transaction {
            val post = entityManager.createQuery(
                "select p from LazyPost p join fetch p.comments where p.id = :postId",
                LazyPost::class.java
            )
                .setParameter("postId", postId)
                .singleResult

            db.checkQueryCount(1, "lazy_posts")
            db.checkQueryCount(0, "lazy_post_comments")

            entityManager.remove(post)
        }

        // do outside of transaction so that it is flushed
        db.checkDeleteCount(1, "lazy_posts")
        db.checkDeleteCount(2, "lazy_post_comments")
    }

    @Test
    fun `should propagate remove operation when parent entity is not fetched from DB and cascade is set to ALL`() {
        val postId = createLazyPostWith2Comments()

        transaction {
            entityManager.remove(entityManager.getReference(LazyPost::class.java, postId))
        }

        // hibernate will initialize reference and fetch post comments to make sure cascade works
        db.checkQueryCount(1, "lazy_posts")
        db.checkQueryCount(1, "lazy_post_comments")

        // do outside of transaction so that it is flushed
        db.checkDeleteCount(1, "lazy_posts")
        db.checkDeleteCount(2, "lazy_post_comments")
    }

    @Test
    fun `should fail with constraint violation when parent entity is not fetched from DB and cascade is set to ALL and JPQL is used for delete operation`() {
        val postId = createLazyPostWith2Comments()

        assertThrows<ConstraintViolationException> {
            transaction {
                entityManager.createQuery("delete from LazyPost p where p.id = :postId")
                    .setParameter("postId", postId)
                    .executeUpdate()
            }
        }

        // do outside of transaction so that it is flushed
        // delete is executed but it fails
        db.checkDeleteCount(1, "lazy_posts")
        db.checkDeleteCount(0, "lazy_post_comments")
    }

    @Test
    fun `should fail with data integrity violation when parent entity is fetched from DB with children and cascade does not include REMOVE`() {
        val postId = createNoCascadePostWith2Comments()

        assertThrows<DataIntegrityViolationException> {
            transaction {
                val post = entityManager.createQuery(
                    "select p from NoCascadePost p join fetch p.comments where p.id = :postId",
                    NoCascadePost::class.java
                )
                    .setParameter("postId", postId)
                    .singleResult

                entityManager.remove(post)
            }
        }

        // do outside of transaction so that it is flushed
        // delete is executed but it fails
        db.checkDeleteCount(1, "no_cascade_posts")
        db.checkDeleteCount(0, "no_cascade_post_comments")
    }

    @Test
    fun `should fail with data integrity violation when parent entity is not fetched from DB and cascade does not include REMOVE`() {
        val postId = createNoCascadePostWith2Comments()

        assertThrows<DataIntegrityViolationException> {
            transaction {
                entityManager.remove(entityManager.getReference(NoCascadePost::class.java, postId))
            }
        }

        // do outside of transaction so that it is flushed
        // delete is executed but it fails
        db.checkDeleteCount(1, "no_cascade_posts")
        db.checkDeleteCount(0, "no_cascade_post_comments")
    }

    private fun createLazyPostWith2Comments(): Long? {
        val postId = transaction {
            val post = LazyPost("test 1")
            post.addComment(LazyPostComment("comment 1"))
            post.addComment(LazyPostComment("comment 2"))
            entityManager.persist(post)
            post.id
        }

        db.checkInsertCount(1, "lazy_posts")
        db.checkInsertCount(2, "lazy_post_comments")
        return postId
    }

    private fun createNoCascadePostWith2Comments(): Long? {
        val postId = transaction {
            val post = NoCascadePost("test 1")
            post.addComment(NoCascadePostComment("comment 1"))
            post.addComment(NoCascadePostComment("comment 2"))
            entityManager.persist(post)
            post.id
        }

        db.checkInsertCount(1, "no_cascade_posts")
        db.checkInsertCount(2, "no_cascade_post_comments")
        return postId
    }
}