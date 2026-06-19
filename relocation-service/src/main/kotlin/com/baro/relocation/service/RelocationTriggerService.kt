package com.baro.relocation.service

import com.baro.common.kakao.mobility.KakaoMobilityClient
import com.baro.common.kakao.mobility.directions.KakaoMobilityDirectionPoint
import com.baro.relocation.client.ControlServiceClient
import com.baro.relocation.dto.RelocationAssignRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RelocationTriggerService(
    private val relocationService: RelocationService,
    private val kakaoMobilityClient: KakaoMobilityClient,
    private val controlServiceClient: ControlServiceClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun triggerRelocation(carId: Long, lat: Double, lon: Double) {
        val dest = relocationService.assignRelocation(
            RelocationAssignRequest(carId = carId, currentLat = lat, currentLon = lon)
        )

        val kakaoResponse = kakaoMobilityClient.findDirections(
            origin = KakaoMobilityDirectionPoint(longitude = lon, latitude = lat),
            destination = KakaoMobilityDirectionPoint(longitude = dest.targetLon, latitude = dest.targetLat),
        )

        val route = kakaoResponse.routes.firstOrNull()
            ?: throw IllegalStateException("카카오 경로 없음. carId=$carId")
        if (route.result_code != 0) {
            throw IllegalStateException("카카오 경로 오류: ${route.result_msg}. carId=$carId")
        }
        val summary = route.summary
            ?: throw IllegalStateException("카카오 경로 요약 없음. carId=$carId")

        val routePoints = route.sections.flatMap { section ->
            section.roads.flatMap { road ->
                road.vertexes.chunked(2)
                    .filter { it.size == 2 }
                    .map { mapOf("lat" to it[1], "lng" to it[0]) }
            }
        }

        controlServiceClient.sendRelocateCommand(
            carId = carId,
            route = routePoints,
            distanceMeters = summary.distance,
            durationSeconds = summary.duration,
        )

        log.info("재배치 명령 전송 완료. carId={}, standId={}, points={}", carId, dest.targetStandId, routePoints.size)
    }
}
