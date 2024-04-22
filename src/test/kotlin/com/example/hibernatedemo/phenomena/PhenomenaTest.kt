package com.example.hibernatedemo.phenomena

import com.example.hibernatedemo.BaseDbTest
import com.example.hibernatedemo.base.BaseEntity
import com.example.hibernatedemo.onetomany.fetchtypes.LazyPost
import com.example.hibernatedemo.onetomany.version.VersionIdentityPost
import jakarta.persistence.*
import org.amshove.kluent.*
import org.hibernate.engine.spi.SessionImplementor
import org.hibernate.exception.LockAcquisitionException
import org.junit.jupiter.api.*
import java.lang.RuntimeException
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread
import kotlin.reflect.KProperty
import kotlin.system.measureTimeMillis

// on DB level two updates with criteria that select overlapping rows will lock - second update will wait for the first one to commit and will re-evaluate. Here is nice description: https://www.postgresql.org/docs/current/transaction-iso.html
class PhenomenaTest : BaseDbTest() {
    // dirty write - not possible in databases - it would mean that one transaction would issue a rollback that reverts changes made by other transaction -> no isolation between transactions
    // dirty read - not possible - it means that one transaction can read uncommitted changed from the other transaction
    // non-repeatable read - possible in read committed. may be hard to test as hibernate caches selected values. one transaction selects a row, second updates it and then second select in first transaction sees changed values
    // phantom read - 2 reads using the same criteria returns different results. Repeatable read may handle it (postgres does) but depends on engine implementation
    // read skew - requires 2 tables. Read one table. Second transaction updates dependant row in both initial and second table and commits.
    //      First transaction reads updated row from second table and sees changes. View is inconsistent - one table is from before update in second transaction and second is after update
    // write skew - similar as above - one transaction modifies post and second post_comment - at the end contract is broken eg. updated_by field in one table doesn't match the author of changes in second table (more about making sure that app makes those changes atomic. On db level correct isolation level or shared lock are needed
    // lost update - happens when row is selected without lock in one transaction and then second transaction reads and modifies the same row and commits the changes. Then first transaction makes its changes and updates the row - update form second transaction is lost ( optimistic locking, shared locks or correct isolation level)

    // shared lock can be used to allow concurrent reads with the same lock level while not allowing update lock.
    // Writes from concurrent transactions are blocked so if 2 transactions acquired shared lock and tried update then one will have to be rolled back
    // shared lock should be used only to read the data while making sure nobody will modify it concurrently

    // postgres allows to read the row with no lock when shared/update lock is in place - mysql behaves differently

    // unique constraints will also block concurrent transactions unit other one commits or rolls back. It is similar like updates. Stronger isolation levels will rise serialization error like for updates

    val uncaughtExceptions: ConcurrentHashMap<String, Throwable> = ConcurrentHashMap()

    @BeforeEach
    fun setup() {
        uncaughtExceptions.clear()
    }

    @Test
    fun `should throw optimistic locking exception during commit when no flushing is used`() {
        concurrentTransactionsTest<VersionIdentityPost>(
            firstTransaction = prefetched { p1, _ ->
                p1.title = "t1"
            },
            secondTransaction = prefetched { p2, em ->
                p2.title = "t2"

                Thread.sleep(100)
                val ex = assertThrows<RollbackException> {
                    em.transaction.commit()
                }
                ex.cause shouldBeInstanceOf OptimisticLockException::class.java
            },
            dbAssertions = {
                //additional query from thread with optimistic locking exception
                db.checkQueryCount(3, VersionIdentityPost.TABLE_NAME)
                db.checkUpdateCount(2, VersionIdentityPost.TABLE_NAME)
            },
            expectedTitle = "t1"
        )
    }

