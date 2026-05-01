package io.github.joxebus.service;

import io.github.joxebus.dto.ProductOrderDTO;
import io.github.joxebus.dto.PurchaseOrderDTO;
import io.github.joxebus.entity.Order;
import io.github.joxebus.entity.OrderItem;
import io.github.joxebus.enums.PurchaseOrderStatus;
import io.github.joxebus.repository.OrderRepository;
import io.github.joxebus.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderCommandService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCommandService productCommandService;

    public PurchaseOrderDTO createOrder(PurchaseOrderDTO dto) {
        // Validate products exist and have sufficient stock
        for (ProductOrderDTO item : dto.getProducts()) {
            if (!productRepository.existsById(item.getProductId())) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }
        }

        // Create order entity
        Order order = new Order();
        order.setId(dto.getOrderId());
        order.setCustomerId(dto.getCustomerId());
        order.setTotal(dto.getTotal());
        order.setOrderDate(dto.getOrderDate());
        order.setStatus(PurchaseOrderStatus.CREATED);

        // Create order items and decrement stock
        for (ProductOrderDTO item : dto.getProducts()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            orderItem.setSubtotal(item.getTotal());
            order.addItem(orderItem);

            // Decrement product stock
            try {
                productCommandService.decrementStock(item.getProductId(), item.getQuantity());
            } catch (RuntimeException e) {
                // Mark order as cancelled if stock is insufficient
                order.setStatus(PurchaseOrderStatus.CANCELLED);
                orderRepository.save(order);
                throw e;
            }
        }

        // Update order status to PENDING
        order.setStatus(PurchaseOrderStatus.PENDING);
        Order saved = orderRepository.save(order);

        // Return DTO with updated status
        dto.setStatus(saved.getStatus());
        return dto;
    }

    public void updateOrderStatus(String orderId, PurchaseOrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        order.setStatus(status);
        orderRepository.save(order);
    }
}
