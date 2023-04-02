package io.csbroker.apiserver.service.Problem

interface AdminProblemService {

    fun findProblems()
    fun findProblemById()
    fun createProblem()
    fun updateProblem()

}
