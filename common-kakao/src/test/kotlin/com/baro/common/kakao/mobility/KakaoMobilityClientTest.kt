package com.baro.common.kakao.mobility

import com.baro.common.core.exception.ExternalServiceException
import com.baro.common.kakao.config.KakaoMobilityProperties
import com.baro.common.kakao.mobility.directions.KakaoMobilityDirectionPoint
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KakaoMobilityClientTest {
    @Test
    fun `API 키가 없으면 카카오 API를 호출하지 않고 예외를 던진다`() {
        val client = KakaoMobilityClient(KakaoMobilityProperties(apiKey = ""))

        val exception = assertFailsWith<ExternalServiceException> {
            client.findDirections(
                origin = KakaoMobilityDirectionPoint(longitude = 127.1, latitude = 37.4),
                destination = KakaoMobilityDirectionPoint(longitude = 127.2, latitude = 37.5),
            )
        }

        assertEquals("카카오모빌리티 API 키가 설정되지 않았습니다.", exception.message)
    }

    @Test
    fun `길찾기 요청은 카카오 API 경로와 필수 쿼리 헤더를 포함한다`() {
        val builder = RestClient.builder().baseUrl("https://kakao.test")
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KakaoMobilityClient(
            properties = KakaoMobilityProperties(baseUrl = "https://kakao.test", apiKey = "test-api-key"),
            client = builder.build(),
        )
        server.expect { request ->
            assertEquals(HttpMethod.GET, request.method)
            assertEquals(KakaoMobilityApiPaths.DIRECTIONS, request.uri.path)
            assertEquals("KakaoAK test-api-key", request.headers.getFirst(HttpHeaders.AUTHORIZATION))

            val queryParams = UriComponentsBuilder.fromUri(request.uri).build().queryParams
            assertEquals("127.1,37.4", queryParams.getFirst("origin"))
            assertEquals("127.2,37.5", queryParams.getFirst("destination"))
            assertEquals("RECOMMEND", queryParams.getFirst("priority"))
            assertEquals("false", queryParams.getFirst("alternatives"))
            assertEquals("false", queryParams.getFirst("road_details"))
            assertEquals("false", queryParams.getFirst("summary"))
        }.andRespond(
            withSuccess(
                """
                {
                  "routes": [
                    {
                      "result_code": 0,
                      "result_msg": "길찾기 성공",
                      "summary": {
                        "fare": {"taxi": 12100, "toll": 0},
                        "distance": 13840,
                        "duration": 2701
                      },
                      "sections": [
                        {
                          "roads": [
                            {"vertexes": [127.1, 37.4, 127.2, 37.5]}
                          ]
                        }
                      ]
                    }
                  ]
                }
                """.trimIndent(),
                MediaType.APPLICATION_JSON,
            ),
        )

        val response = client.findDirections(
            origin = KakaoMobilityDirectionPoint(longitude = 127.1, latitude = 37.4),
            destination = KakaoMobilityDirectionPoint(longitude = 127.2, latitude = 37.5),
        )

        server.verify()
        assertEquals(1, response.routes.size)
        assertEquals(0, response.routes.first().result_code)
        assertEquals("길찾기 성공", response.routes.first().result_msg)
        assertEquals(12100, response.routes.first().summary?.fare?.taxi)
        assertEquals(13840, response.routes.first().summary?.distance)
        assertEquals(2701, response.routes.first().summary?.duration)
        assertEquals(listOf(127.1, 37.4, 127.2, 37.5), response.routes.first().sections.first().roads.first().vertexes)
    }

    @Test
    fun `카카오 API가 빈 응답을 반환하면 예외를 던진다`() {
        val builder = RestClient.builder().baseUrl("https://kakao.test")
        val server = MockRestServiceServer.bindTo(builder).build()
        val client = KakaoMobilityClient(
            properties = KakaoMobilityProperties(baseUrl = "https://kakao.test", apiKey = "test-api-key"),
            client = builder.build(),
        )
        server.expect { request ->
            assertEquals(KakaoMobilityApiPaths.DIRECTIONS, request.uri.path)
        }.andRespond(withSuccess("", MediaType.APPLICATION_JSON))

        val exception = assertFailsWith<ExternalServiceException> {
            client.findDirections(
                origin = KakaoMobilityDirectionPoint(longitude = 127.1, latitude = 37.4),
                destination = KakaoMobilityDirectionPoint(longitude = 127.2, latitude = 37.5),
            )
        }

        server.verify()
        assertEquals("카카오모빌리티에서 빈 응답을 반환했습니다.", exception.message)
    }
}
