package io.barinek.continuum.backlog

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.barinek.continuum.discovery.DiscoveryClient
import io.barinek.continuum.jdbcsupport.DataSourceConfig
import io.barinek.continuum.jdbcsupport.JdbcTemplate
import io.barinek.continuum.restsupport.BasicServer
import io.barinek.continuum.restsupport.DefaultController
import io.barinek.continuum.restsupport.RestTemplate
import java.lang.System.getenv
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class App(val url: String, port: Int) : BasicServer(port) {
    val mapper: ObjectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    override fun registerContexts() {
        val dataSource = DataSourceConfig().createDataSource(url)
        val template = JdbcTemplate(dataSource)

        context("/stories", StoryController(mapper, StoryDataGateway(template), ProjectClient(mapper, RestTemplate())))
        context("/", DefaultController())
    }

    override fun start() {
        super.start()
        Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory()).scheduleAtFixedRate({
            DiscoveryClient(mapper, RestTemplate()).heartbeat("backlog", uri())
        }, 0L, 30L, TimeUnit.SECONDS)
    }
}

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val url = getenv("DATABASE_URL")
    val port = getenv("PORT").toInt()
    App(url, port).start()
}
