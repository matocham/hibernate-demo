package com.example.hibernatedemo.spring

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPostComment
import com.example.hibernatedemo.onetomany.version.PrimitiveVersionPost
import com.example.hibernatedemo.onetomany.version.PrimitiveVersionPostComment
import com.example.hibernatedemo.onetomany.version.VersionIdentityPost
import com.example.hibernatedemo.onetomany.version.VersionIdentityPostComment
import com.example.hibernatedemo.single.ManualIdPost
import com.example.hibernatedemo.single.ManualStringIdPost
import com.example.hibernatedemo.single.PrimitiveIdPost
import org.junit.jupiter.api.Test

class RepositorySaveTest: BaseDbTest() {
    // Spring isNew check flow:
    // 1. if version does not exist or it is a primitive type then use id to determine if entity is new or not
    // 2. if version exists and it is not primitive type then entity is new if version is null
    // 3. checks based on id:
    //  - if id is not a primitive then it is new if it has null value
    //  - if id is primitive then entity is new when id is 0

    @Test
    fun `should insert new post when saving new LazyPost entity`() {
        // given
        val post = LazyPost("test")

        // when
        transaction {
            lazyPostRepository.save(post)
        }

        // then
        db.checkInsertCount(1, LazyPost.TABLE_NAME)
    }

    @Test
    fun `should select and then insert new post when saving new ManualIdPost entity`() {
        // given
        val post = ManualIdPost(1, "test")

        // when
        transaction {
            manualIPostRepository.save(post)
        }

        // then
        // query is issued because non-primitive type is used for id and spring sees it as detached entity and invokes merge instead of persist
        db.checkQueryCount(1, ManualIdPost.TABLE_NAME)
        db.checkInsertCount(1, ManualIdPost.TABLE_NAME)
    }

    @Test
    fun `should select and then insert new post when saving new ManualStringIdPost entity`() {
        // given
        val post = ManualStringIdPost("1", "test")

        // when
        transaction {
            manualStringIPostRepository.save(post)
        }

        // then
        // query is issued because non-primitive type is used for id and spring sees it as detached entity and invokes merge instead of persist
        db.checkQueryCount(1, ManualStringIdPost.TABLE_NAME)
        db.checkInsertCount(1, ManualStringIdPost.TABLE_NAME)
    }

    @Test
    fun `should insert new post when saving new PrimitiveIdPost entity with unchanged id`() {
        // given
        val post = PrimitiveIdPost("test")

        // when
        transaction {
            primitiveIPostRepository.save(post)
        }

        // then
        db.checkInsertCount(1, PrimitiveIdPost.TABLE_NAME)

        // clean up
        transaction {
            entityManager.remove(entityManager.getReference(PrimitiveIdPost::class.java, post.id))
        }
        db.checkDeleteCount(1, PrimitiveIdPost.TABLE_NAME)
    }

    @Test
    fun `should select and then insert new post when saving new LazyPost with specified id`() {
        // given
        val post = LazyPost("test")
        post.id = 1234

        // when
        transaction {
            lazyPostRepository.save(post)
        }

        // then
        db.checkQueryCount(1, LazyPost.TABLE_NAME, joins = listOf(LazyPostComment.TABLE_NAME))
        db.checkInsertCount(1, LazyPost.TABLE_NAME)
    }

    @Test
    fun `should select and then insert new post when saving new PrimitiveIdPost entity with changed id`() {
        // given
        val post = PrimitiveIdPost("test")
        post.id = 21

        // when
        transaction {
            primitiveIPostRepository.save(post)
        }

        // then
        db.checkQueryCount(1, PrimitiveIdPost.TABLE_NAME)
        db.checkInsertCount(1, PrimitiveIdPost.TABLE_NAME)
    }

