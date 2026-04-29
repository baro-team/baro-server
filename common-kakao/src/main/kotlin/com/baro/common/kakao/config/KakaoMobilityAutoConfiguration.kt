package com.baro.common.kakao.config

import com.baro.common.kakao.mobility.KakaoMobilityClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(KakaoMobilityProperties::class)
class KakaoMobilityAutoConfiguration {
    @Bean
    fun kakaoMobilityClient(properties: KakaoMobilityProperties): KakaoMobilityClient =
        KakaoMobilityClient(properties)
}
