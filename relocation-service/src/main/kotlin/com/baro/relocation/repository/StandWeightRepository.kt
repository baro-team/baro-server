package com.baro.relocation.repository

import com.baro.relocation.entity.StandWeight
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StandWeightDistanceProjection {
    val standId: String
    val weight: Double
    val latitude: Double
    val longitude: Double
    val distance: Double
}

interface StandWeightRepository : JpaRepository<StandWeight, Long> {
    @Query(value = """
        SELECT stand_id as standId, weight, latitude, longitude, 
               ST_DistanceSphere(geom, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)) as distance
        FROM stand_weight 
        WHERE ST_DWithin(geom::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :distance)
    """, nativeQuery = true)
    fun findWithinDistance(
        @Param("lon") lon: Double, 
        @Param("lat") lat: Double, 
        @Param("distance") distance: Double
    ): List<StandWeightDistanceProjection>
}