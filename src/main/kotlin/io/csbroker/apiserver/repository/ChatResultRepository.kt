package io.csbroker.apiserver.repository

import io.csbroker.apiserver.model.ChatResult
import org.springframework.data.jpa.repository.JpaRepository

interface ChatResultRepository : JpaRepository<ChatResult, Long>
