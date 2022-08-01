package com.csbroker.apiserver.repository

import com.csbroker.apiserver.model.UserAnswer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserAnswerRepository : JpaRepository<UserAnswer, Long>, UserAnswerRepositoryCustom {
    @Query(
        "SELECT ua FROM UserAnswer ua WHERE (ua.problem.id = :id OR :id IS NULL) " +
            "AND (ua.assignedUser.username = :assignedBy OR :assignedBy IS NULL) " +
            "AND (ua.validatingUser.username = :validatedBy OR :validatedBy IS NULL) " +
            "AND (ua.problem.title LIKE '%'||:problemTitle||'%' OR :problemTitle IS NULL) " +
            "AND (ua.answer LIKE '%'||:answer||'%' OR :answer IS NULL) " +
            "AND (ua.isLabeled = :isLabeled OR :isLabeled IS NULL) " +
            "AND (ua.isValidated =:isValidated OR :isValidated IS NULL)"
    )
    fun findUserAnswersByQuery(
        @Param("id") id: Long?,
        @Param("assignedBy") assignedBy: String?,
        @Param("validatedBy") validatedBy: String?,
        @Param("problemTitle") problemTitle: String?,
        @Param("answer") answer: String?,
        @Param("isLabeled") isLabeled: Boolean?,
        @Param("isValidated") isValidated: Boolean?,
        pageable: Pageable
    ): Page<UserAnswer>
}
