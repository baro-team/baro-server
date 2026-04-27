package com.baro.dispatch.domain.model

data class GeoPoint(
    val longitude: Double,
    val latitude: Double,
    val name: String? = null,
) {
    init {
        require(longitude in MIN_LONGITUDE..MAX_LONGITUDE) { "경도는 -180도 이상 180도 이하여야 합니다." }
        require(latitude in MIN_LATITUDE..MAX_LATITUDE) { "위도는 -90도 이상 90도 이하여야 합니다." }
    }

    private companion object {
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
    }
}
