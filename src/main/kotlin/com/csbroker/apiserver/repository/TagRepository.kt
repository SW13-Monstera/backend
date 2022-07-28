package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {
    fun findTagsByNameIn(names: List<String>): List<Tag>
}
