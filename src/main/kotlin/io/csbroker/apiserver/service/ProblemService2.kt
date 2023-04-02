package io.csbroker.apiserver.service

interface ProblemService2 {

    fun findProblemDetailById(id: Long, email: String)
    fun submitProblem()


}
