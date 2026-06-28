package com.hogimn.malchart.testsupport

import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import javax.sql.DataSource

class TestScenarioSupport(val dataSource: DataSource) {
    val template = JdbcTemplate(dataSource)

    fun loadTestScenario(name: String) {
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        this.javaClass.classLoader.getResourceAsStream("$name.sql").reader().readLines()
                .asSequence()
                .filterNot(String::isNullOrBlank)
                .forEach { template.execute(it) }
    }
}
