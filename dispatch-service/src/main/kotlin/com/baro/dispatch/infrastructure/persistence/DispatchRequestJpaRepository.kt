package com.baro.dispatch.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface DispatchRequestJpaRepository : JpaRepository<DispatchRequestEntity, Long>
