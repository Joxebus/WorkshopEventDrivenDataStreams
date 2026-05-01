package io.github.joxebus.kafka.producer;

import io.github.joxebus.dto.PurchaseOrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderProducer {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderProducer.class);
    private static final String TOPIC = "purchase-orders";

    @Autowired
    private KafkaTemplate<String, PurchaseOrderDTO> kafkaTemplate;

    public void sendOrder(PurchaseOrderDTO order) {
        logger.info("Sending purchase order to Kafka: {}", order.getOrderId());
        kafkaTemplate.send(TOPIC, order.getOrderId(), order);
        logger.info("Purchase order sent successfully: {}", order.getOrderId());
    }
}
