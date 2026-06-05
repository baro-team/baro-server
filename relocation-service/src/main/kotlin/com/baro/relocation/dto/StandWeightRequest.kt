package com.baro.relocation.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class StandWeightRequest(
    val weights: List<StandWeightDto>
)

data class StandWeightDto(
    @JsonProperty("stand_id")
    val standId: String,
    @JsonProperty("time_zone")
    val timeZone: Int,
    @JsonProperty("day_of_week")
    val dayOfWeek: Int,
    val weight: Double
)