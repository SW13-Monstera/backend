package io.csbroker.apiserver.repository.problem

import io.csbroker.apiserver.model.Challenge
import org.springframework.data.jpa.repository.JpaRepository

interface ChallengeRepository : JpaRepository<Challenge, Long>
