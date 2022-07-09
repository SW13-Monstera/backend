package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.GradingHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GradingHistoryRepository : JpaRepository<GradingHistory, UUID>
