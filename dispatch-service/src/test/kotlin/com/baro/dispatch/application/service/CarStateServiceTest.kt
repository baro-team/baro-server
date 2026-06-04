package com.baro.dispatch.application.service

import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.DispatchableCarCandidate
import com.baro.dispatch.infrastructure.kafka.CarStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class CarStateServiceTest {

    @Test
    fun `대기 상태 차량은 Redis GEO에 저장한다`() {
        val calls = mutableListOf<String>()
        val service = CarStateService(object : DispatchableCarProjection {
            override fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double) {
                calls += "save:$carId:$latitude:$longitude"
            }

            override fun removeCar(carId: Long) {
                calls += "remove:$carId"
            }

            override fun findNearestIdleCar(latitude: Double, longitude: Double, radiusKm: Double): DispatchableCarCandidate? = null
        })

        service.handle(
            CarStateCommand(
                carIdKey = null,
                carId = 11L,
                latitude = 37.1,
                longitude = 127.2,
                speed = 0,
                battery = 90,
                heading = 0.0,
                status = CarStatus.IDLE,
                timestamp = "2026-06-04T10:00:00Z",
            ),
        )

        assertEquals(listOf("save:11:37.1:127.2"), calls)
    }

    @Test
    fun `이동 중인 차량은 배차 가능 목록에서 제거한다`() {
        val calls = mutableListOf<String>()
        val service = CarStateService(object : DispatchableCarProjection {
            override fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double) {
                calls += "save:$carId:$latitude:$longitude"
            }

            override fun removeCar(carId: Long) {
                calls += "remove:$carId"
            }

            override fun findNearestIdleCar(latitude: Double, longitude: Double, radiusKm: Double): DispatchableCarCandidate? = null
        })

        service.handle(
            CarStateCommand(
                carIdKey = null,
                carId = 11L,
                latitude = 37.1,
                longitude = 127.2,
                speed = 30,
                battery = 90,
                heading = 0.0,
                status = CarStatus.DRIVING,
                timestamp = "2026-06-04T10:00:00Z",
            ),
        )

        assertEquals(listOf("remove:11"), calls)
    }

    @Test
    fun `재배치 중인 차량은 Redis GEO에 저장한다`() {
        val calls = mutableListOf<String>()
        val service = CarStateService(object : DispatchableCarProjection {
            override fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double) {
                calls += "save:$carId:$latitude:$longitude"
            }

            override fun removeCar(carId: Long) {
                calls += "remove:$carId"
            }

            override fun findNearestIdleCar(latitude: Double, longitude: Double, radiusKm: Double): DispatchableCarCandidate? = null
        })

        service.handle(
            CarStateCommand(
                carIdKey = null,
                carId = 11L,
                latitude = 37.1,
                longitude = 127.2,
                speed = 30,
                battery = 90,
                heading = 0.0,
                status = CarStatus.RELOCATING,
                timestamp = "2026-06-04T10:00:00Z",
            ),
        )

        assertEquals(listOf("save:11:37.1:127.2"), calls)
    }
}
