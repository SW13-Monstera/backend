package io.csbroker.apiserver.service.problem.admin

import io.csbroker.apiserver.auth.ProviderType
import io.csbroker.apiserver.common.exception.ConditionConflictException
import io.csbroker.apiserver.common.exception.EntityNotFoundException
import io.csbroker.apiserver.model.LongProblem
import io.csbroker.apiserver.model.ProblemTag
import io.csbroker.apiserver.model.Tag
import io.csbroker.apiserver.model.User
import io.csbroker.apiserver.repository.problem.ProblemTagRepository
import io.csbroker.apiserver.repository.problem.TagRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class TagUpserterTest {
    private val tagRepository = mockk<TagRepository>()
    private val problemTagRepository = mockk<ProblemTagRepository>()
    private val tagUpserter = TagUpserter(tagRepository, problemTagRepository)

    private val tags = mutableListOf(Tag(name = "tag1"), Tag(name = "tag2"))
    private val user = User(
        id = UUID.randomUUID(),
        email = "test@test.com",
        password = "test1234!",
        username = "test",
        providerType = ProviderType.LOCAL,
    )

    @BeforeEach
    fun setUp() {
        clearMocks(tagRepository, problemTagRepository)
    }

    @Test
    fun `태그가 없는 문제에 태그를 업데이트 시킬 수 있다`() {
        // given
        val problem = getLongProblem()
        val tagNames = listOf("tag1", "tag2")
        val tag1 = Tag(id = 1, name = "tag1")
        val tag2 = Tag(id = 2, name = "tag2")
        every { tagRepository.findTagsByNameIn(tagNames) } returns listOf(tag1, tag2)
        every { problemTagRepository.saveAll(any<List<ProblemTag>>()) } returns mutableListOf()

        // when
        tagUpserter.setTags(problem, tagNames)

        // then
        verify {
            tagRepository.findTagsByNameIn(tagNames)
            problemTagRepository.saveAll(any<List<ProblemTag>>())
        }
        assertEquals(2, problem.problemTags.size)
        val updatedTagSet = problem.problemTags.map { it.tag.name }.toSet()
        assert(updatedTagSet.contains("tag1"))
        assert(updatedTagSet.contains("tag2"))
    }

    @Test
    fun `tagName은 비어있을 수 없다`() {
        // given
        val problem = getLongProblem()
        val tagNames = emptyList<String>()

        // when, then
        assertThrows<ConditionConflictException> { tagUpserter.setTags(problem, tagNames) }
    }

    @Test
    fun `tagNames에 존재하지 않는 태그가 있다면 예외가 발생한다`() {
        // given
        val problem = getLongProblem()
        val tagNames = listOf("tag1", "tag2")
        val tags = listOf(Tag(name = "tag1"))
        every { tagRepository.findTagsByNameIn(tagNames) } returns tags

        // when, then
        assertThrows<EntityNotFoundException> { tagUpserter.setTags(problem, tagNames) }
    }

    @Test
    fun `존재하지 않는 태그를 업데이트하려고 하면 예외가 발생한다`() {
        // given
        val problem = getLongProblem()
        val tagNames = listOf("tag1", "tag2")
        val tags = listOf(Tag(name = "tag1"))
        every { tagRepository.findTagsByNameIn(tagNames) } returns tags

        // when, then
        assertThrows<EntityNotFoundException> { tagUpserter.updateTags(problem, tagNames.toMutableList()) }
    }

    private fun getLongProblem(): LongProblem = LongProblem(
        title = "test title",
        description = "test description",
        creator = user,
    )
}
