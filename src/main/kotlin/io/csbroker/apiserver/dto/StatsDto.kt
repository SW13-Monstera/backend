package io.csbroker.apiserver.dto

data class StatsDto(
    val problemCnt: Long,
    val gradableProblemCnt: Long,
    val userCnt: Long
) {
    constructor() : this(0, 0, 0)
}
