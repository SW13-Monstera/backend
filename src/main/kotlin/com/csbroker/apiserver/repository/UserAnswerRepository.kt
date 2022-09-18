package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.UserAnswer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface UserAnswerRepository : JpaRepository<UserAnswer, Long>, UserAnswerRepositoryCustom {
    @Query("select count(ua.id) from UserAnswer ua where ua.id in :ids")
    fun cntUserAnswer(@Param("ids") ids: List<Long>): Int

    @Modifying
    @Query("update UserAnswer ua set ua.assignedUser.id = :user_id where ua.id in :ids")
    fun updateLabelerId(@Param("ids") ids: List<Long>, @Param("user_id") userId: UUID)

    @Modifying
    @Query("update UserAnswer ua set ua.validatingUser.id = :user_id where ua.id in :ids")
    fun updateValidatorId(@Param("ids") ids: List<Long>, @Param("user_id") userId: UUID)
}
