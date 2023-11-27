package io.csbroker.apiserver.repository.common

import io.csbroker.apiserver.common.config.properties.AppProperties
import io.csbroker.apiserver.dto.common.RankListDto
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ZSetOperations.TypedTuple
import java.util.UUID

class RedisRepositoryTest {
    private lateinit var sut: RedisRepository
    private lateinit var redisTemplate: StringRedisTemplate
    private val appProperties = AppProperties(
        auth = AppProperties.Auth(
            tokenExpiry = 1,
            refreshTokenExpiry = 1,
            tokenSecret = "this is secret"
        ),
        oAuth2 = AppProperties.OAuth2(
            authorizedRedirectUris = listOf("http://localhost:3000/oauth2/redirect"),
        ),
    )

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        sut = RedisRepository(
            redisTemplate = redisTemplate,
            appProperties = appProperties,
        )
    }

    @Test
    fun `랭킹 조회에 성공한다`() {
        // given
        val randomUUIDs = (0..6).map { UUID.randomUUID() }
        every { redisTemplate.opsForZSet().reverseRangeWithScores(RANKING, 0, 8) } returns setOf(
            TypedTuple.of("${randomUUIDs[0]}@username1", 5.0),
            TypedTuple.of("${randomUUIDs[1]}@username2", 5.0),
            TypedTuple.of("${randomUUIDs[2]}@username3", 4.0),
            TypedTuple.of("${randomUUIDs[3]}@username4", 4.0),
            TypedTuple.of("${randomUUIDs[4]}@username5", 3.0),
            TypedTuple.of("${randomUUIDs[5]}@username6", 2.0),
            TypedTuple.of("${randomUUIDs[6]}@username7", 1.0),
        )
        every { redisTemplate.opsForZSet().size(RANKING) } returns 7
        every { redisTemplate.opsForZSet().reverseRangeByScore(RANKING, 5.0, 5.0,  0, 1) } returns setOf(
            "${randomUUIDs[0]}@username1",
            "${randomUUIDs[1]}@username2",
        )
        every { redisTemplate.opsForZSet().reverseRank(RANKING, "${randomUUIDs[0]}@username1") } returns 0
        every { redisTemplate.opsForZSet().reverseRangeByScore(RANKING, 4.0, 4.0,  0, 1) } returns setOf(
            "${randomUUIDs[2]}@username3",
            "${randomUUIDs[3]}@username4",
        )
        every { redisTemplate.opsForZSet().reverseRank(RANKING, "${randomUUIDs[2]}@username3") } returns 2
        every { redisTemplate.opsForZSet().reverseRangeByScore(RANKING, 3.0, 3.0,  0, 1) } returns setOf(
            "${randomUUIDs[4]}@username5",
        )
        every { redisTemplate.opsForZSet().reverseRank(RANKING, "${randomUUIDs[4]}@username5") } returns 4
        every { redisTemplate.opsForZSet().reverseRangeByScore(RANKING, 2.0, 2.0,  0, 1) } returns setOf(
            "${randomUUIDs[5]}@username6",
        )
        every { redisTemplate.opsForZSet().reverseRank(RANKING, "${randomUUIDs[5]}@username6") } returns 5
        every { redisTemplate.opsForZSet().reverseRangeByScore(RANKING, 1.0, 1.0,  0, 1) } returns setOf(
            "${randomUUIDs[6]}@username7",
        )
        every { redisTemplate.opsForZSet().reverseRank(RANKING, "${randomUUIDs[6]}@username7") } returns 6

        // when
        val result = sut.getRanks(
            size = 9,
            page = 0,
        )

        // then
        result.size shouldBe 9
        result.totalPage shouldBe 1
        result.currentPage shouldBe 0
        result.numberOfElements shouldBe 7
        result.contents shouldBe listOf(
            RankListDto.RankDetail(
                randomUUIDs[0],
                "username1",
                1L,
                5.0,
            ),
            RankListDto.RankDetail(
                randomUUIDs[1],
                "username2",
                1L,
                5.0,
            ),
            RankListDto.RankDetail(
                randomUUIDs[2],
                "username3",
                3L,
                4.0,
            ),
            RankListDto.RankDetail(
                randomUUIDs[3],
                "username4",
                3L,
                4.0,
            ),
            RankListDto.RankDetail(
                randomUUIDs[4],
                "username5",
                5L,
                3.0,
            ),
            RankListDto.RankDetail(
                randomUUIDs[5],
                "username6",
                6L,
                2.0,
            ),
            RankListDto.RankDetail(
                randomUUIDs[6],
                "username7",
                7L,
                1.0,
            ),
        )
    }
}
