package com.example.hibernatedemo.onetomany.cascade

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.builders.Builders.createLazyPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createNoCascadePostWith2Comments
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import com.example.hibernatedemo.onetomany.nocascade.NoCascadePost
import com.example.hibernatedemo.onetomany.nocascade.NoCascadePostComment
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.dao.DataIntegrityViolationException

class CascadeTest : BaseDbTest() {
    @Test
    fun `should remove parent with it's children when parent is removed and cascade is set to ALL`() {
        val post = createLazyPostWith2Comments()

        transaction {
            val post = entityManager.createQuery(
                "select p from LazyPost p join fetch p.comments where p.id = :postId",
                LazyPost::class.java
            )
                .setParameter("postId", post.id)
                .singleResult

            db.checkQueryCount(1, LazyPost.TABLE_NAME, joins = listOf(LazyPostComment.TABLE_NAME))
            db.checkQueryCount(0, LazyPostComment.TABLE_NAME)

            entityManager.remove(post)
        }

        // do outside of transaction so that it is flushed
        db.checkDeleteCount(1, LazyPost.TABLE_NAME)
        db.checkDeleteCount(2, LazyPostComment.TABLE_NAME)
    }

    @Test
    fun `should propagate remove operation when parent entity is not fetched from DB and cascade is set to ALL`() {
        val post = createLazyPostWith2Comments()

        transaction {
            entityManager.remove(entityManager.getReference(LazyPost::class.java, post.id))
        }

        // hibernate will initialize reference and fetch post comments to make sure cascade works
        db.checkQueryCount(1, LazyPost.TABLE_NAME)
        db.checkQueryCount(1, LazyPostComment.TABLE_NAME)

        // do outside of transaction so that it is flushed
        db.checkDeleteCount(1, LazyPost.TABLE_NAME)
        db.checkDeleteCount(2, LazyPostComment.TABLE_NAME)
    }

    @Test
    fun `should fail with constraint violation when parent entity is not fetched from DB and cascade is set to ALL and JPQL is used for delete operation`() {
        var post = createLazyPostWith2Comments()

        assertThrows<ConstraintViolationException> {
            transaction {
                entityManager.createQuery("delete from LazyPost p where p.id = :postId")
                    .setParameter("postId", post.id)
                    .executeUpdate()
            }
        }

        // do outside of transaction so that it is flushed
        // delete is executed but it fails
        db.checkDeleteCount(1, LazyPost.TABLE_NAME)
        db.checkDeleteCount(0, LazyPostComment.TABLE_NAME)
    }

    @Test
    fun `should fail with data integrity violation when parent entity is fetched from DB with children and cascade does not include REMOVE`() {
        val post = createNoCascadePostWith2Comments()

        assertThrows<DataIntegrityViolationException> {
            transaction {
                val post = entityManager.createQuery(
                    "select p from NoCascadePost p join fetch p.comments where p.id = :postId",
                    NoCascadePost::class.java
                )
                    .setParameter("postId", post.id)
                    .singleResult

                entityManager.remove(post)
            }
        }

        db.checkQueryCount(1, NoCascadePost.TABLE_NAME, joins = listOf(NoCascadePostComment.TABLE_NAME))
        // do outside of transaction so that it is flushed
        // delete is executed but it fails
        db.checkDeleteCount(1, NoCascadePost.TABLE_NAME)
        db.checkDeleteCount(0, NoCascadePostComment.TABLE_NAME)
    }

    @Test
    fun `should fail with data integrity violation when parent entity is not fetched from DB and cascade does not include REMOVE`() {
        val post = createNoCascadePostWith2Comments()

        assertThrows<DataIntegrityViolationException> {
            transaction {
                entityManager.remove(entityManager.getReference(NoCascadePost::class.java, post.id))
            }
        }

        // do outside of transaction so that it is flushed
        // delete is executed but it fails
        db.checkDeleteCount(1, NoCascadePost.TABLE_NAME)
        db.checkDeleteCount(0, NoCascadePostComment.TABLE_NAME)
    }
}