    @Test
    fun `should throw optimistic locking exception during flush when changes are manually flushed`() {
        concurrentTransactionsTest<VersionIdentityPost>(
            firstTransaction = prefetched { p1, em ->
                p1.title = "t1"
                // flush changes right away
                em.flush()
                // wait 500ms for commit
                Thread.sleep(500)
                em.transaction.commit()
            },
            secondTransaction = prefetched { p2, em ->
                p2.title = "t2"
                // wait for the first thread to catch up because of sleep in concurrentTransactionsTest method
                Thread.sleep(200)
                val time = measureTimeMillis {
                    // because first transaction committed this will throw exception
                    assertThrows<OptimisticLockException> {
                        // flush the changes. Should wait for first transaction to commit or rollback
                        em.flush()
                    }
                }
                // transaction is active as we handled exception
                em.transaction.isActive shouldBeEqualTo true
                // rollback manually
                em.transaction.rollback()
                // flush was hanging for over 350ms waiting for first transaction commit
                // 500ms + 150ms - 100ms - 200ms = 350ms because of internal waits that make sure transactions overlap
                // check is a little bit lower because of sleep imperfections
                time shouldBeGreaterThan 330
            },
            dbAssertions = {
                db.checkQueryCount(2, VersionIdentityPost.TABLE_NAME)
                db.checkUpdateCount(2, VersionIdentityPost.TABLE_NAME)
            },
            expectedTitle = "t1"
        )
    }

