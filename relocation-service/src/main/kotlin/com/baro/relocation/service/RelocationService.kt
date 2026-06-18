package com.baro.relocation.service

import com.baro.relocation.dto.RelocationAssignRequest
import com.baro.relocation.dto.RelocationAssignResponse
import com.baro.relocation.entity.StandWeight
import com.baro.relocation.repository.StandWeightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.*

@Service
@Transactional(readOnly = true) // 데이터베이스 데이터를 읽기만 하므로 읽기 전용 트랜잭션을 사용하여 성능을 최적화합니다.
class RelocationService(
    private val standWeightRepository : StandWeightRepository
) {
    fun assignRelocation(request: RelocationAssignRequest): RelocationAssignResponse {

        //  조회 기준 변경 예정
        val weights = standWeightRepository.findAll()

        // DB에 가중치 데이터가 없을 경우 예외처리
        if (weights.isEmpty()) {
            throw IllegalStateException("재배치 가능한 정거장 데이터가 없습니다.")
        }

        // 차량 현재 위치 <-> 정류장들 거리 계산
        val mappedWeights = weights.map { stand ->
            stand to calculateHaversineDistance(request.currentLat, request.currentLon, stand.latitude, stand.longitude)
        }

        // 먼저 10,000m (10km) 이내인 정거장들만 필터링합니다.
        val within10km = mappedWeights.filter { it.second <= 10000.0 }

        val targetList = if (within10km.isNotEmpty()) {
            within10km
        } else {
            // 10km 이내에 없으면 30km로 확장
            val within30km = mappedWeights.filter { it.second <= 30000.0 }
            // 그래도 없으면 전체 조회
            if (within30km.isNotEmpty()) within30km else mappedWeights
        }

        // 정규화를 위한 최대/최소값 계산
        val maxWeight = targetList.maxOf { it.first.weight }
        val minWeight = targetList.minOf { it.first.weight }
        val maxDist = targetList.maxOf { it.second }
        val minDist = targetList.minOf { it.second }

        var bestStand: StandWeight? = null
        var bestScore = Double.NEGATIVE_INFINITY

        // 스코어링 (최종 점수 = 정규화된 가중치 - 정규화된 거리)
        // 가중치(70%) 거리(30%)
        val weightW1 = 0.7
        val distanceW2 = 0.3

        for ((stand, distance) in targetList) {
            // Min-Max 정규화 공식: (현재값 - 최소값) / (최대값 - 최소값)
            // 최대값과 최소값 동이랗면 1.0으로 통일 (정거장이 1개 or 가중치가 모두 같다면)
            val normWeight = if (maxWeight == minWeight) 1.0 else (stand.weight - minWeight) / (maxWeight - minWeight)
            val normDist = if (maxDist == minDist) 1.0 else (distance - minDist) / (maxDist - minDist)
            
            // 점수 계산: 가중치는 더하고, 거리는 페널티
            val score = (normWeight * weightW1) - (normDist * distanceW2)
            
            // 최고 점수보다 높으면 갱신
            if (score > bestScore) {
                bestScore = score
                bestStand = stand
            }
        }

        // bestStand 비어있으면, 가장 첫 번째 정거장 임시 선택
        val finalStand = bestStand ?: targetList.first().first

        return RelocationAssignResponse(
            carId = request.carId,
            targetStandId = finalStand.standId,
            targetLat = finalStand.latitude,
            targetLon = finalStand.longitude
        )
    }


    /**
     * 하버사인 공식 : 두 좌표 사이의 직선 거리를 미터(m) 단위로 계산
     */
    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // 지구의 평균 반지름 (단위: 미터)
        
        // 위도, 경도 Degree에서 Radian으로 변환
        val phi1 = lat1 * PI / 180
        val phi2 = lat2 * PI / 180
        val deltaPhi = (lat2 - lat1) * PI / 180
        val deltaLambda = (lon2 - lon1) * PI / 180

        // 두 점 사이의 호의 길이 구하기
        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)
        
        // 아크탄젠트로 최종 중심각을 구하기
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // 반지름에 중심각을 곱해 최종 미터(m) 거리를 반환
        return r * c
    }
}