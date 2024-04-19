package com.example.hibernatedemo

import com.example.hibernatedemo.utils.DbAssertions
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@ActiveProfiles("test")
abstract class BaseDbTest {
    companion object {
        @JvmStatic
        @Container
        @ServiceConnection
        val container = PostgreSQLContainer(DockerImageName.parse("postgres:16.2"))
            .withUsername("mm")
            .withPassword("secret")

        @RegisterExtension
        @JvmStatic
        val db = DbAssertions()
    }

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Autowired
    lateinit var entityManager: EntityManager

    fun <T> transaction(block: () -> T): T? {
        return transactionTemplate.execute {
            block()
        }
    }
}
