package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.UserAnswer
import org.springframework.data.jpa.repository.JpaRepository

interface UserAnswerRepository : JpaRepository<UserAnswer, Long>, UserAnswerRepositoryCustom
