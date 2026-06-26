package com.baro.dispatch.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "taxi_stands")
class TaxiStandEntity(
    @Id
    @Column(name = "id")
    val id: String = "",

    @Column(name = "longitude", nullable = false)
    val longitude: Double = 0.0,

    @Column(name = "latitude", nullable = false)
    val latitude: Double = 0.0,

    @Column(name = "district")
    val district: String? = null,

    @Column(name = "road_address")
    val roadAddress: String? = null,

    @Column(name = "extra_road")
    val extraRoad: String? = null,

    @Column(name = "name")
    val name: String? = null,
)
