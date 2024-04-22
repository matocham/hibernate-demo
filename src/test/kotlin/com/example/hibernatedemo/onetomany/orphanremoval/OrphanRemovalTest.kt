package com.example.hibernatedemo.onetomany.orphanremoval

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.builders.Builders.createLazyPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createPostWithOrphanRemovalWith2Comments
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import com.example.hibernatedemo.onetomany.orphanremoval.OrphanedPostComment
import com.example.hibernatedemo.onetomany.orphanremoval.PostWithOrphanRemoval
import org.junit.jupiter.api.Test

class OrphanRemovalTest : BaseDbTest() {
    @Test
    fun `should require manual child removal when child entity is removed from parent list and orphan removal is false`() {
        transaction {
            var post = createLazyPostWith2Comments()
            entityManager.flush()

            post = entityManager.find(LazyPost::class.java, post.id)!!
            val firstComment = post.comments.first()

            post.removeComment(firstComment.id!!)
            entityManager.flush()

            db.checkDeleteCount(0, LazyPostComment.TABLE_NAME)
            // post id is removed but not child
            db.checkUpdateCount(1, LazyPostComment.TABLE_NAME)
        }
    }

    @Test
    fun `should remove child when it is removed from parent list when orphan removal is true`() {
        transaction {
            var post = createPostWithOrphanRemovalWith2Comments()
            entityManager.flush()

            post = entityManager.find(PostWithOrphanRemoval::class.java, post.id)!!
            val firstComment = post.comments.first()

            post.removeComment(firstComment.id!!)
            entityManager.flush()

            // should remove child
            db.checkDeleteCount(1, OrphanedPostComment.TABLE_NAME)
            db.checkUpdateCount(0, OrphanedPostComment.TABLE_NAME)
        }
    }
}