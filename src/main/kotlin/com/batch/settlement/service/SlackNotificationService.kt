package com.batch.settlement.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class SlackNotificationService(
    @Value("\${slack.webhook-url}") private val slackWebhookUrl: String,
    private val restTemplate: RestTemplate
) {
    fun sendSlackNotification(message: String) {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val payload = mapOf("text" to message)
        val request = HttpEntity(payload, headers)
        restTemplate.postForEntity(slackWebhookUrl, request, String::class.java)
    }
}