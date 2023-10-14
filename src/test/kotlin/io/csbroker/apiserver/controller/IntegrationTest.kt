package io.csbroker.apiserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.csbroker.apiserver.auth.AuthTokenProvider
import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.enums.Role
import io.csbroker.apiserver.model.User
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.Date
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit
import javax.persistence.Query

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTest {
    @PersistenceUnit
    private lateinit var entityManagerFactory: EntityManagerFactory

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var authTokenProvider: AuthTokenProvider

    protected lateinit var defaultUser: User
    protected lateinit var adminUser: User
    private val userEmail = "test@test.com"
    private val adminEmail = "admin@test.com"

    @BeforeEach
    fun setUp() {
        defaultUser = getOrCreateUser(userEmail)
        adminUser = getOrCreateUser(adminEmail)
    }

    fun getOrCreateUser(email: String): User =
        try {
            findOne("SELECT u FROM User u where u.email = '$email'")
        } catch (e: Exception) {
            saveUser(email, email == adminEmail)
        }

    fun saveUser(email: String, isAdmin: Boolean): User =
        save(
            User(
                email = email,
                username = email.split("@")[0],
                password = "test",
                role = if (isAdmin) Role.ROLE_ADMIN else Role.ROLE_USER,
                providerType = ProviderType.LOCAL,
            ),
        )

    fun request(
        method: HttpMethod,
        url: String,
        body: Any? = null,
        headers: MultiValueMap<String, String> = LinkedMultiValueMap(),
        params: MultiValueMap<String, String> = LinkedMultiValueMap(),
        isAdmin: Boolean = false,
    ): ResultActions {
        val req = MockMvcRequestBuilders.request(method, url)
            .headers(HttpHeaders(headers.also { it.add("Authorization", "Bearer ${createToken(isAdmin)}") }))
            .params(params)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)

        body?.let {
            req.content(objectMapper().writeValueAsString(it))
        }

        return mockMvc.perform(req)
    }

    fun <T> save(entity: T): T = runWithEntityManager(readonly = false) {
        it.persist(entity)
        entity
    }

    fun <T> findOne(queryString: String, params: Map<String, Any> = emptyMap()): T = runWithEntityManager {
        val query = it.createQuery(queryString, Any::class.java)
        setParams(query, params)
        query.singleResult as T
    }

    fun <T> findAll(queryString: String, params: Map<String, Any> = emptyMap()): List<T> = runWithEntityManager {
        val query = it.createQuery(queryString, Any::class.java)
        setParams(query, params)
        query.resultList as List<T>
    }

    fun <T> findById(id: Long, clazz: Class<T>): T = runWithEntityManager {
        it.find(clazz, id)
    }

    private fun setParams(query: Query, params: Map<String, Any>) {
        params.forEach { (key, value) ->
            query.setParameter(key, value)
        }
    }

    private fun <T> runWithEntityManager(readonly: Boolean = true, block: (em: EntityManager) -> T): T {
        val em = entityManagerFactory.createEntityManager()
        val result = if (readonly) {
            runCatching {
                block(em)
            }
        } else {
            val transaction = em.transaction
            transaction.begin()
            runCatching {
                block(em)
            }.onFailure {
                transaction.rollback()
            }.onSuccess {
                transaction.commit()
            }
        }
        em.close()
        return result.getOrThrow()
    }

    private fun createToken(isAdmin: Boolean): String {
        val user = getOrCreateUser(if (isAdmin) adminEmail else userEmail)

        return authTokenProvider.createAuthToken(
            user.email,
            Date(Date().time + 6000),
            user.role.code,
        ).token
    }

    private fun objectMapper(): ObjectMapper {
        return Jackson2ObjectMapperBuilder()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .featuresToDisable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .build()
    }
}
