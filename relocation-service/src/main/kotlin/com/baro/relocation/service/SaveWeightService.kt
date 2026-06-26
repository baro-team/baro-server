package com.baro.relocation.service

import com.baro.relocation.dto.StandWeightRequest
import com.baro.relocation.entity.StandWeight
import com.baro.relocation.repository.StandWeightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.PrecisionModel

@Service
@Transactional
class SaveWeightService(
    private val standWeightRepository : StandWeightRepository
) {
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    fun saveWeights(request: StandWeightRequest) {
        // 기존 가중치 데이터 삭제 (새 데이터로 전체 갱신)
        standWeightRepository.deleteAllInBatch()

        // 새 가중치 저장 api
        val entities = request.weights.map { dto ->
            StandWeight(
                standId = dto.standId,
                timeZone = dto.timeZone,
                dayOfWeek = dto.dayOfWeek,
                weight = dto.weight,
                longitude = dto.longitude,
                latitude = dto.latitude,
                geom = geometryFactory.createPoint(Coordinate(dto.longitude, dto.latitude)),
                updatedAt = LocalDateTime.now(),
            )
        }

        standWeightRepository.saveAll(entities)
    }

}