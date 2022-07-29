package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.GradingStandard
import org.springframework.data.jpa.repository.JpaRepository

interface GradingStandardRepository : JpaRepository<GradingStandard, Long>
