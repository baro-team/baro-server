package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.infrastructure.persistence.TaxiStandJpaRepository
import org.springframework.data.domain.Page
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

@RestController
@RequestMapping("/dispatch/stands")
class TaxiStandController(
    private val taxiStandRepository: TaxiStandJpaRepository,
) {
    @GetMapping
    fun getStands(
        @PageableDefault(size = 500, sort = ["id"]) pageable: Pageable,
    ): Page<TaxiStandResponse> =
        taxiStandRepository.findAll(pageable).map {
            TaxiStandResponse(
                id = it.id,
                longitude = it.longitude,
                latitude = it.latitude,
                name = it.name,
                district = it.district,
            )
        }
}
