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

@Service
class DispatchExportService(
    private val dispatchJpaRepository: DispatchJpaRepository
) {

    @Transactional(readOnly = true)
    fun exportDailyDispatchDataToTempFile(): File {
        val endOfPeriod = OffsetDateTime.now(ZoneId.of("Asia/Seoul"))
        val startOfPeriod = endOfPeriod.minusHours(24)
        val tempFile = File.createTempFile("dispatch_export_", ".csv.gz")

        try {
            tempFile.outputStream().use { fos ->
                GZIPOutputStream(fos).use { gzipOs ->
                    BufferedWriter(OutputStreamWriter(gzipOs, Charsets.UTF_8)).use { writer ->
                        // Write CSV Header
                        writer.write("requested_at,request_id,user_id,start_latitude,start_longitude,end_latitude,end_longitude,status\n")

                        // Fetch data as stream
                        dispatchJpaRepository.streamAllByCreatedAtBetween(startOfPeriod, endOfPeriod).use { stream ->
                            stream.forEach { dto ->
                                val row = buildString {
                                    append(dto.createdAt).append(",")
                                    append(dto.requestId).append(",")
                                    append(dto.userId).append(",")
                                    append(dto.startLatitude).append(",")
                                    append(dto.startLongitude).append(",")
                                    append(dto.endLatitude).append(",")
                                    append(dto.endLongitude).append(",")
                                    append(dto.status.name)
                                    append("\n")
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
