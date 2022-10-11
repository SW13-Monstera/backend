package io.csbroker.apiserver.dto.common

import java.util.UUID

data class RankListDto(
    val size: Long,
    val totalPage: Long,
    val currentPage: Long,
    val numberOfElements: Long,
    val contents: List<RankDetail>
) {
    data class RankDetail(
        val id: UUID,
        val username: String,
        val rank: Long,
        val score: Double
    )
}
