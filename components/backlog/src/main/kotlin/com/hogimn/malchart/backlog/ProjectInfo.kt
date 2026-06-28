package com.hogimn.malchart.backlog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectInfo(val active: Boolean)
