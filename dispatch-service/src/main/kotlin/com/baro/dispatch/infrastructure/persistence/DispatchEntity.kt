package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.Dispatch
import com.baro.dispatch.domain.model.DispatchStatus
import com.baro.dispatch.domain.model.GeoPoint
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val DEFAULT_OFFSET_DATE_TIME: OffsetDateTime = OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)

@Entity
@Table(
    name = "dispatch",
    uniqueConstraints = [UniqueConstraint(name = "uk_dispatch_request_id", columnNames = ["request_id"])],
)
class DispatchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispatch_id")
    var dispatchId: Long? = null,

    @Column(name = "request_id", nullable = false)
    var requestId: Long = 0,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(name = "car_id", nullable = false)
    var carId: Long = 0,

    @Column(name = "car_number")
    var carNumber: String? = null,

    @Column(name = "stand_id", nullable = false)
    var standId: Long = 0,

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = DEFAULT_OFFSET_DATE_TIME,

    @Column(name = "estimated_pickup_time", nullable = false)
    var estimatedPickupTime: Int = 0,

    @Column(name = "estimated_ride_time", nullable = false)
    var estimatedRideTime: Int = 0,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "pickup_route_path", nullable = false)
    var pickupRoutePath: List<List<Double>> = emptyList(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dropoff_route_path", nullable = false)
    var dropoffRoutePath: List<List<Double>> = emptyList(),

    @Column(nullable = false)
    var fare: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_status", nullable = false)
    var status: DispatchStatus = DispatchStatus.REQUESTED,
) {
    fun toDomain(): Dispatch = Dispatch(
        id = dispatchId,
        requestId = requestId,
        userId = userId,
        carId = carId,
        carNumber = carNumber,
        standId = standId,
        createdAt = createdAt,
        estimatedPickupTime = estimatedPickupTime,
        estimatedRideTime = estimatedRideTime,
        pickupRoutePath = pickupRoutePath.toGeoPoints(),
        dropoffRoutePath = dropoffRoutePath.toGeoPoints(),
        fare = fare,
        status = status,
    )

    companion object {
        fun from(dispatch: Dispatch): DispatchEntity =
            DispatchEntity(
                dispatchId = dispatch.id,
                requestId = dispatch.requestId,
                userId = dispatch.userId,
                carId = dispatch.carId,
                carNumber = dispatch.carNumber,
                standId = dispatch.standId,
                createdAt = dispatch.createdAt,
                estimatedPickupTime = dispatch.estimatedPickupTime,
                estimatedRideTime = dispatch.estimatedRideTime,
                pickupRoutePath = dispatch.pickupRoutePath.toRoutePath(),
                dropoffRoutePath = dispatch.dropoffRoutePath.toRoutePath(),
                fare = dispatch.fare,
                status = dispatch.status,
            )

        private fun List<GeoPoint>.toRoutePath(): List<List<Double>> = map { listOf(it.longitude, it.latitude) }
        private fun List<List<Double>>.toGeoPoints(): List<GeoPoint> =
            filter { it.size == 2 }.map { GeoPoint(longitude = it[0], latitude = it[1]) }
    }
}
