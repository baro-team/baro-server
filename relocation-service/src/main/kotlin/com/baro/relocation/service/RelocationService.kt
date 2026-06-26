package com.baro.relocation.service

import com.baro.relocation.dto.RelocationAssignRequest
import com.baro.relocation.dto.RelocationAssignResponse
import com.baro.relocation.entity.StandWeight
import com.baro.relocation.repository.StandWeightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.*

@Service
@Transactional(readOnly = true)
class RelocationService(
    private val standWeightRepository : StandWeightRepository
) {
    fun assignRelocation(request: RelocationAssignRequest): RelocationAssignResponse {

        class TargetStand(val standId: String, val weight: Double, val distance: Double, val latitude: Double, val longitude: Double)
        var targetList: List<TargetStand>

        if (request.usePostGis) {
            // PostGIS ST_DWithin을 사용하여 반경 10km 이내의 정거장 조회
            var dbResult = standWeightRepository.findWithinDistance(request.currentLon, request.currentLat, 10000.0)
            if (dbResult.isEmpty()) {
                dbResult = standWeightRepository.findWithinDistance(request.currentLon, request.currentLat, 30000.0)
            }
            targetList = dbResult.map { TargetStand(it.standId, it.weight, it.distance, it.latitude, it.longitude) }
        } else {
            // 어플리케이션 단에서 하버사인 연산
            val allStands = standWeightRepository.findAll()
            targetList = allStands.map { stand ->
                val dist = calculateHaversine(request.currentLat, request.currentLon, stand.latitude, stand.longitude)
                TargetStand(stand.standId, stand.weight, dist, stand.latitude, stand.longitude)
            }.filter { it.distance <= 10000.0 }
            
            if (targetList.isEmpty()) {
                targetList = allStands.map { stand ->
                    val dist = calculateHaversine(request.currentLat, request.currentLon, stand.latitude, stand.longitude)
                    TargetStand(stand.standId, stand.weight, dist, stand.latitude, stand.longitude)
                }.filter { it.distance <= 30000.0 }
            }
        }

        // DB에 가중치 데이터가 전혀 없거나 반경 30km 내에도 없을 경우 예외처리
        if (targetList.isEmpty()) {
            throw IllegalStateException("재배치 가능한 정거장 데이터가 없습니다.")
        }

        // 정규화를 위한 최대/최소값 계산
        var maxWeight = Double.NEGATIVE_INFINITY
        var minWeight = Double.POSITIVE_INFINITY
        var maxDist = Double.NEGATIVE_INFINITY
        var minDist = Double.POSITIVE_INFINITY

        for (stand in targetList) {
            val distance = stand.distance
            if (stand.weight > maxWeight) maxWeight = stand.weight
            if (stand.weight < minWeight) minWeight = stand.weight
            if (distance > maxDist) maxDist = distance
            if (distance < minDist) minDist = distance
        }

        var bestStandId: String? = null
        var bestLat: Double = 0.0
        var bestLon: Double = 0.0
        var bestScore = Double.NEGATIVE_INFINITY

        // 스코어링 (최종 점수 = 정규화된 가중치 - 정규화된 거리)
        // 가중치(70%) 거리(30%)
        val weightW1 = 0.7
        val distanceW2 = 0.3

        for (stand in targetList) {
            val distance = stand.distance
            // Min-Max 정규화 : (현재값 - 최소값) / (최대값 - 최소값)
            // 최대값과 최소값 동일하면 1.0으로 통일 (정거장이 1개 or 가중치가 모두 같은 경우)
            val normWeight = if (maxWeight == minWeight) 1.0 else (stand.weight - minWeight) / (maxWeight - minWeight)
            val normDist = if (maxDist == minDist) 1.0 else (distance - minDist) / (maxDist - minDist)
            
            // 점수 계산: 가중치는 더하고, 거리는 페널티
            val score = (normWeight * weightW1) - (normDist * distanceW2)
            
            // 최고 점수보다 높으면 갱신
            if (score > bestScore) {
                bestScore = score
                bestStandId = stand.standId
                bestLat = stand.latitude
                bestLon = stand.longitude
            }
        }

        // bestStandId 비어있으면, 가장 첫 번째 정거장 임시 선택
        if (bestStandId == null) {
            val finalStand = targetList.first()
            bestStandId = finalStand.standId
            bestLat = finalStand.latitude
            bestLon = finalStand.longitude
        }

        return RelocationAssignResponse(
            carId = request.carId,
            targetStandId = bestStandId,
            targetLat = bestLat,
            targetLon = bestLon
        )
    }

    // 하버사인 거리 계산 함수
    private fun calculateHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // 지구 반지름
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
}