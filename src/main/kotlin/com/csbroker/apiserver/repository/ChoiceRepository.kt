package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.Choice
import org.springframework.data.jpa.repository.JpaRepository

interface ChoiceRepository : JpaRepository<Choice, Long>
