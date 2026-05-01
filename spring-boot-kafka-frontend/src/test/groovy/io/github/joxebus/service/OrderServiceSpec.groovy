package io.github.joxebus.service

import io.github.joxebus.dto.ProductOrderDTO
import io.github.joxebus.dto.PurchaseOrderDTO
import io.github.joxebus.enums.PurchaseOrderStatus
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class OrderServiceSpec extends Specification {

    RestTemplate restTemplate = Mock()

    @Subject
    OrderService service = new OrderService(
            restTemplate: restTemplate,
            backendUrl: "http://localhost:8081/api"
    )

    def "should get all orders"() {
        given: "backend returns orders"
        def orders = [
                new PurchaseOrderDTO("order-1", "CUSTOMER-001", [], 50.0, LocalDateTime.now(), PurchaseOrderStatus.PENDING),
                new PurchaseOrderDTO("order-2", "CUSTOMER-001", [], 75.0, LocalDateTime.now(), PurchaseOrderStatus.COMPLETED)
        ] as PurchaseOrderDTO[]
        restTemplate.getForObject("http://localhost:8081/api/orders", PurchaseOrderDTO[].class) >> orders

        when: "getting all orders"
        def result = service.getAllOrders()

        then: "orders are returned"
        result.size() == 2
        result[0].orderId == "order-1"
        result[1].orderId == "order-2"
    }

    def "should return empty list when backend returns null"() {
        given: "backend returns null"
        restTemplate.getForObject("http://localhost:8081/api/orders", PurchaseOrderDTO[].class) >> null

        when: "getting all orders"
        def result = service.getAllOrders()

        then: "empty list is returned"
        result.isEmpty()
    }

    def "should get order by ID"() {
        given: "backend returns order with items"
        def products = [
                new ProductOrderDTO("product-1", 2, 10.0, 20.0),
                new ProductOrderDTO("product-2", 1, 15.0, 15.0)
        ]
        def order = new PurchaseOrderDTO("order-1", "CUSTOMER-001", products, 35.0, LocalDateTime.now(), PurchaseOrderStatus.PENDING)
        restTemplate.getForObject("http://localhost:8081/api/orders/order-1", PurchaseOrderDTO.class) >> order

        when: "getting order by ID"
        def result = service.getOrderById("order-1")

        then: "order is returned with items"
        result.orderId == "order-1"
        result.customerId == "CUSTOMER-001"
        result.total == 35.0
        result.products.size() == 2
    }
}
