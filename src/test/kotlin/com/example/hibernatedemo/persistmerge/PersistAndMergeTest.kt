package com.example.hibernatedemo.persistmerge

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.builders.Builders.createLazyPostWith2Comments
import com.example.hibernatedemo.builders.Builders.createVerIdentityPostWith2Comments
import com.example.hibernatedemo.builders.Builders.getSeqIdentityPostWith2Comments
import com.example.hibernatedemo.builders.Builders.getVerIdentityPostWith2Comments
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import com.example.hibernatedemo.onetomany.sequenceid.SequencePost
import com.example.hibernatedemo.onetomany.sequenceid.SequencePostComment
import com.example.hibernatedemo.onetomany.version.VersionIdentityPost
import com.example.hibernatedemo.onetomany.version.VersionIdentityPostComment
import com.example.hibernatedemo.onetomany.version.VersionSeqPost
import com.example.hibernatedemo.onetomany.version.VersionSeqPostComment
import com.example.hibernatedemo.single.PrimitiveIdPost
import org.junit.jupiter.api.Test

class PersistAndMergeTest : BaseDbTest() {
    @Test
    fun `should execute 1 insert for post and 2 inserts for post comments when using sequence id generation`() {
        transaction {
            val post = SequencePost("post 1")
            post.addComment(SequencePostComment("comment 1"))
            post.addComment(SequencePostComment("comment 2"))

            entityManager.persist(post)
            // immediately get sequence values
            db.checkNextValCount(1, SequencePost.SEQUENCE_NAME)
            // only one sequence call because of pooled sequence algorithm
            db.checkNextValCount(1, SequencePostComment.SEQUENCE_NAME)
            db.checkInsertCount(0, SequencePost.TABLE_NAME)
            db.checkInsertCount(0, SequencePostComment.TABLE_NAME)
        }

        // entities are persisted only after transaction
        db.checkInsertCount(1, SequencePost.TABLE_NAME)
        db.checkInsertCount(2, SequencePostComment.TABLE_NAME)
    }

    @Test
    fun `should immediately save post and 2 comments when using entities with identity generation strategy`() {
        transaction {
            val post = LazyPost("post 1")
            post.addComment(LazyPostComment("comment 1"))
            post.addComment(LazyPostComment("comment 2"))

            entityManager.persist(post)
            // entities are saved straight away
            db.checkInsertCount(1, LazyPost.TABLE_NAME)
            db.checkInsertCount(2, LazyPostComment.TABLE_NAME)
        }
    }

    @Test
    fun `should execute 1 insert for post and 2 inserts for post comments when using sequence id generation and version attribute`() {
        transaction {
            val post = getSeqIdentityPostWith2Comments()

            entityManager.persist(post)
            // immediately get sequence values
            db.checkNextValCount(1, VersionSeqPost.SEQUENCE_NAME)
            db.checkNextValCount(1, VersionSeqPostComment.SEQUENCE_NAME)
            db.checkInsertCount(0, VersionSeqPost.TABLE_NAME)
            db.checkInsertCount(0, VersionSeqPostComment.TABLE_NAME)
        }

        // entities are persisted only after transaction
        db.checkInsertCount(1, VersionSeqPost.TABLE_NAME)
        db.checkInsertCount(2, VersionSeqPostComment.TABLE_NAME)
    }

    @Test
    fun `should immediately save post and 2 comments when using entities with identity generation strategy and version attribute`() {
        transaction {
            val post = getVerIdentityPostWith2Comments()

            entityManager.persist(post)
            // entities are saved straight away
            db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
            db.checkInsertCount(2, VersionIdentityPostComment.TABLE_NAME)
        }
    }

    @Test
    fun `should execute 1 insert statement when saving post with identity id generation when using merge function`() {
        // given
        val post = LazyPost("post 1")

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkInsertCount(1, LazyPost.TABLE_NAME)
    }

    @Test
    fun `should execute 1 insert statement when saving post with sequence id generation when using merge function`() {
        // given
        val post = SequencePost("post 1")

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkNextValCount(1, SequencePost.SEQUENCE_NAME)
        db.checkInsertCount(1, SequencePost.TABLE_NAME)
    }

