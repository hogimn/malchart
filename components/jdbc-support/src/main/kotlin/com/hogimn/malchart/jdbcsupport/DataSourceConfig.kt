package com.hogimn.malchart.jdbcsupport

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

open class DataSourceConfig {
    fun createDataSource(url: String, maximumPoolSize: Int = 3): HikariDataSource {
        return HikariDataSource(HikariConfig().apply {
            jdbcUrl = url
            this.maximumPoolSize = maximumPoolSize
            validate()
        })
    }
}
