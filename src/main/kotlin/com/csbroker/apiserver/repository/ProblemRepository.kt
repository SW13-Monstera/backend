package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.Problem
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface ProblemRepository : JpaRepository<Problem, UUID>, ProblemRepositoryCustom
