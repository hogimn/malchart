package com.hogimn.malchart.poll

import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import java.sql.Connection
import java.time.LocalDateTime

class PollDataGateway(val jdbcTemplate: JdbcTemplate) {
    val createSql = """
        insert into poll (content_id, topic_id, poll_option_id, title, episode, votes, created_at, updated_at) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

    fun create(
        connection: Connection,
        contentId: Int,
        topicId: Int,
        pollOptionId: Int,
        title: String,
        episode: Int,
        votes: Int,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime
    ): PollRecord {
        val now = LocalDateTime.now()

        return jdbcTemplate.create(
            connection,
            createSql,
            { PollRecord(contentId, topicId, pollOptionId, title, episode, votes, now, now) },
            contentId, topicId, pollOptionId, title, episode, votes, createdAt, updatedAt
        )
    }

    fun create(
        contentId: Int,
        topicId: Int,
        pollOptionId: Int,
        title: String,
        episode: Int,
        votes: Int,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime
    ): PollRecord {
        val now = LocalDateTime.now()

        return jdbcTemplate.create(
            createSql,
            { PollRecord(contentId, topicId, pollOptionId, title, episode, votes, now, now) },
            contentId, topicId, pollOptionId, title, episode, votes, createdAt, updatedAt
        )
    }

    fun findObject(contentId: Int, topicId: Int, pollOptionId: Int): PollRecord? {
        val s = """
            select content_id, topic_id, poll_option_id, title, episode, votes, created_at, updated_at
            from poll
            where content_id = ? and poll_option_id = ? and topic_id = ?
        """.trimIndent()

        return jdbcTemplate.findObject(
            s,
            { rs ->
                PollRecord(
                    contentId = rs.getInt("content_id"),
                    topicId = rs.getInt("topic_id"),
                    pollOptionId = rs.getInt("poll_option_id"),
                    title = rs.getString("title"),
                    episode = rs.getInt("episode"),
                    votes = rs.getInt("votes"),
                    createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                    updatedAt = rs.getTimestamp("updated_at").toLocalDateTime()
                )
            },
            contentId, pollOptionId, topicId
        )
    }
}