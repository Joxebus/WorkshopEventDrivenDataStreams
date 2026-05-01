package io.github.joxebus.service;

import io.github.joxebus.dto.ProductOrderDTO;
import io.github.joxebus.dto.PurchaseOrderDTO;
import io.github.joxebus.entity.Order;
import io.github.joxebus.entity.OrderItem;
import io.github.joxebus.enums.PurchaseOrderStatus;
import io.github.joxebus.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OrderQueryService {

    @Autowired
    private OrderRepository orderRepository;

    public List<PurchaseOrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public PurchaseOrderDTO getOrderById(String id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        return mapToDTO(order);
    }

    public List<PurchaseOrderDTO> getOrdersByCustomerId(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public List<PurchaseOrderDTO> getOrdersByStatus(PurchaseOrderStatus status) {
        return orderRepository.findByStatus(status).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    private PurchaseOrderDTO mapToDTO(Order order) {
        List<ProductOrderDTO> items = order.getItems().stream()
            .map(this::mapItemToDTO)
            .collect(Collectors.toList());

        return new PurchaseOrderDTO(
            order.getId(),
            order.getCustomerId(),
            items,
            order.getTotal(),
            order.getOrderDate(),
            order.getStatus()
        );
    }

    private ProductOrderDTO mapItemToDTO(OrderItem item) {
        return new ProductOrderDTO(
            item.getProductId(),
            item.getQuantity(),
            item.getPrice(),
            item.getSubtotal()
        );
    }
}
