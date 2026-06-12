package com.baro.dispatch.application.service

import com.baro.dispatch.infrastructure.persistence.DispatchRequestJpaRepository
import com.baro.dispatch.domain.model.DispatchRequestStatus
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.time.OffsetDateTime
import java.util.zip.GZIPOutputStream

@Service
class DispatchExportService(
    private val dispatchRequestJpaRepository: DispatchRequestJpaRepository,
    private val entityManager: EntityManager
) {

    @Transactional(readOnly = true)
    fun exportDailyDispatchDataToTempFile(): File {
        val yesterday = OffsetDateTime.now().minusHours(24)
        val tempFile = File.createTempFile("dispatch_export_", ".csv.gz")

        tempFile.outputStream().use { fos ->
            GZIPOutputStream(fos).use { gzipOs ->
                BufferedWriter(OutputStreamWriter(gzipOs, Charsets.UTF_8)).use { writer ->
                    // Write CSV Header
                    writer.write("requested_at,request_id,user_id,start_latitude,start_longitude,end_latitude,end_longitude,status\n")

                    // Fetch data as stream
                    dispatchRequestJpaRepository.streamAllByRequestedAtAfterAndStatus(yesterday, DispatchRequestStatus.COMPLETED).use { stream ->
                        stream.forEach { dispatchRequest ->
                            val row = buildString {
                                append(dispatchRequest.requestedAt).append(",")
                                append(dispatchRequest.requestId).append(",")
                                append(dispatchRequest.userId).append(",")
                                append(dispatchRequest.startLatitude).append(",")
                                append(dispatchRequest.startLongitude).append(",")
                                append(dispatchRequest.endLatitude).append(",")
                                append(dispatchRequest.endLongitude).append(",")
                                append(dispatchRequest.status.name)
                                append("\n")
                            }
                            writer.write(row)
                            entityManager.detach(dispatchRequest)
                        }
                    }
                    writer.flush()
                }
            }
        }
        return tempFile
    }
}
