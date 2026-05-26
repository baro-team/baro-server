package com.baro.dispatch.infrastructure.persistence

import com.baro.dispatch.domain.model.DispatchRequest
import com.baro.dispatch.domain.model.DispatchRequestStatus
import com.baro.dispatch.domain.model.GeoPoint
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.ColumnTransformer
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime

@Entity
@Table(name = "dispatch_request")
class DispatchRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    var requestId: Long? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,

    @Column(name = "start_latitude", nullable = false)
    var startLatitude: Double = 0.0,

    @Column(name = "start_longitude", nullable = false)
    var startLongitude: Double = 0.0,

    @ColumnTransformer(write = "?::point")
    @Column(name = "start_location", columnDefinition = "point", nullable = false)
    var startLocation: String = "(0.0,0.0)",

    @Column(name = "start_name")
    var startName: String? = null,

    @Column(name = "end_latitude", nullable = false)
    var endLatitude: Double = 0.0,

    @Column(name = "end_longitude", nullable = false)
    var endLongitude: Double = 0.0,

    @ColumnTransformer(write = "?::point")
    @Column(name = "end_location", columnDefinition = "point", nullable = false)
    var endLocation: String = "(0.0,0.0)",

    @Column(name = "end_name")
    var endName: String? = null,

    @Column(nullable = false)
    var fare: Int = 0,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "route_path", nullable = false)
    var routePath: List<List<Double>> = emptyList(),

    @Column(name = "estimated_time", nullable = false)
    var estimatedTime: Int = 0,

    @Column(name = "distance_km", nullable = false)
    var distanceKm: Double = 0.0,

    @Column(name = "requested_at", nullable = false)
    var requestedAt: OffsetDateTime = OffsetDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_request_status", nullable = false)
    var status: DispatchRequestStatus = DispatchRequestStatus.PENDING,
) {
    companion object {
        fun from(request: DispatchRequest): DispatchRequestEntity =
            DispatchRequestEntity(
                userId = request.userId,
                startLatitude = request.origin.latitude,
                startLongitude = request.origin.longitude,
                startLocation = request.origin.toPointLiteral(),
                startName = request.origin.name,
                endLatitude = request.destination.latitude,
                endLongitude = request.destination.longitude,
                endLocation = request.destination.toPointLiteral(),
                endName = request.destination.name,
                fare = request.fare,
                routePath = request.routePath.map { listOf(it.longitude, it.latitude) },
                estimatedTime = request.estimatedTime,
                distanceKm = request.distanceKm,
                requestedAt = request.requestedAt,
                updatedAt = request.updatedAt,
                status = request.status,
            )

        private fun GeoPoint.toPointLiteral(): String = "($longitude,$latitude)"
    }
}
