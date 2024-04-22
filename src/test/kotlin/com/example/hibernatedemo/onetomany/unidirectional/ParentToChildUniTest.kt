package com.example.hibernatedemo.onetomany.unidirectional

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.builders.Builders.createListPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createNoJoinColPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createSetPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createUniListPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createUniSetPostWith2Comments
import org.junit.jupiter.api.Test

class ParentToChildUniTest : BaseDbTest() {

    @Test
    fun `should execute single insert statement for parent and insert and update for children when children are list`() {
        // when
        transaction {
            val post = UnidirectionalListPost("test 1")
            post.addComment(UnidirectionalListPostComment("comment 1"))
            post.addComment(UnidirectionalListPostComment("comment 2"))
            // it would fail if join column was not nullable
            entityManager.persist(post)
        }

        // then
        db.checkInsertCount(1, UnidirectionalListPost.TABLE_NAME)
        db.checkInsertCount(2, UnidirectionalListPostComment.TABLE_NAME)
        db.checkUpdateCount(2, UnidirectionalListPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single insert statement for parent and insert and update for children when children are set`() {
        // when
        transaction {
            val post = UnidirectionalSetPost("test 1")
            post.addComment(UnidirectionalSetPostComment("comment 1"))
            post.addComment(UnidirectionalSetPostComment("comment 2"))
            entityManager.persist(post)
        }

        // then
        db.checkInsertCount(1, UnidirectionalSetPost.TABLE_NAME)
        db.checkInsertCount(2, UnidirectionalSetPostComment.TABLE_NAME)
        db.checkUpdateCount(2, UnidirectionalSetPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single insert statement for parent and inserts for children and join table when join column annotation is not used`() {
        // when
        transaction {
            val post = UniNoJoinColumnPost("test 1")
            post.addComment(UniNoJoinColumnPostComment("comment 1"))
            post.addComment(UniNoJoinColumnPostComment("comment 2"))
            entityManager.persist(post)
        }

        // then
        db.checkInsertCount(1, UniNoJoinColumnPost.TABLE_NAME)
        db.checkInsertCount(2, UniNoJoinColumnPostComment.TABLE_NAME)
        db.checkInsertCount(2, UniNoJoinColumnPost.JOIN_TABLE_NAME)
    }

    @Test
    fun `should execute single insert statement and single update statement when adding a child to the list`() {
        // given
        var post = createUniListPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.addComment(UnidirectionalListPostComment("a new one"))
        }
        // for merge
        db.checkQueryCount(
            1,
            UnidirectionalListPost.TABLE_NAME,
            joins = listOf(UnidirectionalListPostComment.TABLE_NAME)
        ) // one query with join thanks to cascade

        // then
        db.checkInsertCount(1, UnidirectionalListPostComment.TABLE_NAME)
        db.checkUpdateCount(1, UnidirectionalListPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single insert statement when adding a child to the set`() {
        // given
        var post = createUniSetPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.addComment(UnidirectionalSetPostComment("a new one"))
        }
        // for merge
        db.checkQueryCount(
            1,
            UnidirectionalSetPost.TABLE_NAME,
            joins = listOf(UnidirectionalSetPostComment.TABLE_NAME)
        ) // one query with join thanks to cascade

        // then
        db.checkInsertCount(1, UnidirectionalSetPostComment.TABLE_NAME)
        db.checkUpdateCount(1, UnidirectionalSetPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single delete and 3 inserts for join table and 1 insert into child table when adding a child to list without join column annotation`() {
        // given
        var post = createNoJoinColPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.addComment(UniNoJoinColumnPostComment("a new one"))
        }
        db.checkQueryCount(
            1,
            UniNoJoinColumnPost.TABLE_NAME,
            joins = listOf(UniNoJoinColumnPost.JOIN_TABLE_NAME, UniNoJoinColumnPostComment.TABLE_NAME)
        ) // one query with join thanks to cascade

        // then
        db.checkDeleteCount(1, UniNoJoinColumnPost.JOIN_TABLE_NAME)
        db.checkInsertCount(1, UniNoJoinColumnPostComment.TABLE_NAME)
        db.checkInsertCount(3, UniNoJoinColumnPost.JOIN_TABLE_NAME)
    }

    @Test
    fun `should execute single update statement and single delete statement when removing a child from the list`() {
        // given
        var post = createUniListPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.removeComment(post.comments.first())
        }
        // for merge
        db.checkQueryCount(
            1,
            UnidirectionalListPost.TABLE_NAME,
            joins = listOf(UnidirectionalListPostComment.TABLE_NAME)
        ) // one query with join thanks to cascade

        // then
        // first sets post id to null
        db.checkUpdateCount(1, UnidirectionalListPostComment.TABLE_NAME)
        db.checkDeleteCount(1, UnidirectionalListPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single update statement and single delete statement when removing a child from the set`() {
        // given
        var post = createUniSetPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.removeComment(post.comments.first())
        }
        // for merge
        db.checkQueryCount(
            1,
            UnidirectionalSetPost.TABLE_NAME,
            joins = listOf(UnidirectionalSetPostComment.TABLE_NAME)
        ) // one query with join thanks to cascade

        // then
        // first set post_id to null
        db.checkUpdateCount(1, UnidirectionalSetPostComment.TABLE_NAME)
        db.checkDeleteCount(1, UnidirectionalSetPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement in child and join tables and one insert in join table when removing an item from list without join column annotation`() {
        // given
        var post = createNoJoinColPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.removeComment(post.comments.first())
        }
        db.checkQueryCount(
            1,
            UniNoJoinColumnPost.TABLE_NAME,
            joins = listOf(UniNoJoinColumnPost.JOIN_TABLE_NAME, UniNoJoinColumnPostComment.TABLE_NAME)
        ) // one query with join thanks to cascade

        // then
        // first clears join table for given post
        db.checkDeleteCount(1, UniNoJoinColumnPost.JOIN_TABLE_NAME)
        db.checkInsertCount(1, UniNoJoinColumnPost.JOIN_TABLE_NAME)
        db.checkDeleteCount(1, UniNoJoinColumnPostComment.TABLE_NAME)
    }
}