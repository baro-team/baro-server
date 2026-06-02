package com.baro.dispatch.infrastructure.kafka

import com.baro.dispatch.application.port.out.DispatchEvent
import com.baro.dispatch.application.port.out.DispatchEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaDispatchEventAdapter(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    @Value("\${kafka.topic.dispatch-events}") private val dispatchEventsTopic: String,
) : DispatchEventPublisher {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun publish(event: DispatchEvent) {
        val message = mapOf(
            "dispatch_id"     to event.dispatchId,
            "user_id"         to event.userId,
            "car_id"          to event.carId,
            "start_latitude"  to event.startLatitude,
            "start_longitude" to event.startLongitude,
            "end_latitude"    to event.endLatitude,
            "end_longitude"   to event.endLongitude,
            "fare"            to event.fare,
            "distance_km"     to event.distanceKm,
            "estimated_time"  to event.estimatedTime,
            "status"          to event.status,
            "requested_at"    to event.requestedAt.toString(),
        )
        kafkaTemplate.send(dispatchEventsTopic, event.dispatchId.toString(), message)
            .whenComplete { _, ex ->
                if (ex != null) log.error("dispatch 이벤트 Kafka 발행 실패 [dispatchId={}]: {}", event.dispatchId, ex.message)
            }
    }
}
