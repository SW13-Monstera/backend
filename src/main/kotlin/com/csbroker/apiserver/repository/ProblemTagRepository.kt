package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.ProblemTag
import org.springframework.data.jpa.repository.JpaRepository

interface ProblemTagRepository : JpaRepository<ProblemTag, Long>
