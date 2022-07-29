package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.MultipleChoiceProblem
import org.springframework.data.jpa.repository.JpaRepository

interface MultipleChoiceProblemRepository : JpaRepository<MultipleChoiceProblem, Long>
