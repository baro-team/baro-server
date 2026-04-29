package com.baro.dispatch.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kakao.mobility")
data class KakaoMobilityProperties(
    val baseUrl: String = "https://apis-navi.kakaomobility.com",
    val apiKey: String = "",
) {
    fun hasApiKey(): Boolean = apiKey.isNotBlank()

    fun authorizationHeaderValue(): String = "KakaoAK ${apiKey.trim()}"
}
