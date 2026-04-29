package com.baro.dispatch.infrastructure.kakao

import com.baro.common.kakao.mobility.KakaoMobilityClient
import com.baro.common.kakao.mobility.directions.KakaoDirectionsResponse
import com.baro.common.kakao.mobility.directions.KakaoMobilityDirectionPoint
import com.baro.dispatch.application.port.out.DirectionsPort
import com.baro.dispatch.application.port.out.RouteEstimate
import com.baro.dispatch.domain.exception.RouteNotFoundException
import com.baro.dispatch.domain.model.GeoPoint
import org.springframework.stereotype.Component

@Component
class KakaoMobilityDirectionsAdapter(
    private val kakaoMobilityClient: KakaoMobilityClient,
) : DirectionsPort {
    override fun findRoute(origin: GeoPoint, destination: GeoPoint): RouteEstimate {
        val response = kakaoMobilityClient.findDirections(
            origin = origin.toKakaoDirectionPoint(),
            destination = destination.toKakaoDirectionPoint(),
        )

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

    private fun GeoPoint.toKakaoDirectionPoint(): KakaoMobilityDirectionPoint =
        KakaoMobilityDirectionPoint(
            longitude = longitude,
            latitude = latitude,
            name = name,
        )
}
