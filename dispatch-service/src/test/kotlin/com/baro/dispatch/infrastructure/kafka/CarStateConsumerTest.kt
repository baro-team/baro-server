package com.baro.dispatch.infrastructure.kafka

import com.baro.dispatch.application.port.out.DispatchableCarProjection
import com.baro.dispatch.application.port.out.DispatchableCarCandidate
import com.baro.dispatch.application.service.CarStateCommand
import com.baro.dispatch.application.service.CarStateService
import com.baro.dispatch.application.service.VehicleLocationStreamService
import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.repository.DispatchRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class CarStateConsumerTest {

    @Test
    fun `차량 상태 메시지를 수신하면 처리 서비스에 명령을 전달한다`() {
        var received: CarStateCommand? = null
        val consumer = CarStateConsumer(
            object : CarStateService(object : DispatchableCarProjection {
                override fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double) = Unit
                override fun removeCar(carId: Long) = Unit
                override fun findNearestIdleCar(latitude: Double, longitude: Double): DispatchableCarCandidate? = null
            }, noopVehicleLocationStreamService()) {
                override fun handle(command: CarStateCommand) {
                    received = command
                }
            },
        )

        consumer.consume(
            CarStateMessage(
                carId = 101L,
                latitude = 37.5,
                longitude = 127.0,
                speed = 40,
                battery = 88,
                heading = 123.4,
                status = CarStatus.IDLE,
                timestamp = "2026-06-04T10:00:00Z",
            ),
            carIdKey = "101",
        )

        assertEquals(
            CarStateCommand(
                carIdKey = "101",
                carId = 101L,
                latitude = 37.5,
                longitude = 127.0,
                speed = 40,
                battery = 88,
                heading = 123.4,
                status = CarStatus.IDLE,
                timestamp = "2026-06-04T10:00:00Z",
            ),
            received,
        )
    }

    @Test
    fun `배차 가능 상태가 아닌 차량 상태 메시지를 수신해도 처리 서비스에 전달한다`() {
        var received: CarStateCommand? = null
        val consumer = CarStateConsumer(
            object : CarStateService(object : DispatchableCarProjection {
                override fun saveIdleCarLocation(carId: Long, latitude: Double, longitude: Double) = Unit
                override fun removeCar(carId: Long) = Unit
                override fun findNearestIdleCar(latitude: Double, longitude: Double): DispatchableCarCandidate? = null
            }, noopVehicleLocationStreamService()) {
                override fun handle(command: CarStateCommand) {
                    received = command
                }
            },
        )

        consumer.consume(
            CarStateMessage(
                carId = 101L,
                latitude = 37.5,
                longitude = 127.0,
                speed = 40,
                battery = 88,
                heading = 123.4,
                status = CarStatus.MOVING_TO_PICKUP,
                timestamp = "2026-06-04T10:00:00Z",
            ),
            carIdKey = "101",
        )

        assertEquals(CarStatus.MOVING_TO_PICKUP, received?.status)
    }

    private fun noopVehicleLocationStreamService() = VehicleLocationStreamService(object : DispatchRepository {
        override fun save(dispatch: Dispatch): Long = 0L
        override fun update(dispatch: Dispatch) = Unit
        override fun findById(dispatchId: Long): Dispatch? = null
        override fun findActiveByCarId(carId: Long): Dispatch? = null
    })
}
