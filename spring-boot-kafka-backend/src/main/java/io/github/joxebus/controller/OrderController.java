package io.github.joxebus.controller;

import io.github.joxebus.dto.PurchaseOrderDTO;
import io.github.joxebus.enums.PurchaseOrderStatus;
import io.github.joxebus.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:8081")
public class OrderController {

    @Autowired
    private OrderQueryService orderQueryService;

    @GetMapping
    public ResponseEntity<List<PurchaseOrderDTO>> getAllOrders() {
        List<PurchaseOrderDTO> orders = orderQueryService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getOrderById(@PathVariable String id) {
        try {
            PurchaseOrderDTO order = orderQueryService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PurchaseOrderDTO>> getOrdersByCustomerId(@PathVariable String customerId) {
        List<PurchaseOrderDTO> orders = orderQueryService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PurchaseOrderDTO>> getOrdersByStatus(@PathVariable PurchaseOrderStatus status) {
        List<PurchaseOrderDTO> orders = orderQueryService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }
}
