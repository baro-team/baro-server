package com.baro.common.web.config

import com.baro.common.web.interceptor.InternalSecurityInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

import org.springframework.context.annotation.Import

@Configuration
@Import(InternalSecurityInterceptor::class)
class InternalWebMvcConfig(
    private val internalSecurityInterceptor: InternalSecurityInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(internalSecurityInterceptor)
            .addPathPatterns("/internal/**")
    }
}
