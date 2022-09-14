package com.csbroker.apiserver.dto

data class StatsDto(
    val problemCnt: Long,
    val gradableProblemCnt: Long,
    val userCnt: Long
)
