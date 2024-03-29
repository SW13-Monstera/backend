package io.csbroker.apiserver.repository.common

import io.csbroker.apiserver.model.Tech
import org.springframework.data.jpa.repository.JpaRepository

interface TechRepository : JpaRepository<Tech, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<Tech>
}
