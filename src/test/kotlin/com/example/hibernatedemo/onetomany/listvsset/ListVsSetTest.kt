package com.example.hibernatedemo.onetomany.listvsset

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.builders.Builders.createElementColPostWith2CommentsAndTags
import com.example.hibernatedemo.builders.Builders.createListPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createOrderColPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createSetPostWith2Comments
import com.example.hibernatedemo.onetomany.elementcollection.ElementColPost
import com.example.hibernatedemo.onetomany.elementcollection.ElementColPostComment
import com.example.hibernatedemo.onetomany.list.ListPost
import com.example.hibernatedemo.onetomany.list.ListPostComment
import com.example.hibernatedemo.onetomany.set.SetPost
import com.example.hibernatedemo.onetomany.set.SetPostComment
import com.example.hibernatedemo.onetomany.sortcolumn.OrderColPost
import com.example.hibernatedemo.onetomany.sortcolumn.OrderColPostComment
import org.junit.jupiter.api.Test

class ListVsSetTest : BaseDbTest() {
    @Test
    fun `should execute single insert statement when adding new child to the list`() {
        // given
        val detachedPost = createListPostWith2Comments()

        // when
        transaction {
            val post = entityManager.createQuery(
                "select p from ListPost p join fetch p.comments where p.id = :postId",
                ListPost::class.java
            )
                .setParameter("postId", detachedPost.id).singleResult
            post.addComment(ListPostComment("a new one"))
        }

        // then
        // 1 query to fetch entity
        db.checkQueryCount(1, ListPost.TABLE_NAME, joins = listOf(ListPostComment.TABLE_NAME))
        // should be one additional insert
        db.checkInsertCount(1, ListPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single insert statement when adding new child to the set`() {
        // given
        val post = createSetPostWith2Comments()

        // when
        transaction {
            post.addComment(SetPostComment("a new one"))
            // a different way to add entity to persistence context
            entityManager.merge(post)
        }

        // then
        // 1 query to fetch entity
        db.checkQueryCount(1, SetPost.TABLE_NAME, joins = listOf(SetPostComment.TABLE_NAME))
        // should be one additional insert
        db.checkInsertCount(1, SetPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single insert statement and 3 updates when adding new child to the list with order column`() {
        // given
        var post = createOrderColPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.addComment(OrderColPostComment("a new one"))
        }

        // then
        // 1 query to fetch entity
        db.checkQueryCount(1, OrderColPost.TABLE_NAME, joins = listOf(OrderColPostComment.TABLE_NAME))
        // should be one additional insert
        db.checkInsertCount(1, OrderColPostComment.TABLE_NAME)
        // 3 updates, one for each child
        db.checkUpdateCount(3, OrderColPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and then add all elements when adding an element to element collection list`() {
        // given
        var post = createElementColPostWith2CommentsAndTags()

        // when
        transaction {
            // merge synchronizes all collection at the start
            post = entityManager.merge(post)
            // all collections are fetched separately because of LAZY fetch type on ElementCollection
            db.checkQueryCount(1, ElementColPost.TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.CHILD_TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.TAGS_TABLE_NAME)

            post.comments.add(ElementColPostComment("a new one", "new author"))
        }

        // then
        // all elements are removed by default
        // can be optimized by adding OrderColumn annotation
        // https://vladmihalcea.com/how-to-optimize-unidirectional-collections-with-jpa-and-hibernate/
        db.checkDeleteCount(1, ElementColPost.CHILD_TABLE_NAME)
        // should be 3 inserts
        db.checkInsertCount(3, ElementColPost.CHILD_TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and then add all elements when adding an element to element collection containing primitive type`() {
        // given
        var post = createElementColPostWith2CommentsAndTags()

        // when
        transaction {
            post = entityManager.find(ElementColPost::class.java, post.id)
            // only parent is fetched by default because of LAZY fetch type on ElementCollection
            db.checkQueryCount(1, ElementColPost.TABLE_NAME)
            db.checkQueryCount(0, ElementColPost.CHILD_TABLE_NAME)
            db.checkQueryCount(0, ElementColPost.TAGS_TABLE_NAME)

            post.tags.add("TAG_3")
            // tags are fetched because we modify them
            db.checkQueryCount(1, ElementColPost.TAGS_TABLE_NAME)
        }

        // all elements are removed by default
        db.checkDeleteCount(1, ElementColPost.TAGS_TABLE_NAME)
        // should be 3 inserts
        db.checkInsertCount(3, ElementColPost.TAGS_TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement when removing a child from the list`() {
        // given
        var post = createListPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.removeComment(post.comments[0].id!!)
        }

        // then
        // 1 query to fetch entity
        db.checkQueryCount(1, ListPost.TABLE_NAME, joins = listOf(ListPostComment.TABLE_NAME))
        db.checkDeleteCount(1, ListPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement when removing a child from the set`() {
        // given
        var post = createSetPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.removeComment(post.comments.first().id!!)
        }

        // then
        // 1 query to fetch entity
        db.checkQueryCount(1, SetPost.TABLE_NAME, joins = listOf(SetPostComment.TABLE_NAME))
        db.checkDeleteCount(1, SetPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and 1 update when removing first element form the list with order column`() {
        // given
        var post = createOrderColPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.removeComment(post.comments.first().id!!)
        }

        // then
        // 1 query to fetch entity
        db.checkQueryCount(1, OrderColPost.TABLE_NAME, joins = listOf(OrderColPostComment.TABLE_NAME))
        db.checkDeleteCount(1, OrderColPostComment.TABLE_NAME)
        // update count in the remaining child
        db.checkUpdateCount(1, OrderColPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and 1 insert when removing first element form the element collection list`() {
        // given
        var post = createElementColPostWith2CommentsAndTags()

        // when
        transaction {
            post = entityManager.merge(post)
            db.checkQueryCount(1, ElementColPost.TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.CHILD_TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.TAGS_TABLE_NAME)

            post.comments.remove(post.comments.first())
        }

        // then
        db.checkDeleteCount(1, ElementColPost.CHILD_TABLE_NAME)
        db.checkInsertCount(1, ElementColPost.CHILD_TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and 1 insert when removing first element form the element collection list containing primitive type`() {
        // given
        var post = createElementColPostWith2CommentsAndTags()

        // when
        transaction {
            post = entityManager.merge(post)
            db.checkQueryCount(1, ElementColPost.TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.CHILD_TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.TAGS_TABLE_NAME)

            post.tags.remove(post.tags.first())
        }

        // then
        db.checkDeleteCount(1, ElementColPost.TAGS_TABLE_NAME)
        db.checkInsertCount(1, ElementColPost.TAGS_TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and 1 update when removing last element form the list with order column`() {
        // given
        var post = createOrderColPostWith2Comments()

        // when
        transaction {
            post = entityManager.merge(post)
            post.removeComment(post.comments.last().id!!)
        }

        // then
        // 1 query to fetch entity
        db.checkQueryCount(1, OrderColPost.TABLE_NAME, joins = listOf(OrderColPostComment.TABLE_NAME))
        db.checkDeleteCount(1, OrderColPostComment.TABLE_NAME)
        db.checkUpdateCount(1, OrderColPostComment.TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and 1 insert when removing last element form the element collection list`() {
        // given
        var post = createElementColPostWith2CommentsAndTags()

        // when
        transaction {
            post = entityManager.merge(post)
            db.checkQueryCount(1, ElementColPost.TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.CHILD_TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.TAGS_TABLE_NAME)

            post.comments.remove(post.comments.last())
        }

        // then
        db.checkDeleteCount(1, ElementColPost.CHILD_TABLE_NAME)
        db.checkInsertCount(1, ElementColPost.CHILD_TABLE_NAME)
    }

    @Test
    fun `should execute single delete statement and 1 insert when removing last element form the element collection list containing primitive type`() {
        // given
        var post = createElementColPostWith2CommentsAndTags()

        // when
        transaction {
            post = entityManager.merge(post)
            // all child collections are fetched because of merge and lazy loading
            db.checkQueryCount(1, ElementColPost.TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.CHILD_TABLE_NAME)
            db.checkQueryCount(1, ElementColPost.TAGS_TABLE_NAME)

            post.tags.remove(post.tags.last())
        }

        // then
        // again this can be optimized by adding OrderColumn. Then one row will be removed and updates will happen instead of inserts
        db.checkDeleteCount(1, ElementColPost.TAGS_TABLE_NAME)
        db.checkInsertCount(1, ElementColPost.TAGS_TABLE_NAME)
    }
}