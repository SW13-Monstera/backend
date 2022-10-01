package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {
    fun findTagsByNameIn(names: List<String>): List<Tag>
}
