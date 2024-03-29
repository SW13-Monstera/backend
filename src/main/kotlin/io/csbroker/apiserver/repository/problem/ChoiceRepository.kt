package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.Choice
import org.springframework.data.jpa.repository.JpaRepository

interface ChoiceRepository : JpaRepository<Choice, Long>
