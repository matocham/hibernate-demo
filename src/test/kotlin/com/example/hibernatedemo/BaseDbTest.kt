package com.example.hibernatedemo

import com.example.hibernatedemo.spring.*
import com.example.hibernatedemo.utils.DbAssertions
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
abstract class BaseDbTest {

    // spring automatically looks for nested configurations and loads them
    @Configuration
    class TestContainer {

        // registering container as a bean binds it to spring context.
        // this is a more optimal approach than using @TestContainers and @Container annotations
        // as they cause new container to be created for each test class and @DirtiesContext annotation is needed
        // to create new connection pool for each test class
        @Bean
        @ServiceConnection
        fun postgres(): PostgreSQLContainer<*>? {
            return PostgreSQLContainer(DockerImageName.parse("postgres:16.2"))
                .withUsername("mm")
                .withPassword("secret")
                .withDatabaseName("test_db")
        }
    }
    companion object {
        @RegisterExtension
        @JvmStatic
        val db = DbAssertions()
    }

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    lateinit var entityManager: EntityManager

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    lateinit var lazyPostRepository: LazyPostRepository

    @Autowired
    lateinit var sequencePostRepository: SequencePostRepository

    @Autowired
    lateinit var versionIdentityRepository: VersionIdentityPostRepository

    @Autowired
    lateinit var versionSequenceRepository: VersionSeqPostRepository

    @Autowired
    lateinit var manualIPostRepository: ManualIdPostRepository

    @Autowired
    lateinit var manualStringIPostRepository: ManualStringIdPostRepository

    @Autowired
    lateinit var primitiveIPostRepository: PrimitiveIdPostRepository

    @Autowired
    lateinit var primitiveVersionPostRepository: PrimitiveVersionPostRepository

    fun <T> transaction(block: () -> T): T? {
        return transactionTemplate.execute {
            block()
        }
    }
}
