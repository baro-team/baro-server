package com.baro.dispatch.application.service

import com.baro.dispatch.infrastructure.persistence.DispatchJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.zip.GZIPOutputStream
import com.baro.dispatch.infrastructure.persistence.DispatchRequestJpaRepository

@Service
class DispatchExportService(
    private val dispatchJpaRepository: DispatchJpaRepository,
    private val dispatchRequestJpaRepository: DispatchRequestJpaRepository
) {

    @Transactional(readOnly = true)
    fun exportDailyDispatchesToTempFile(): File {
        val endOfPeriod = OffsetDateTime.now(ZoneId.of("Asia/Seoul"))
        val startOfPeriod = endOfPeriod.minusHours(24)
        val tempFile = File.createTempFile("dispatch_export_", ".csv.gz")

        try {
            tempFile.outputStream().use { fos ->
                GZIPOutputStream(fos).use { gzipOs ->
                    BufferedWriter(OutputStreamWriter(gzipOs, Charsets.UTF_8)).use { writer ->
                        // Write CSV Header
                        writer.write("dispatch_id,request_id,user_id,car_id,car_number,stand_id,created_at,estimated_pickup_time,estimated_ride_time,fare,status,pickup_route_path,dropoff_route_path\n")

                        dispatchJpaRepository.streamAllByCreatedAtBetween(startOfPeriod, endOfPeriod).use { stream ->
                            stream.forEach { entity ->
                                val pickupJson = "\"${entity.pickupRoutePath.toString().replace("\"", "\"\"")}\""
                                val dropoffJson = "\"${entity.dropoffRoutePath.toString().replace("\"", "\"\"")}\""
                                val row = buildString {
                                    append(entity.dispatchId).append(",")
                                    append(entity.requestId).append(",")
                                    append(entity.userId).append(",")
                                    append(entity.carId).append(",")
                                    append(entity.carNumber ?: "").append(",")
                                    append(entity.standId).append(",")
                                    append(entity.createdAt).append(",")
                                    append(entity.estimatedPickupTime).append(",")
                                    append(entity.estimatedRideTime).append(",")
                                    append(entity.fare).append(",")
                                    append(entity.status.name).append(",")
                                    append(pickupJson).append(",")
                                    append(dropoffJson).append("\n")
                                }
                                writer.write(row)
                            }
                        }
                        writer.flush()
                    }
                }
            }
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        }
        return tempFile
    }

    @Transactional(readOnly = true)
    fun exportDailyRequestsToTempFile(): File {
        val endOfPeriod = OffsetDateTime.now(ZoneId.of("Asia/Seoul"))
        val startOfPeriod = endOfPeriod.minusHours(24)
        val tempFile = File.createTempFile("dispatch_request_export_", ".csv.gz")

        try {
            tempFile.outputStream().use { fos ->
                GZIPOutputStream(fos).use { gzipOs ->
                    BufferedWriter(OutputStreamWriter(gzipOs, Charsets.UTF_8)).use { writer ->
                        // Write CSV Header
                        writer.write("request_id,user_id,start_latitude,start_longitude,start_location,start_name,end_latitude,end_longitude,end_location,end_name,fare,estimated_time,distance_km,requested_at,updated_at,status,route_path\n")

                        dispatchRequestJpaRepository.streamAllByRequestedAtBetween(startOfPeriod, endOfPeriod).use { stream ->
                            stream.forEach { entity ->
                                val routePathJson = "\"${entity.routePath.toString().replace("\"", "\"\"")}\""
                                val row = buildString {
                                    append(entity.requestId).append(",")
                                    append(entity.userId).append(",")
                                    append(entity.startLatitude).append(",")
                                    append(entity.startLongitude).append(",")
                                    append("\"${entity.startLocation}\"").append(",")
                                    append("\"${entity.startName ?: ""}\"").append(",")
                                    append(entity.endLatitude).append(",")
                                    append(entity.endLongitude).append(",")
                                    append("\"${entity.endLocation}\"").append(",")
                                    append("\"${entity.endName ?: ""}\"").append(",")
                                    append(entity.fare).append(",")
                                    append(entity.estimatedTime).append(",")
                                    append(entity.distanceKm).append(",")
                                    append(entity.requestedAt).append(",")
                                    append(entity.updatedAt).append(",")
                                    append(entity.status.name).append(",")
                                    append(routePathJson).append("\n")
                                }
                                writer.write(row)
                            }
                        }
                        writer.flush()
                    }
                }
            }
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        }
        return tempFile
    }
}