    @Nested
    inner class ReadCommitted {
        // lost update
        @Test
        fun `should be able to update the same entity without version attribute in 2 concurrent transactions`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                },
                secondTransaction = prefetched { p2, _ ->
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"

                    Thread.sleep(100)
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t2"
            )
        }

        // lost update - optimistic locking to the rescue
        @Test
        fun `should not be able to update the same entity with version attribute in 2 concurrent transactions`() {
            concurrentTransactionsTest<VersionIdentityPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                },
                secondTransaction = prefetched { p2, em ->
                    val ex = assertThrows<RollbackException> {
                        p2.title shouldBeEqualTo "test"
                        p2.title = "t2"

                        Thread.sleep(100)
                        em.transaction.commit()
                    }
                    ex.cause shouldBeInstanceOf OptimisticLockException::class.java
                },
                dbAssertions = {
                    //additional query from thread with optimistic locking exception
                    db.checkQueryCount(3, VersionIdentityPost.TABLE_NAME)
                    db.checkUpdateCount(2, VersionIdentityPost.TABLE_NAME)
                },
                expectedTitle = "t1"
            )
        }

        // shared lock prevents from acquiring update lock - should also block updates in other transactions. Other transactions can acquire shared lock but cannot perform updates
        @Test
        fun `should NOT be able to update the same entity without version attribute in 2 concurrent transactions when shared locking is used`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, em ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"

                    val ex = assertThrows<RollbackException> {
                        em.transaction.commit()
                    }

                    // rolled back because of deadlock - 2 transactions wanted to update the same row using shared lock - one had to be rolled back
                    ex.cause?.cause shouldBeInstanceOf LockAcquisitionException::class
                },
                secondTransaction = prefetched { p2, em ->
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"
                    Thread.sleep(100)

                    em.transaction.commit()
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t2",
                lockModeType = LockModeType.PESSIMISTIC_READ
            )
        }

        // here lost update is still possible because transaction with no lock will wait for locked to finish
        @Test
        fun `should be able to update the same entity without version attribute in 2 concurrent transactions when shared locking is used in only one transaction`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, em ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                    // even though commit is executed right away it will block and wait for second transaction with share lock to finish
                    em.transaction.commit()
                },
                secondTransaction = prefetched { p2, _ ->
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"
                    Thread.sleep(100)
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t1",
                lockModeType = LockModeType.NONE,
                secondTransactionLockType = LockModeType.PESSIMISTIC_READ
            )
        }

        // lost update - pessimistic locking to the rescue
        // second select waits for first transaction commit and sees changes made by it
        @Test
        fun `should be able to update the same entity without version attribute in 2 concurrent transactions without loosing data when pessimistic locking is used`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                },
                secondTransaction = prefetched { p2, _ ->
                    // second transaction waits for lock release
                    p2.title shouldBeEqualTo "t1"
                    p2.title = "t2"
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t2",
                lockModeType = LockModeType.PESSIMISTIC_WRITE
            )
        }

        // postgres allows to read data when query does not use any locking even when FOR UPDATE lock is placed on given row
        @Test
        fun `should be able to query and modify the same row concurrently when only one transaction uses pessimistic locking`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"

                    // wait a bit so that it is visible that second transactions waits
                    Thread.sleep(100)
                },
                secondTransaction = prefetched { p2, em ->
                    // still sees old value because postgres allows to query lock row
                    p2.title shouldBeEqualTo "test"
                    val time = measureTimeMillis {
                        p2.title = "t2"
                        em.transaction.commit()
                    }
                    // transaction waits for the first one and lock release but it was able to read the value
                    time shouldBeGreaterThan 90
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t2",
                lockModeType = LockModeType.PESSIMISTIC_WRITE,
                secondTransactionLockType = LockModeType.NONE
            )
        }

        // non-repeatable read - hibernate provides repeatable reads by caching entities in persistence context so it has to be cleared
        @Test
        fun `transaction should be able to read data committed by concurrent second transaction`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"

                    Thread.sleep(200)
                },
                secondTransaction = prefetched { p2, em ->
                    // second transaction waits for lock release
                    p2.title shouldBeEqualTo "test"

                    Thread.sleep(500)
                    em.clear()
                    val p2AfterCommit = em.getEntityByQuery<LazyPost>(p2.id)
                    p2AfterCommit.title shouldBeEqualTo "t1"
                },
                dbAssertions = {
                    db.checkQueryCount(3, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(1, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t1"
            )
        }

        // phantom read
        @Test
        fun `querying 2 times for the list of posts with the same condition should return different number of rows after second transaction commits new matching post`() {
            val (_, secondPost) = transaction {
                val lazyPost = LazyPost("phantomRead")
                val secondLazyPost = LazyPost("other")
                entityManager.persist(lazyPost)
                entityManager.persist(secondLazyPost)
                listOf(lazyPost, secondLazyPost)
            }!!
            db.checkInsertCount(2, LazyPost.TABLE_NAME)

            val t1 = thread {
                entityManagerFactory.createEntityManager().use { em ->
                    try {
                        em.transaction.begin()
                        val innerPost = em.getEntityByQuery<LazyPost>(secondPost.id)
                        innerPost.title shouldBeEqualTo "other"
                        // modify the value
                        innerPost.title = "phantomRead 2"
                        // wait a bit and commit
                        Thread.sleep(200)
                        em.transaction.commit()
                    } catch (e: Throwable) {
                        uncaughtExceptions[Thread.currentThread().name] = e
                    }
                }
            }

            val t2 = thread {
                entityManagerFactory.createEntityManager().use { em ->
                    try {
                        em.transaction.begin()
                        var posts = em.createQuery(
                            "select p from LazyPost p where p.title like 'phantomRead%'",
                            LazyPost::class.java
                        ).resultList
                        posts shouldHaveSize 1
                        // wait a bit and check db once more
                        Thread.sleep(500)
                        em.clear()
                        posts = em.createQuery(
                            "select p from LazyPost p where p.title like 'phantomRead%'",
                            LazyPost::class.java
                        ).resultList
                        posts shouldHaveSize 2
                        em.transaction.commit()
                    } catch (e: Throwable) {
                        uncaughtExceptions[Thread.currentThread().name] = e
                    }
                }
            }

            t1.join()
            t2.join()

            if (uncaughtExceptions.isNotEmpty()) {
                fail(
                    RuntimeException(
                        "Expected no exceptions in threads but got ${uncaughtExceptions.size} in ${uncaughtExceptions.keys.joinToString()} threads",
                        uncaughtExceptions.values.first()
                    )
                )
            }

            db.checkQueryCount(3, LazyPost.TABLE_NAME)
            db.checkUpdateCount(1, LazyPost.TABLE_NAME)
        }
    }

    @Nested
    inner class RepeatableRead {
        // lost update - second transaction should detect changes and throw serialization error
        @Test
        fun `should not be able to update the same entity without version attribute in 2 concurrent transactions`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                },
                secondTransaction = prefetched { p2, em ->
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"

                    // wait a little bit for the first transaction to commit
                    Thread.sleep(200)
                    val ex = assertThrows<RollbackException> {
                        em.transaction.commit()
                    }
                    // fails because of serialization error
                    ex.cause?.cause?.cause?.message!! shouldBeEqualTo "ERROR: could not serialize access due to concurrent update"
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t1",
                // only second transaction has elevated isolation level. It is enough to throw error in case of concurrent update
                secondTransactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            )
        }

        // lost update - optimistic locking is not needed as serialization error will be thrown either way
        @Test
        fun `should not be able to update the same entity with version attribute in 2 concurrent transactions`() {
            concurrentTransactionsTest<VersionIdentityPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                },
                secondTransaction = prefetched { p2, em ->

                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"

                    Thread.sleep(200)
                    val ex = assertThrows<RollbackException> {
                        em.transaction.commit()
                    }
                    ex.cause?.cause?.cause?.message!! shouldBeEqualTo "ERROR: could not serialize access due to concurrent update"
                },
                dbAssertions = {
                    db.checkQueryCount(2, VersionIdentityPost.TABLE_NAME)
                    db.checkUpdateCount(2, VersionIdentityPost.TABLE_NAME)
                },
                expectedTitle = "t1",
                secondTransactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            )
        }

        // shared lock causes deadlock and first transaction is rolled back. That's why second can complete even though it had stronger isolation and would be rolled back instead
        @Test
        fun `should NOT be able to update the same entity without version attribute in 2 concurrent transactions when shared locking is used`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, em ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"

                    val ex = assertThrows<RollbackException> {
                        em.transaction.commit()
                    }

                    // rolled back because of deadlock - 2 transactions wanted to update the same row using shared lock - one had to be rolled back
                    ex.cause?.cause shouldBeInstanceOf LockAcquisitionException::class
                },
                secondTransaction = prefetched { p2, em ->
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"
                    Thread.sleep(200)

                    // no serialization error because first transaction was rolled back
                    em.transaction.commit()
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t2",
                lockModeType = LockModeType.PESSIMISTIC_READ,
                secondTransactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            )
        }

        @Test
        fun `should NOT be able to update the same entity without version attribute in 2 concurrent transactions when shared locking is used in only one transaction`() {
            // here the result depends on lock and isolation level combinations. If lock and stronger isolation level are applied in second transaction then it will execute successfully
            // because first transaction will either way wait on the lock.
            // that's why current test locks entity in second transaction and elevates isolation level in the first one
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, em ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                    val ex = assertThrows<RollbackException> {
                        // update flushed at this moment has to wait for the second transaction to complete as it has lock.
                        // after concurrent commit DB detects concurrent update and rolls back this transaction
                        em.transaction.commit()
                    }
                    ex.cause?.cause?.cause?.message!! shouldBeEqualTo "ERROR: could not serialize access due to concurrent update"
                },
                secondTransaction = prefetched { p2, _ ->
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"
                    Thread.sleep(100)
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t2",
                lockModeType = LockModeType.NONE,
                secondTransactionLockType = LockModeType.PESSIMISTIC_READ,
                transactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ,
                secondTransactionIsolationLevel = Connection.TRANSACTION_READ_COMMITTED
            )
        }

        // lost update - pessimistic locking will cause exception to be thrown earlier as second transaction was stuck on select waiting for the first transaction to release its lock
        @Test
        fun `should not be able to update the same entity without version attribute in 2 concurrent transactions when pessimistic locking is used`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                },
                secondTransaction = standalone { p2, em ->
                    val ex = assertThrows<OptimisticLockException> {
                        // LockModeType has to be manually set in this case
                        em.getEntityByQuery<LazyPost>(p2.id, LockModeType.PESSIMISTIC_WRITE)
                    }
                    ex.cause?.cause?.message!! shouldBeEqualTo "ERROR: could not serialize access due to concurrent update"
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(1, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t1",
                lockModeType = LockModeType.PESSIMISTIC_WRITE,
                secondTransactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            )
        }

        @Test
        fun `should NOT be able to query and modify the same row concurrently when only one transaction uses pessimistic locking`() {
            // here it also depends on lock/isolation level combination on both transaction. As one transaction uses lock and other
            // stronger isolation level it has to fail.
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"
                },
                secondTransaction = prefetched { p2, em ->
                    // still sees old value because postgres allows to query lock row
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"
                    val ex = assertThrows<OptimisticLockException> {
                        em.flush()
                    }
                    ex.cause?.cause?.message!! shouldBeEqualTo "ERROR: could not serialize access due to concurrent update"
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t1",
                lockModeType = LockModeType.PESSIMISTIC_WRITE,
                secondTransactionLockType = LockModeType.NONE,
                secondTransactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            )
        }

        @Test
        fun `should be able to query and modify the same row concurrently when only one transaction uses pessimistic locking and repeatable read isolation level`() {
            // as only one transaction uses both lock and repeatable read it has preference with the update and can commit first
            // then second transaction executes its update as it had no locks or elevated isolation levels
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    // this transaction can read the row but cannot update it because of lock hold by second transaction
                    p1.title shouldBeEqualTo "test"
                    // this update happens after second transaction completes
                    p1.title = "t1"
                },
                secondTransaction = prefetched { p2, _ ->
                    // this transaction locks the row and also uses stronger isolation level
                    p2.title shouldBeEqualTo "test"
                    p2.title = "t2"
                },
                dbAssertions = {
                    db.checkQueryCount(2, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(2, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t1",
                lockModeType = LockModeType.NONE,
                secondTransactionLockType = LockModeType.PESSIMISTIC_WRITE,
                transactionIsolationLevel = Connection.TRANSACTION_READ_COMMITTED,
                secondTransactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            )
        }

        // non-repeatable read - second transaction sees the same value after first commits
        // update would cause serialization error, but it was already demonstrated above in concurrent updates
        @Test
        fun `transaction should NOT see data committed by concurrently committed transaction`() {
            concurrentTransactionsTest<LazyPost>(
                firstTransaction = prefetched { p1, _ ->
                    p1.title shouldBeEqualTo "test"
                    p1.title = "t1"

                    Thread.sleep(200)
                },
                secondTransaction = prefetched { p2, em ->
                    // second transaction waits for lock release
                    p2.title shouldBeEqualTo "test"

                    Thread.sleep(500)
                    em.clear()
                    val p2AfterCommit = em.getEntityByQuery<LazyPost>(p2.id)
                    p2AfterCommit.title shouldBeEqualTo "test"
                },
                dbAssertions = {
                    db.checkQueryCount(3, LazyPost.TABLE_NAME)
                    db.checkUpdateCount(1, LazyPost.TABLE_NAME)
                },
                expectedTitle = "t1",
                secondTransactionIsolationLevel = Connection.TRANSACTION_REPEATABLE_READ
            )
        }

        // phantom read
        @Test
        fun `querying 2 times for the list of posts with the same condition should return the same number of rows after second transaction commits new matching post`() {
            val (_, secondPost) = transaction {
                val lazyPost = LazyPost("repeatablePhantomRead")
                val secondLazyPost = LazyPost("other")
                entityManager.persist(lazyPost)
                entityManager.persist(secondLazyPost)
                listOf(lazyPost, secondLazyPost)
            }!!
            db.checkInsertCount(2, LazyPost.TABLE_NAME)

            val t1 = thread {
                entityManagerFactory.createEntityManager().use { em ->
                    try {
                        em.transaction.begin()
                        val innerPost = em.getEntityByQuery<LazyPost>(secondPost.id)
                        innerPost.title shouldBeEqualTo "other"
                        // modify the value
                        innerPost.title = "repeatablePhantomRead 2"
                        // wait a bit and commit
                        Thread.sleep(200)
                        em.transaction.commit()
                    } catch (e: Throwable) {
                        uncaughtExceptions[Thread.currentThread().name] = e
                    }
                }
            }

            val t2 = thread {
                entityManagerFactory.createEntityManager().use { em ->
                    val physicalConnection =
                        em.unwrap(SessionImplementor::class.java).jdbcCoordinator.logicalConnection.physicalConnection
                    physicalConnection.transactionIsolation = Connection.TRANSACTION_REPEATABLE_READ
                    try {
                        em.transaction.begin()
                        var posts = em.createQuery(
                            "select p from LazyPost p where p.title like 'repeatablePhantomRead%'",
                            LazyPost::class.java
                        ).resultList
                        posts shouldHaveSize 1
                        // wait a bit and check db once more
                        Thread.sleep(500)
                        em.clear()
                        posts = em.createQuery(
                            "select p from LazyPost p where p.title like 'repeatablePhantomRead%'",
                            LazyPost::class.java
                        ).resultList
                        posts shouldHaveSize 1
                        em.transaction.commit()
                    } catch (e: Throwable) {
                        uncaughtExceptions[Thread.currentThread().name] = e
                        em.transaction.rollback()
                    } finally {
                        physicalConnection.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
                    }
                }
            }

            t1.join()
            t2.join()

            if (uncaughtExceptions.isNotEmpty()) {
                fail(
                    RuntimeException(
                        "Expected no exceptions in threads but got ${uncaughtExceptions.size} in ${uncaughtExceptions.keys.joinToString()} threads",
                        uncaughtExceptions.values.first()
                    )
                )
            }

            db.checkQueryCount(3, LazyPost.TABLE_NAME)
            db.checkUpdateCount(1, LazyPost.TABLE_NAME)
        }
    }

    // serializable is similar to repeatable reads but also monitors concurrent changes that may modify other transaction behaviour and then abort them
    // that's why no new tests were written
    @Nested
    inner class Serializable

    private inline fun <reified T> EntityManager.getEntityByQuery(
        id: Long?,
        lockMode: LockModeType = LockModeType.NONE
    ) = this.createQuery("select p from ${T::class.simpleName} p where p.id = :id", T::class.java)
        .setParameter("id", id)
        .setLockMode(lockMode)
        .singleResult

    /**
     * Function uses reflection to create one Post, then runs 2 concurrent transactions
     * that try to update the same record using different entity managers.
     *
     * Transactions are automatically started and committed at the end of the thread
     * if transaction is still active at this point.
     *
     * Lambdas passed as arguments can use passed EntityManager to manually commit or roll back the transaction
     * lockModeType arguments can be used to control lock type used when fetching created post from the DB
     * transactionIsolationLevel arguments can be used to control transaction isolation level
     * Isolation level is changed in similar way like in Spring HibernateTransactionManager (DataSourceUtils.prepareConnectionForTransaction)
     *
     * Assertions for inserts and query that checks update outcome are performed inside the function.
     * Assertions for query and update count in the threads have to be performed in dbAssertions lambda
     * because they can be different based on test scenario
     */
    private inline fun <reified T : BaseEntity> concurrentTransactionsTest(
        firstTransaction: RunnableTransaction<T>,
        secondTransaction: RunnableTransaction<T>,
        dbAssertions: () -> Unit,
        expectedTitle: String,
        lockModeType: LockModeType = LockModeType.NONE,
        secondTransactionLockType: LockModeType = lockModeType,
        transactionIsolationLevel: Int = Connection.TRANSACTION_READ_COMMITTED,
        secondTransactionIsolationLevel: Int = transactionIsolationLevel,
    ) {
        val entityName = T::class.simpleName!!
        val tableName = T::class.annotations.filterIsInstance<Table>().firstOrNull()?.name
        val post = transaction {
            val versionedPost = T::class.java.getConstructor(String::class.java).newInstance("test")
            entityManager.persist(versionedPost)
            versionedPost
        }!!
        db.checkInsertCount(1, tableName)

        // first transactions always executes query first and then waits a bit for the second one to start
        val t1 = transactionThread(
            post,
            entityName,
            0L,
            150L,
            lockModeType,
            transactionIsolationLevel,
            firstTransaction
        )
        // second transactions waits a bit before query and then executes without interruption. It waits 50ms less to avoid race conditions
        val t2 = transactionThread(
            post,
            entityName,
            100L,
            0L,
            secondTransactionLockType,
            secondTransactionIsolationLevel,
            secondTransaction
        )

        t1.join()
        t2.join()

        if (uncaughtExceptions.isNotEmpty()) {
            fail(
                RuntimeException(
                    "Expected no exceptions in threads but got ${uncaughtExceptions.size} in ${uncaughtExceptions.keys.joinToString()} threads",
                    uncaughtExceptions.values.first()
                )
            )
        }
        dbAssertions()

        transaction {
            val updatedPost = entityManager.getEntityByQuery<T>(post.id)

            val property = T::class.members.filterIsInstance<KProperty<*>>().find { it.name == "title" }!!
            property.getter.call(updatedPost) shouldBeEqualTo expectedTitle
        }
        db.checkQueryCount(1, tableName)
    }

    /**
     * Here delays are added to make sure 2 transactions are overlapping. Delay was moved into function to
     * avoid adding them in every test that is based on pattern of the same entity updated in 2 transactions
     */
    private inline fun <reified T : BaseEntity> transactionThread(
        post: T,
        entityName: String,
        initialDelay: Long,
        delayAfterQuery: Long,
        lockModeType: LockModeType,
        transactionIsolationLevel: Int,
        transactionLogic: RunnableTransaction<T>
    ): Thread = thread {
        val em = entityManagerFactory.createEntityManager()
        val physicalConnection =
            em.unwrap(SessionImplementor::class.java).jdbcCoordinator.logicalConnection.physicalConnection
        physicalConnection.transactionIsolation = transactionIsolationLevel
        try {
            em.transaction.begin()
            Thread.sleep(initialDelay)
            val preparedPost = when (transactionLogic) {
                is RunnableTransaction.PrefetchedEntityTransactionRunner ->
                    em.getEntityByQuery<T>(post.id, lockModeType)

                // note that this is not setting lockMode as there is no query to lock
                is RunnableTransaction.StandaloneTransactionRunner -> post
            }
            Thread.sleep(delayAfterQuery)
            transactionLogic.run(preparedPost, em)
            if (em.transaction.isActive) {
                em.transaction.commit()
            }
        } catch (e: Throwable) {
            if (em.transaction.isActive) {
                em.transaction.rollback()
            }
            uncaughtExceptions[Thread.currentThread().name] = e
            throw e
        } finally {
            physicalConnection.transactionIsolation = Connection.TRANSACTION_READ_COMMITTED
            // avoid connection leaks
            em.close()
        }
    }
}

sealed interface RunnableTransaction<T> {
    fun run(p: T, em: EntityManager)

    /**
     * This interface represents a test where entity is fetched from db outside of the test and can be operated on straight away
     */
    fun interface PrefetchedEntityTransactionRunner<T> : RunnableTransaction<T>

    /**
     * This interface receives detached entity as an argument and has to do all the work on its own. This is a workaround for pessimistic locking test with repeatable reads isolation level
     */
    fun interface StandaloneTransactionRunner<T> : RunnableTransaction<T>
}

inline fun <reified T> prefetched(crossinline logic: (p: T, em: EntityManager) -> Unit): RunnableTransaction.PrefetchedEntityTransactionRunner<T> =
    RunnableTransaction.PrefetchedEntityTransactionRunner { p, em -> logic(p, em) }

inline fun <reified T> standalone(crossinline logic: (p: T, em: EntityManager) -> Unit): RunnableTransaction.StandaloneTransactionRunner<T> =
    RunnableTransaction.StandaloneTransactionRunner { p, em -> logic(p, em) }