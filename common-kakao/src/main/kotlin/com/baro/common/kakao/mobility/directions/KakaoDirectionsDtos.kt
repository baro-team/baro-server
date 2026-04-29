package com.baro.common.kakao.mobility.directions

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoDirectionsResponse(
    val routes: List<KakaoRoute> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoRoute(
    val result_code: Int,
    val result_msg: String,
    val summary: KakaoSummary? = null,
    val sections: List<KakaoSection> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoSummary(
    val fare: KakaoFare,
    val distance: Int,
    val duration: Int,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoFare(
    val taxi: Int,
    val toll: Int,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoSection(
    val roads: List<KakaoRoad> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class KakaoRoad(
    val vertexes: List<Double> = emptyList(),
)
