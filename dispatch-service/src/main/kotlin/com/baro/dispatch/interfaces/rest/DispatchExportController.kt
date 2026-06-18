package com.baro.dispatch.interfaces.rest

import com.baro.dispatch.application.service.DispatchExportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Dispatch Export API", description = "배차 데이터 추출 관련 API")
@RestController
@RequestMapping(DispatchApiPaths.INTERNAL_DISPATCH)
class DispatchExportController(
    private val dispatchExportService: DispatchExportService
) {

    @Operation(summary = "24시간 배차 데이터 추출", description = "최근 24시간 동안의 배차 데이터를 압축된 CSV 파일 형태로 실시간 스트리밍 다운로드")
    @GetMapping(DispatchApiPaths.EXPORT_DAILY)
    fun exportDailyDispatchData(response: HttpServletResponse) {
        response.contentType = "application/gzip"
        response.setHeader("Content-Disposition", "attachment; filename=\"dispatches_last_24h.csv.gz\"")
        
        val tempFile = dispatchExportService.exportDailyDispatchDataToTempFile()
        
        try {
            tempFile.inputStream().use { inputStream ->
                inputStream.copyTo(response.outputStream)
            }
        } finally {
            response.outputStream.flush()
            tempFile.delete()
        }
    }
}