    @Test
    fun `should execute 1 insert statement when saving post with identity id generation and null version attribute when using merge function`() {
        // given
        val post = VersionIdentityPost("post 1")

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should execute 1 insert statement when saving post with sequence id generation and null version attribute when using merge function`() {
        // given
        val post = VersionSeqPost("post 1")

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkNextValCount(1, VersionSeqPost.SEQUENCE_NAME)
        db.checkInsertCount(1, VersionSeqPost.TABLE_NAME)
    }

    @Test
    fun `should execute 1 insert statement when saving post with identity id generation and non-null version attribute when using merge function`() {
        // given
        val post = VersionIdentityPost("post 1")
        post.version = 1

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should execute 1 insert statement when saving post with sequence id generation and non-null version attribute when using merge function`() {
        // given
        val post = VersionSeqPost("post 1")
        post.version = 1

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkNextValCount(1, VersionSeqPost.SEQUENCE_NAME)
        db.checkInsertCount(1, VersionSeqPost.TABLE_NAME)
    }

    @Test
    fun `should execute 1 select and 1 insert statement when saving post with identity id generation, non-null id and non-null version attribute when using merge function`() {
        // given
        val post = VersionIdentityPost("post 1")
        post.version = 1
        post.id = 100

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkQueryCount(1, VersionIdentityPost.TABLE_NAME, joins = listOf(VersionIdentityPostComment.TABLE_NAME))
        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should execute 1 insert statement when saving post with sequence id generation, non-null id and non-null version attribute when using merge function`() {
        // given
        val post = VersionSeqPost("post 1")
        post.version = 1
        post.id = 100

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkNextValCount(1, VersionSeqPost.SEQUENCE_NAME)
        db.checkQueryCount(1, VersionSeqPost.TABLE_NAME, joins = listOf(VersionSeqPostComment.TABLE_NAME))
        db.checkInsertCount(1, VersionSeqPost.TABLE_NAME)
    }

    @Test
    fun `should execute select and update when new entity with identity generation strategy with existing id is merged in new transaction when using the same entity manager`() {
        // given
        val post = createLazyPostWith2Comments()

        // when
        transaction {
            val secondPost = LazyPost("new title")
            secondPost.id = post.id
            entityManager.merge(secondPost)
        }

        db.checkQueryCount(1, LazyPost.TABLE_NAME, joins = listOf(LazyPostComment.TABLE_NAME))
        db.checkUpdateCount(1, LazyPost.TABLE_NAME)
    }

    @Test
    fun `should execute select and update when new entity with sequence generation strategy with existing id is merged in new transaction when using the same entity manager`() {
        // given
        // manually created to avoid child entities. Otherwise orphan removal kicks in and deletes children
        val post = transaction {
            val innerPost = SequencePost("title")
            entityManager.persist(innerPost)
            innerPost
        }!!

        db.checkNextValCount(1, SequencePost.SEQUENCE_NAME)
        db.checkInsertCount(1, SequencePost.TABLE_NAME)

        // when
        transaction {
            val secondPost = SequencePost("new title")
            secondPost.id = post.id
            entityManager.merge(secondPost)
        }

        db.checkQueryCount(1, SequencePost.TABLE_NAME, joins = listOf(SequencePostComment.TABLE_NAME))
        db.checkUpdateCount(1, SequencePost.TABLE_NAME)
    }

    @Test
    fun `should execute select and update when new entity with identity generation strategy and version attribute with existing id is merged in new transaction when using the same entity manager`() {
        // given
        val post = transaction {
            val innerPost = VersionIdentityPost("title")
            entityManager.persist(innerPost)
            innerPost
        }!!

        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)

        // when
        transaction {
            val secondPost = VersionIdentityPost("new title")
            secondPost.id = post.id
            // when version is not initialized PropertyValueException will be thrown ( combination of existing id and missing version is not allowed?)
            // setting it to other value will cause OptimisticLockException because of version mismatch
            secondPost.version = post.version
            entityManager.merge(secondPost)
        }

        db.checkQueryCount(1, VersionIdentityPost.TABLE_NAME, joins = listOf(VersionIdentityPostComment.TABLE_NAME))
        db.checkUpdateCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should execute select and update when new entity with sequence generation strategy and version attribute with existing id is merged in new transaction when using the same entity manager`() {
        // given
        val post = transaction {
            val innerPost = VersionSeqPost("title")
            entityManager.persist(innerPost)
            innerPost
        }!!

        db.checkNextValCount(1, VersionSeqPost.SEQUENCE_NAME)
        db.checkInsertCount(1, VersionSeqPost.TABLE_NAME)

        // when
        transaction {
            val secondPost = VersionSeqPost("new title")
            secondPost.id = post.id
            // the same as above. Version has to match
            secondPost.version = post.version
            entityManager.merge(secondPost)
        }

        db.checkQueryCount(1, VersionSeqPost.TABLE_NAME, joins = listOf(VersionSeqPostComment.TABLE_NAME))
        db.checkUpdateCount(1, VersionSeqPost.TABLE_NAME)
    }

    @Test
    fun `should execute select and update when new entity with identity generation strategy and version attribute with existing id is merged in new transaction when using different entity manager`() {
        // given
        val post = transaction {
            val innerPost = VersionIdentityPost("title")
            entityManager.persist(innerPost)
            innerPost
        }!!

        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)

        // when
        val secondEntityManager = entityManagerFactory.createEntityManager()
        secondEntityManager.transaction.begin()
        val secondPost = VersionIdentityPost("new title")
        secondPost.id = post.id
        // when version is not initialized PropertyValueException will be thrown
        // setting it to other value will cause OptimisticLockException because of version mismatch
        secondPost.version = post.version
        secondEntityManager.merge(secondPost)
        secondEntityManager.transaction.commit()
        secondEntityManager.close()

        db.checkQueryCount(1, VersionIdentityPost.TABLE_NAME, joins = listOf(VersionIdentityPostComment.TABLE_NAME))
        db.checkUpdateCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should execute select and update when new entity with sequence generation strategy and version attribute with existing id is merged in new transaction when using different entity manager`() {
        // given
        val post = transaction {
            val innerPost = VersionSeqPost("title")
            entityManager.persist(innerPost)
            innerPost
        }!!

        db.checkNextValCount(1, VersionSeqPost.SEQUENCE_NAME)
        db.checkInsertCount(1, VersionSeqPost.TABLE_NAME)

        // when
        val secondEntityManager = entityManagerFactory.createEntityManager()
        secondEntityManager.transaction.begin()
        val secondPost = VersionSeqPost("new title")
        secondPost.id = post.id
        // the same as above. Version has to match
        secondPost.version = post.version
        secondEntityManager.merge(secondPost)
        secondEntityManager.transaction.commit()
        secondEntityManager.close()

        db.checkQueryCount(1, VersionSeqPost.TABLE_NAME, joins = listOf(VersionSeqPostComment.TABLE_NAME))
        db.checkUpdateCount(1, VersionSeqPost.TABLE_NAME)
    }

    @Test
    fun `should hande situation when second post with the same id is merged in the same transaction when identity generation strategy is used`() {
        // given
        transaction {
            val firstPost = VersionIdentityPost("title")
            entityManager.persist(firstPost)

            db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)

            val secondPost = VersionIdentityPost("title 2")
            secondPost.id = firstPost.id
            // because identity requires actual insert to get the id also version is set and needs to be moved over to avoid optimistic lock exception
            secondPost.version = firstPost.version
            entityManager.merge(secondPost)
        }

        // second merge causes update.
        // if 2 managed entity with the same id have different changes, then they are merged at the end of the transaction
        db.checkUpdateCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should hande situation when second post with the same id is merged in the same transaction when sequence generation strategy is used`() {
        // given
        transaction {
            val firstPost = VersionSeqPost("title")
            entityManager.persist(firstPost)

            db.checkNextValCount(1, VersionSeqPost.SEQUENCE_NAME)

            val secondPost = VersionSeqPost("title 2")
            secondPost.id = firstPost.id
            entityManager.merge(secondPost)
        }

        db.checkInsertCount(1, VersionSeqPost.TABLE_NAME)
        // second merge causes update.
        // if 2 managed entity with the same id have different changes, then they are merged at the end of the transaction
        db.checkUpdateCount(1, VersionSeqPost.TABLE_NAME)
    }

    @Test
    fun `merge of detached entity with new child should result in new insert`() {
        // given
        val post = createVerIdentityPostWith2Comments()

        // when
        val secondPost = VersionIdentityPost("changed")
        secondPost.addComment(post.comments.first())
        secondPost.addComment(VersionIdentityPostComment("a new one"))
        secondPost.id = post.id
        secondPost.version = post.version

        transaction {
            entityManager.merge(secondPost)
        }

        // then
        db.checkQueryCount(1, VersionIdentityPost.TABLE_NAME, joins = listOf(VersionIdentityPostComment.TABLE_NAME))
        // no delete because of no orphan removal on parent - second post was not added to new entity but it was not removed
        db.checkDeleteCount(0, VersionIdentityPostComment.TABLE_NAME)
        db.checkInsertCount(1, VersionIdentityPostComment.TABLE_NAME)
        db.checkUpdateCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should always query for existing entities when merging entity with primitive id`() {
        // given
        val post = PrimitiveIdPost("test")

        // when
        transaction {
            entityManager.merge(post)
        }

        // then
        db.checkQueryCount(1, PrimitiveIdPost.TABLE_NAME)
        db.checkInsertCount(1, PrimitiveIdPost.TABLE_NAME)

        // clean up
        transaction {
            entityManager.remove(entityManager.getReference(PrimitiveIdPost::class.java, post.id))
        }
        db.checkDeleteCount(1, PrimitiveIdPost.TABLE_NAME)
    }
}