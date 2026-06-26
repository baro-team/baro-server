package com.baro.relocation.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "stand_weight")
data class StandWeight(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "stand_id", nullable = false)
    val standId: String,

    @Column(name = "time_zone", nullable = false)
    val timeZone: Int,

    @Column(name = "day_of_week", nullable = false)
    val dayOfWeek: Int,

    @Column(name = "weight", nullable = false)
    val weight: Double,

    @Column(name = "latitude", nullable = false)
    val latitude: Double,

    @Column(name = "longitude", nullable = false)
    val longitude: Double,

    @Column(columnDefinition = "geography(Point, 4326)")
    val geom: org.locationtech.jts.geom.Point? = null,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()

)
