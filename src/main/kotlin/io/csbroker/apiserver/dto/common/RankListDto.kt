package io.csbroker.apiserver.dto.common

import java.util.UUID

data class RankListDto(
    val size: Long,
    val totalPage: Long,
    val currentPage: Long,
    val numberOfElements: Long,
    val contents: List<RankDetail>,
) {
    constructor() : this(0, 0, 0, 0, arrayListOf())
    data class RankDetail(
        val id: UUID,
        val username: String,
        val rank: Long,
        val score: Double,
    ) {
        constructor() : this(UUID.randomUUID(), "", 0, 0.0)
    }
}
