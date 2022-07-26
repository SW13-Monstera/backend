package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.GradingHistory
import org.springframework.data.jpa.repository.JpaRepository

interface GradingHistoryRepository : JpaRepository<GradingHistory, Long>
