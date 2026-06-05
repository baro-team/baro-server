package com.baro.relocation.service

import com.baro.relocation.dto.StandWeightRequest
import com.baro.relocation.entity.StandWeight
import com.baro.relocation.repository.StandWeightRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class RelocationService(
    private val standWeightRepository : StandWeightRepository
) {

    fun saveWeights(request: StandWeightRequest) {
        // 새 가중치 저장
        val entities = request.weights.map { dto ->
            StandWeight(
                standId = dto.standId,
                timeZone = dto.timeZone,
                dayOfWeek = dto.dayOfWeek,
                weight = dto.weight,
                updatedAt = LocalDateTime.now()
            )
        }

        standWeightRepository.saveAll(entities)
        println("[RelocationService] 가중치 저장 완료: ${entities.size}건")
    }
}