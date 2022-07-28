package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.ShortProblem
import org.springframework.data.jpa.repository.JpaRepository

interface ShortProblemRepository : JpaRepository<ShortProblem, Long>
