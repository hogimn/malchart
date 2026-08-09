package com.hogimn.malcollector.anime

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hogimn.malcollector.circuitbreakersupport.CircuitBreaker
import com.hogimn.malcollector.discoverysupport.DiscoveryClient
import com.hogimn.malcollector.restsupport.RestTemplate
import java.time.LocalDateTime

open class PollClient(val mapper: ObjectMapper, val template: RestTemplate) {
    private val circuitBreaker = CircuitBreaker()

    open fun fetchPollContentIds(contentType: String): Set<Int> {
        val endpoint = DiscoveryClient(mapper, template).getUrl("poll")

        val response = circuitBreaker.withCircuitBreaker({
            template.get("$endpoint/poll/content-ids?contentType=$contentType", "application/json")
        }, fallback())

        if (response.isNullOrBlank()) {
            return emptySet()
        }

        val idList: List<Int> = mapper.readValue(response, object : TypeReference<List<Int>>() {})
        return idList.toSet()
    }

    open fun fetchEpisodeDistribution(
        contentId: Int,
        contentType: String
    ): Pair<Map<Int, Map<String, Any>>?, LocalDateTime?> {
        return try {
            val endpoint = DiscoveryClient(mapper, template).getUrl("poll")

            val response = circuitBreaker.withCircuitBreaker({
                template.get("$endpoint/poll/summary?contentId=$contentId&contentType=$contentType", "application/json")
            }, fallback())

            if (response.isNullOrBlank()) {
                return Pair(null, null)
            }

            val jsonNode: JsonNode = mapper.readTree(response)

            val distributionNode = jsonNode.get("episodeDistribution")
            val distribution = if (distributionNode != null && !distributionNode.isNull) {
                mapper.convertValue(distributionNode, object : TypeReference<Map<Int, Map<String, Any>>>() {})
            } else {
                null
            }

            val maxUpdatedAtNode = jsonNode.get("maxUpdatedAt")
            val maxUpdatedAt = if (maxUpdatedAtNode != null && !maxUpdatedAtNode.isNull && !maxUpdatedAtNode.asText()
                    .isNullOrBlank()
            ) {
                LocalDateTime.parse(maxUpdatedAtNode.asText())
            } else {
                null
            }

            Pair(distribution, maxUpdatedAt)
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    open fun fetchEpisodeDistributions(
        contentIds: List<Int>,
        contentType: String
    ): Map<Int, Pair<Map<Int, Map<String, Any>>?, LocalDateTime?>> {
        if (contentIds.isEmpty()) return emptyMap()

        return try {
            val endpoint = DiscoveryClient(mapper, template).getUrl("poll")
            val idsParam = contentIds.joinToString(",")

            val response = circuitBreaker.withCircuitBreaker({
                template.get(
                    "$endpoint/poll/summaries?contentType=$contentType&contentIds=$idsParam",
                    "application/json"
                )
            }, fallback())

            if (response.isNullOrBlank()) {
                return emptyMap()
            }

            val jsonNode: JsonNode = mapper.readTree(response)
            val resultMap = mutableMapOf<Int, Pair<Map<Int, Map<String, Any>>?, LocalDateTime?>>()

            if (jsonNode.isArray) {
                for (node in jsonNode) {
                    val contentId = node.get("contentId")?.asInt() ?: continue

                    val distNode = node.get("episodeDistribution")
                    val distMap = if (distNode != null && !distNode.isNull) {
                        mapper.convertValue(
                            distNode,
                            object : TypeReference<Map<Int, Map<String, Any>>>() {}
                        )
                    } else {
                        null
                    }

                    val maxUpdatedAtNode = node.get("maxUpdatedAt")
                    val maxUpdatedAt = if (maxUpdatedAtNode != null
                        && !maxUpdatedAtNode.isNull
                        && !maxUpdatedAtNode.asText().isNullOrBlank()
                    ) {
                        LocalDateTime.parse(maxUpdatedAtNode.asText())
                    } else {
                        null
                    }

                    resultMap[contentId] = Pair(distMap, maxUpdatedAt)
                }
            }

            resultMap
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun fallback(): () -> Nothing? = { null }
}