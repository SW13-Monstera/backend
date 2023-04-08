package io.csbroker.apiserver.repository.common

import io.csbroker.apiserver.model.Major
import org.springframework.data.jpa.repository.JpaRepository

interface MajorRepository : JpaRepository<Major, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<Major>
}
