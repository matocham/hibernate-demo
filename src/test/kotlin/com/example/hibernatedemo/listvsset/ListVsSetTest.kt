package com.example.hibernatedemo.listvsset

import com.example.hibernatedemo.BaseDbTest
import org.junit.jupiter.api.Test

class ListVsSetTest: BaseDbTest() {
    @Test
    fun `test set vs list`() {
        // test how adding and removing single element affects queries in 1:N and N:N setup and n:n with manual join table?
    }

    @Test
    fun `test the same for child to parent one direction many to one`() {
        // test list vs set when only parent side is configured as onetomany
    }

    @Test
    fun `test the same with element collection`() {
        // set list and set when using element collection
    }

    @Test
    fun `test many to many relationship with join table and lists`() {

    }

    @Test
    fun `test many to many relationship with join table using sets`() {
        // refer to book on how it works
    }

    @Test
    fun `test how spring data handles save with specified id`() {
        // two scenarios
        // 1. save in one transaction, then save detached entity in another transaction using the same id - here additional select is expected form spring data
        // 2. save in one transaction, then in the same try updating detached entity with the same id - just create new entity with the same id
        // this replicates situation when entity is mapped to dto and then dto is stored
    }

    @Test
    fun `test concurrency and isolation levels`() {
        // test different phenomena and how they are prevented by isolation levels and optimistic locking
        // test pessimistic locking
        // show how hibernate waits with actual query execution to the end of the transaction
    }
    // test how merge will work when some children are changed, parent is changed, some untracked children are added and then saved
    // test how merge works with non-existing entity with and without version column using primitive or wrapper id types https://vladmihalcea.com/jpa-persi st-and-merge/
    // also test when id is manually assigned and describe differences
    // test version as primitive and wrapper in context of merge call on entity with manually set id that does not exist in db



// 1. if version does not exist or it is a primitive type then use id to determine if entity is new or not
// 2. if version exists and it is not primitive type then entity is new if version is null
// 3. checks based on id:
//  - if id is not a primitive then it is new if it has null value
//  - if id is primitive then entity is new when id is 0


// if spring thinks that entity is new it is going to issue insert.
// Else it will will go with the merge that can also create new entity in some cases but it is going to issue additional select statement
// merge behaviour depends on generator type used - for identity and sequence it will store the entity with additional select
// TODO it means that expected behaviour for unmanaged entities is a select followed by update if something changed
// TODO it means that as long as we are in the same session update based on fresh entity object should not issue additional query?
}