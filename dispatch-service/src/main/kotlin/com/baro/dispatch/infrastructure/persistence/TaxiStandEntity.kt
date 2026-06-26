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
    var longitude: Double = 0.0,

    @Column(name = "latitude", nullable = false)
    var latitude: Double = 0.0,

    @Column(name = "district")
    var district: String? = null,

    @Column(name = "road_address")
    var roadAddress: String? = null,

    @Column(name = "extra_road")
    var extraRoad: String? = null,

    @Column(name = "name")
    var name: String? = null,
)
