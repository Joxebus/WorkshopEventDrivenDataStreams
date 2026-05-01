package io.github.joxebus.repository;

import io.github.joxebus.entity.Order;
import io.github.joxebus.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(PurchaseOrderStatus status);

    List<Order> findByCustomerIdAndStatus(String customerId, PurchaseOrderStatus status);
}