    @Test
    fun `should insert new post when saving entity with uninitialized version attribute`() {
        // given
        val post = VersionIdentityPost("test")

        // when
        transaction {
            versionIdentityRepository.save(post)
        }

        // then
        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should insert new post when saving entity with initialized version attribute`() {
        // given
        val post = VersionIdentityPost("test")
        post.version = 10

        // when
        transaction {
            versionIdentityRepository.save(post)
        }

        // then
        // no query even though merge is invoked because id is null!
        db.checkQueryCount(0, VersionIdentityPost.TABLE_NAME)
        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should query and then insert new post when saving entity with initialized version attribute and id set`() {
        // given
        val post = VersionIdentityPost("test")
        post.version = 10
        post.id = 50

        // when
        transaction {
            versionIdentityRepository.save(post)
        }

        // then
        // spring calls merge and because id is set then merge is required
        db.checkQueryCount(1, VersionIdentityPost.TABLE_NAME, joins = listOf(VersionIdentityPostComment.TABLE_NAME))
        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should insert new post when saving entity with primitive version attribute`() {
        // given
        val post = PrimitiveVersionPost("test")

        // when
        transaction {
            primitiveVersionPostRepository.save(post)
        }

        // then
        // no query as for primitive version spring falls back to id check
        db.checkInsertCount(1, PrimitiveVersionPost.TABLE_NAME)
    }

    @Test
    fun `should insert new post when saving entity with primitive version attribute that is manually set`() {
        // given
        val post = PrimitiveVersionPost("test")
        post.version = 11

        // when
        transaction {
            primitiveVersionPostRepository.save(post)
        }

        // then
        // same as above - version is not taken into account
        db.checkInsertCount(1, PrimitiveVersionPost.TABLE_NAME)
    }

    @Test
    fun `should select and then update detached entity with existing id and no version attribute when save is called`() {
        // given
        val post = lazyPostRepository.save(LazyPost("test"))
        db.checkInsertCount(1, LazyPost.TABLE_NAME)

        // when
        transaction {
            val secondPost = LazyPost("test 2")
            secondPost.id = post.id
            lazyPostRepository.save(secondPost)
        }

        // then
        db.checkQueryCount(1, LazyPost.TABLE_NAME, joins = listOf(LazyPostComment.TABLE_NAME))
        db.checkUpdateCount(1, LazyPost.TABLE_NAME)
    }

    @Test
    fun `should select and then update detached ManualIdPost entity with existing id when save is called`() {
        // given
        manualIPostRepository.save(ManualIdPost(77 ,"test"))
        // first save is also handled by merge and requires select
        db.checkQueryCount(1, ManualIdPost.TABLE_NAME)
        db.checkInsertCount(1, ManualIdPost.TABLE_NAME)

        // when
        transaction {
            val secondPost = ManualIdPost(77, "test 2")
            manualIPostRepository.save(secondPost)
        }

        // then
        db.checkQueryCount(1, ManualIdPost.TABLE_NAME)
        db.checkUpdateCount(1, ManualIdPost.TABLE_NAME)
    }

    @Test
    fun `should select and then update detached entity with existing id and primitive version attribute when save is called`() {
        // given
        val post = primitiveVersionPostRepository.save(PrimitiveVersionPost("test"))
        db.checkInsertCount(1, PrimitiveVersionPost.TABLE_NAME)

        // when
        transaction {
            val secondPost = PrimitiveVersionPost("test 2")
            secondPost.id = post.id
            secondPost.version = post.version
            primitiveVersionPostRepository.save(secondPost)
        }

        // then
        db.checkQueryCount(1, PrimitiveVersionPost.TABLE_NAME, joins = listOf(PrimitiveVersionPostComment.TABLE_NAME))
        db.checkUpdateCount(1, PrimitiveVersionPost.TABLE_NAME)
    }

    @Test
    fun `should select and then update detached entity with existing id and version attribute set to the same value as in persisted entity when save is called`() {
        // given
        val post = versionIdentityRepository.save(VersionIdentityPost("test"))
        db.checkInsertCount(1, VersionIdentityPost.TABLE_NAME)

        // when
        transaction {
            val secondPost = VersionIdentityPost("test 2")
            secondPost.id = post.id
            secondPost.version = post.version
            versionIdentityRepository.save(secondPost)
        }

        // then
        db.checkQueryCount(1, VersionIdentityPost.TABLE_NAME, joins = listOf(VersionIdentityPostComment.TABLE_NAME))
        db.checkUpdateCount(1, VersionIdentityPost.TABLE_NAME)
    }

    @Test
    fun `should handle invoking save twice for the entity with the same id that was automatically generated in the same transaction`() {
        transaction {
            // given
            val post = lazyPostRepository.save(LazyPost("test"))
            db.checkInsertCount(1, LazyPost.TABLE_NAME)

            // when
            val secondPost = LazyPost("change")
            secondPost.id = post.id
            lazyPostRepository.save(secondPost)
        }
        // then
        db.checkUpdateCount(1, LazyPost.TABLE_NAME)
    }

    @Test
    fun `should handle invoking save twice for the entity with the same id that was manually generated in the same transaction`() {
        transaction {
            // given
            manualIPostRepository.save(ManualIdPost(7,"test"))
            db.checkQueryCount(1, ManualIdPost.TABLE_NAME)

            // when
            val secondPost = ManualIdPost(7, "change")
            manualIPostRepository.save(secondPost)
        }
        // then
        // insert is issued at the end of the transaction
        db.checkInsertCount(1, ManualIdPost.TABLE_NAME)
        db.checkUpdateCount(1, ManualIdPost.TABLE_NAME)
    }
}