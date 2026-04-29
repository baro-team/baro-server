package com.baro.common.kakao.mobility

import com.baro.common.core.exception.ExternalServiceException
import com.baro.common.kakao.config.KakaoMobilityProperties
import com.baro.common.kakao.mobility.directions.KakaoDirectionsResponse
import com.baro.common.kakao.mobility.directions.KakaoMobilityDirectionPoint
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class KakaoMobilityClient(
    private val properties: KakaoMobilityProperties,
    private val client: RestClient = RestClient.builder()
        .baseUrl(properties.baseUrl)
        .build(),
) {
    fun findDirections(
        origin: KakaoMobilityDirectionPoint,
        destination: KakaoMobilityDirectionPoint,
    ): KakaoDirectionsResponse {
        if (!properties.hasApiKey()) {
            throw ExternalServiceException("카카오모빌리티 API 키가 설정되지 않았습니다.")
        }

        return client.get()
            .uri { builder ->
                builder.path(KakaoMobilityApiPaths.DIRECTIONS)
                    .queryParam("origin", origin.toKakaoParameter())
                    .queryParam("destination", destination.toKakaoParameter())
                    .queryParam("priority", "RECOMMEND")
                    .queryParam("alternatives", false)
                    .queryParam("road_details", false)
                    .queryParam("summary", false)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", properties.authorizationHeaderValue())
            .retrieve()
            .body(KakaoDirectionsResponse::class.java)
            ?: throw ExternalServiceException("카카오모빌리티에서 빈 응답을 반환했습니다.")
    }

    private fun KakaoMobilityDirectionPoint.toKakaoParameter(): String =
        if (name.isNullOrBlank()) "$longitude,$latitude" else "$longitude,$latitude,name=$name"
}
