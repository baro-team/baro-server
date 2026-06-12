package com.baro.dispatch.application.service

import com.baro.dispatch.infrastructure.persistence.DispatchJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.time.OffsetDateTime
import java.util.zip.GZIPOutputStream

@Service
class DispatchExportService(
    private val dispatchJpaRepository: DispatchJpaRepository
) {

    @Transactional(readOnly = true)
    fun exportDailyDispatchDataAsGzippedCsv(outputStream: OutputStream) {
        val yesterday = OffsetDateTime.now().minusHours(24)

        GZIPOutputStream(outputStream).use { gzipOs ->
            BufferedWriter(OutputStreamWriter(gzipOs, Charsets.UTF_8)).use { writer ->
                // Write CSV Header
                writer.write("dispatch_id,request_id,user_id,car_id,stand_id,created_at,estimated_pickup_time,estimated_ride_time,fare,status\n")

                // Fetch data as stream
                dispatchJpaRepository.streamAllByCreatedAtAfter(yesterday).use { stream ->
                    stream.forEach { dispatch ->
                        val row = buildString {
                            append(dispatch.dispatchId).append(",")
                            append(dispatch.requestId).append(",")
                            append(dispatch.userId).append(",")
                            append(dispatch.carId).append(",")
                            append(dispatch.standId).append(",")
                            append(dispatch.createdAt).append(",")
                            append(dispatch.estimatedPickupTime).append(",")
                            append(dispatch.estimatedRideTime).append(",")
                            append(dispatch.fare).append(",")
                            append(dispatch.status)
                            append("\n")
                        }
                        writer.write(row)
                    }
                }
                writer.flush()
            }
        }
    }
}
