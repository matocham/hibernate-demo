package com.example.hibernatedemo.onetomany.fetchtypes

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.builders.Builders.createEagerPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createLazyPostWith2Comments
import com.example.hibernatedemo.onetomany.fetchtypes.EagerPost
import com.example.hibernatedemo.onetomany.fetchtypes.EagerPostComment
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import mu.KotlinLogging
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class FetchTypesTest : BaseDbTest() {
    @Test
    fun `should fetch parent entity without children when lazy fetchType is used`() {
        val post = createLazyPostWith2Comments()

        transaction {
            val post = entityManager.find(LazyPost::class.java, post.id)!!

            db.checkQueryCount(1, LazyPost.TABLE_NAME)
            db.checkQueryCount(0, LazyPostComment.TABLE_NAME)

            post.comments.forEach { logger.info { it.id } }

            // all post comments are fetched at once
            db.checkQueryCount(1, LazyPostComment.TABLE_NAME)
        }
    }

    @Test
    fun `child entities can be fetched with parent when asked to`() {
        var post = createLazyPostWith2Comments()

        transaction {
            post = entityManager.createQuery(
                "select p from LazyPost p join fetch p.comments where p.id = :postId",
                LazyPost::class.java
            )
                .setParameter("postId", post.id)
                .singleResult

            db.checkQueryCount(1, LazyPost.TABLE_NAME, joins = listOf(LazyPostComment.TABLE_NAME))
            db.checkQueryCount(0, LazyPostComment.TABLE_NAME)

            post.comments.forEach { logger.info { it.id } }

            //no need to fetch comments seperately
            db.checkQueryCount(0, LazyPostComment.TABLE_NAME)
        }
    }

    @Test
    fun `should fetch parent entity with children when eager fetchType is used`() {
        val post = createEagerPostWith2Comments()

        transaction {
            val post = entityManager.find(EagerPost::class.java, post.id)!!

            db.checkQueryCount(1, EagerPost.TABLE_NAME, joins = listOf(EagerPostComment.TABLE_NAME))
            db.checkQueryCount(0, EagerPostComment.TABLE_NAME)

            post.comments.forEach { logger.info { it.id } }

            // all post comments were fetched together with post
            db.checkQueryCount(0, EagerPostComment.TABLE_NAME)
        }
    }

    @Test
    fun `should NOT fetch children with parent entity when eager fetchType is used for JPQL query`() {
        var post = createEagerPostWith2Comments()

        transaction {
            post =
                entityManager.createQuery("select p from EagerPost p where p.id = :postId", EagerPost::class.java)
                    .setParameter("postId", post.id)
                    .singleResult

            db.checkQueryCount(1, EagerPost.TABLE_NAME)
            // this time fetch is done right after main entity is fetched
            db.checkQueryCount(1, EagerPostComment.TABLE_NAME)

            post.comments.forEach { logger.info { it.id } }
        }
    }
}
