package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.LongProblem
import org.springframework.data.jpa.repository.JpaRepository

interface LongProblemRepository : JpaRepository<LongProblem, Long>
