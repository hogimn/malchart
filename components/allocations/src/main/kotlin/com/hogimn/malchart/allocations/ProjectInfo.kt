package com.hogimn.malchart.allocations

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectInfo(val active: Boolean)
