package com.example.hibernatedemo.fetchtypes

import com.example.hibernatedemo.BaseDbTest
import mu.KotlinLogging
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class FetchTypesTest : BaseDbTest() {
    @Test
    fun `should fetch parent entity without children when lazy fetchType is used`() {
        val postId = createLazyPostWith2Comments()

        transaction {
            val post = entityManager.find(LazyPost::class.java, postId)!!

            db.checkQueryCount(1, "lazy_posts")
            db.checkQueryCount(0, "lazy_post_comments")

            post.comments.forEach { logger.info { it.id } }

            // all post comments are fetched at once
            db.checkQueryCount(1, "lazy_post_comments")
        }
    }

    @Test
    fun `child entities can be fetched with parent when asked to`() {
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

            post.comments.forEach { logger.info { it.id } }

            //no need to fetch comments seperately
            db.checkQueryCount(0, "lazy_post_comments")
        }
    }

    @Test
    fun `should fetch parent entity with children when eager fetchType is used`() {
        val postId = createEagerPostWith2Comments()

        transaction {
            val post = entityManager.find(EagerPost::class.java, postId)!!

            db.checkQueryCount(1, "eager_posts")
            db.checkQueryCount(0, "eager_post_comments")

            post.comments.forEach { logger.info { it.id } }

            // all post comments were fetched together with post
            db.checkQueryCount(0, "eager_post_comments")
        }
    }

    @Test
    fun `should NOT fetch children with parent entity when eager fetchType is used for JPQL query`() {
        val postId = createEagerPostWith2Comments()

        transaction {
            val post =
                entityManager.createQuery("select p from EagerPost p where p.id = :postId", EagerPost::class.java)
                    .setParameter("postId", postId)
                    .singleResult

            db.checkQueryCount(1, "eager_posts")
            // this time fetch is done right after main entity is fetched
            db.checkQueryCount(1, "eager_post_comments")

            post.comments.forEach { logger.info { it.id } }

            // no new queries executed
            db.checkQueryCount(1, "eager_post_comments")
        }
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

    private fun createEagerPostWith2Comments(): Long? {
        val postId = transaction {
            val post = EagerPost("test 1")
            post.addComment(EagerPostComment("comment 1"))
            post.addComment(EagerPostComment("comment 2"))
            entityManager.persist(post)
            post.id
        }

        db.checkInsertCount(1, "eager_posts")
        db.checkInsertCount(2, "eager_post_comments")
        return postId
    }
}
