package com.hogimn.malcollector.poll

import java.time.LocalDateTime

data class PollSummaryInfo(
    val contentId: Int,
    val contentType: String,
    val maxUpdatedAt: LocalDateTime?,
    val episodeDistribution: Map<Int, Map<String, Any>>
)