package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.ProblemTag
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProblemTagRepository : JpaRepository<ProblemTag, UUID>
