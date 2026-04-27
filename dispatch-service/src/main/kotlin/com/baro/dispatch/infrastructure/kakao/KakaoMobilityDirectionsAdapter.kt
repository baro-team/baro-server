package com.baro.dispatch.infrastructure.kakao

import com.baro.dispatch.application.exception.ExternalRouteException
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.RouteEstimate
import com.baro.dispatch.domain.exception.RouteNotFoundException
import com.baro.dispatch.domain.model.GeoPoint
import com.baro.dispatch.infrastructure.config.KakaoMobilityProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class KakaoMobilityDirectionsAdapter(
    private val properties: KakaoMobilityProperties,
    restClientBuilder: RestClient.Builder,
) : DirectionsPort {
    private val client = restClientBuilder.baseUrl(properties.baseUrl).build()

    override fun findRoute(origin: GeoPoint, destination: GeoPoint): RouteEstimate {
        if (properties.apiKey.isBlank()) {
            throw ExternalRouteException("카카오모빌리티 API 키가 설정되지 않았습니다.")
        }

        val response = client.get()
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
            .header("Authorization", "KakaoAK ${properties.apiKey}")
            .retrieve()
            .body(KakaoDirectionsResponse::class.java)
            ?: throw ExternalRouteException("카카오모빌리티에서 빈 응답을 반환했습니다.")

        val route = response.routes.firstOrNull()
            ?: throw RouteNotFoundException("경로를 찾을 수 없습니다.")
        if (route.result_code != 0) {
            throw RouteNotFoundException(route.result_msg)
        }
        val summary = route.summary
            ?: throw RouteNotFoundException("경로 요약 정보를 찾을 수 없습니다.")

        return RouteEstimate(
            fare = summary.fare.taxi,
            routePath = route.sections.flatMap { section ->
                section.roads.flatMap { road ->
                    road.vertexes.chunked(2)
                        .filter { it.size == 2 }
                        .map { GeoPoint(longitude = it[0], latitude = it[1]) }
                }
            },
            durationSeconds = summary.duration,
            distanceMeters = summary.distance,
        )
    }

    private fun GeoPoint.toKakaoParameter(): String =
        if (name.isNullOrBlank()) "$longitude,$latitude" else "$longitude,$latitude,name=$name"
}
