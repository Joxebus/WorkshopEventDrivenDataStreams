package io.github.joxebus.kafka.producer

import io.github.joxebus.dto.PurchaseOrderDTO
import io.github.joxebus.enums.PurchaseOrderStatus
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class PurchaseOrderProducerSpec extends Specification {

    KafkaTemplate<String, PurchaseOrderDTO> kafkaTemplate = Mock()

    @Subject
    PurchaseOrderProducer producer = new PurchaseOrderProducer(kafkaTemplate: kafkaTemplate)

    def "should send purchase order to Kafka"() {
        given: "a purchase order"
        def order = new PurchaseOrderDTO(
                "order-123",
                "CUSTOMER-001",
                [],
                100.0,
                LocalDateTime.now(),
                PurchaseOrderStatus.CREATED
        )

        when: "sending order"
        producer.sendOrder(order)

        then: "kafka template sends message to topic"
        1 * kafkaTemplate.send("purchase-orders", "order-123", order)
    }

    def "should send order with correct key"() {
        given: "a purchase order with specific ID"
        def orderId = "test-order-456"
        def order = new PurchaseOrderDTO(
                orderId,
                "CUSTOMER-002",
                [],
                250.0,
                LocalDateTime.now(),
                PurchaseOrderStatus.CREATED
        )

        when: "sending order"
        producer.sendOrder(order)

        then: "message key matches order ID"
        1 * kafkaTemplate.send("purchase-orders", orderId, order)
    }
}
