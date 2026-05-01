package io.github.joxebus.service

import io.github.joxebus.entity.Order
import io.github.joxebus.entity.OrderItem
import io.github.joxebus.enums.PurchaseOrderStatus
import io.github.joxebus.repository.OrderRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class OrderQueryServiceSpec extends Specification {

    OrderRepository orderRepository = Mock()

    @Subject
    OrderQueryService service = new OrderQueryService(orderRepository: orderRepository)

    def "should get all orders"() {
        given: "orders exist in database"
        def order1 = createOrder("id1", PurchaseOrderStatus.PENDING)
        def order2 = createOrder("id2", PurchaseOrderStatus.COMPLETED)
        orderRepository.findAll() >> [order1, order2]

        when: "getting all orders"
        def result = service.getAllOrders()

        then: "all orders are returned"
        result.size() == 2
        result[0].orderId == "id1"
        result[1].orderId == "id2"
    }

    def "should return empty list when no orders exist"() {
        given: "no orders in database"
        orderRepository.findAll() >> []

        when: "getting all orders"
        def result = service.getAllOrders()

        then: "empty list is returned"
        result.isEmpty()
    }

    def "should get order by ID with items"() {
        given: "an order with items exists"
        def order = createOrder("order-1", PurchaseOrderStatus.PENDING)
        def item1 = new OrderItem(1L, order, "product-1", 2, 10.0, 20.0)
        def item2 = new OrderItem(2L, order, "product-2", 1, 15.0, 15.0)
        order.items = [item1, item2]
        orderRepository.findById("order-1") >> Optional.of(order)

        when: "getting order by ID"
        def result = service.getOrderById("order-1")

        then: "order with items is returned"
        result.orderId == "order-1"
        result.products.size() == 2
        result.products[0].productId == "product-1"
        result.products[0].quantity == 2
        result.products[1].productId == "product-2"
        result.products[1].quantity == 1
    }

    def "should throw exception when order not found by ID"() {
        given: "order does not exist"
        orderRepository.findById("non-existent") >> Optional.empty()

        when: "trying to get order"
        service.getOrderById("non-existent")

        then: "exception is thrown"
        def ex = thrown(RuntimeException)
        ex.message.contains("not found")
    }

    def "should get orders by customer ID"() {
        given: "orders for specific customer"
        def order1 = createOrder("id1", PurchaseOrderStatus.PENDING, "CUSTOMER-001")
        def order2 = createOrder("id2", PurchaseOrderStatus.COMPLETED, "CUSTOMER-001")
        orderRepository.findByCustomerId("CUSTOMER-001") >> [order1, order2]

        when: "getting orders by customer ID"
        def result = service.getOrdersByCustomerId("CUSTOMER-001")

        then: "customer orders are returned"
        result.size() == 2
        result.every { it.customerId == "CUSTOMER-001" }
    }

    def "should get orders by status"() {
        given: "orders with specific status"
        def order1 = createOrder("id1", PurchaseOrderStatus.PENDING)
        def order2 = createOrder("id2", PurchaseOrderStatus.PENDING)
        orderRepository.findByStatus(PurchaseOrderStatus.PENDING) >> [order1, order2]

        when: "getting orders by status"
        def result = service.getOrdersByStatus(PurchaseOrderStatus.PENDING)

        then: "orders with status are returned"
        result.size() == 2
        result.every { it.status == PurchaseOrderStatus.PENDING }
    }

    def "should return empty list for customer with no orders"() {
        given: "customer has no orders"
        orderRepository.findByCustomerId("NEW-CUSTOMER") >> []

        when: "getting orders"
        def result = service.getOrdersByCustomerId("NEW-CUSTOMER")

        then: "empty list is returned"
        result.isEmpty()
    }

    private Order createOrder(String id, PurchaseOrderStatus status, String customerId = "CUSTOMER-001") {
        new Order(id, customerId, 50.0, LocalDateTime.now(), status, [])
    }
}
