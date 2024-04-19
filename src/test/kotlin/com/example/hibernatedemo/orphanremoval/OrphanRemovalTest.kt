package com.example.hibernatedemo.orphanremoval

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.fetchtypes.LazyPost
import com.example.hibernatedemo.fetchtypes.LazyPostComment
import org.junit.jupiter.api.Test

class OrphanRemovalTest: BaseDbTest() {
    @Test
    fun `should require manual child removal when child entity is removed from parent list and orphan removal is false`() {
        transaction {
            val postId = createLazyPostWith2Comments()
            entityManager.flush()

            db.checkInsertCount(1, "lazy_posts")
            db.checkInsertCount(2, "lazy_post_comments")

            val post = entityManager.find(LazyPost::class.java, postId)!!
            val firstComment = post.comments.first()

            post.removeComment(firstComment.id!!)
            entityManager.flush()

            db.checkDeleteCount(0, "lazy_post_comments")
            // post id is removed but not child
            db.checkUpdateCount(1, "lazy_post_comments")
        }
    }

    @Test
    fun `should remove child when it is removed from parent list when orphan removal is true`() {
        transaction {
            val postId = createPostWithOrphanRemovalWith2Comments()
            entityManager.flush()

            db.checkInsertCount(1, "orphan_removal_posts")
            db.checkInsertCount(2, "orphaned_post_comments")

            val post = entityManager.find(PostWithOrphanRemoval::class.java, postId)!!
            val firstComment = post.comments.first()

            post.removeComment(firstComment.id!!)
            entityManager.flush()

            // should remove child
            db.checkDeleteCount(1, "orphaned_post_comments")
            db.checkUpdateCount(0, "orphaned_post_comments")
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

    private fun createPostWithOrphanRemovalWith2Comments(): Long? {
        val postId = transaction {
            val post = PostWithOrphanRemoval("test 1")
            post.addComment(OrphanedPostComment("comment 1"))
            post.addComment(OrphanedPostComment("comment 2"))
            entityManager.persist(post)
            post.id
        }

        db.checkInsertCount(1, "orphan_removal_posts")
        db.checkInsertCount(2, "orphaned_post_comments")
        return postId
    }
}