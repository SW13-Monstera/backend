package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.Tech
import org.springframework.data.jpa.repository.JpaRepository

interface TechRepository : JpaRepository<Tech, Long> {
    fun findByNameStartingWithIgnoreCase(name: String): List<Tech>
}
