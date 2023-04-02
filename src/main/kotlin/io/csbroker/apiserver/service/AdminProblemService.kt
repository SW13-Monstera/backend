package io.csbroker.apiserver.service

interface AdminProblemService {

    fun findProblems()
    fun findProblemById()
    fun createProblem()
    fun updateProblem()

}
