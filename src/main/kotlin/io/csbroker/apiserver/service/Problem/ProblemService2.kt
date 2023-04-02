package io.csbroker.apiserver.service.Problem

interface ProblemService2 {

    fun findProblemDetailById(id: Long, email: String)
    fun submitProblem()


}
