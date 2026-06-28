package com.hogimn.malchart.allocations

import com.fasterxml.jackson.databind.ObjectMapper
import com.hogimn.malchart.circuitbreaker.CircuitBreaker
import com.hogimn.malchart.discovery.DiscoveryClient
import com.hogimn.malchart.restsupport.RestTemplate

open class ProjectClient(val mapper: ObjectMapper, val template: RestTemplate) {
    private val circuitBreaker = CircuitBreaker()

    open fun getProject(projectId: Long): ProjectInfo? {
        val endpoint = DiscoveryClient(mapper, template).getUrl("registration") ?: return null

        return circuitBreaker.withCircuitBreaker({
            val response =
                template.get("$endpoint/project", "application/json", Pair("projectId", projectId.toString()))

            if (!response.isBlank()) mapper.readValue(response, ProjectInfo::class.java) else null

        }, fallback())
    }

    private fun fallback(): () -> Nothing? {
        return { null }
    }
}
