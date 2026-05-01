package io.github.joxebus.kafka.consumer;

import io.github.joxebus.dto.PurchaseOrderDTO;
import io.github.joxebus.service.OrderCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderConsumer.class);

    @Autowired
    private OrderCommandService orderCommandService;

    @KafkaListener(topics = "purchase-orders", groupId = "backend-consumer-group")
    public void consume(PurchaseOrderDTO orderDTO) {
        logger.info("Received purchase order: {}", orderDTO.getOrderId());

        try {
            // Process the order
            PurchaseOrderDTO processedOrder = orderCommandService.createOrder(orderDTO);
            logger.info("Successfully processed order: {} with status: {}",
                processedOrder.getOrderId(), processedOrder.getStatus());
        } catch (Exception e) {
            logger.error("Error processing order: {}", orderDTO.getOrderId(), e);
            // In a production system, you might want to send to a dead letter queue
        }
    }
}
