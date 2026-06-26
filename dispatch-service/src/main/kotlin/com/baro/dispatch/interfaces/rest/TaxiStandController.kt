package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.infrastructure.persistence.TaxiStandJpaRepository
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TaxiStandResponse(
    val id: String,
    val longitude: Double,
    val latitude: Double,
    val name: String?,
    val district: String?,
)

data class StandsPageResponse(
    val content: List<TaxiStandResponse>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
)

@RestController
@RequestMapping("/dispatch/stands")
class TaxiStandController(
    private val taxiStandRepository: TaxiStandJpaRepository,
) {
    @GetMapping
    fun getStands(
        @PageableDefault(size = 500, sort = ["id"]) pageable: Pageable,
    ): StandsPageResponse {
        val page = taxiStandRepository.findAll(pageable)
        return StandsPageResponse(
            content = page.content.map {
                TaxiStandResponse(
                    id = it.id,
                    longitude = it.longitude,
                    latitude = it.latitude,
                    name = it.name,
                    district = it.district,
                )
            },
            pageNumber = page.number,
            pageSize = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }
}
