package io.github.joxebus.service

import io.github.joxebus.dto.ProductOrderDTO
import io.github.joxebus.dto.PurchaseOrderDTO
import io.github.joxebus.entity.Order
import io.github.joxebus.enums.PurchaseOrderStatus
import io.github.joxebus.repository.OrderRepository
import io.github.joxebus.repository.ProductRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDateTime

class OrderCommandServiceSpec extends Specification {

    OrderRepository orderRepository = Mock()
    ProductRepository productRepository = Mock()
    ProductCommandService productCommandService = Mock()

    @Subject
    OrderCommandService service = new OrderCommandService(
        orderRepository: orderRepository,
        productRepository: productRepository,
        productCommandService: productCommandService
    )

    def "should create order successfully"() {
        given: "a valid purchase order DTO"
        def products = [
            new ProductOrderDTO("product-1", 2, 10.0, 20.0),
            new ProductOrderDTO("product-2", 1, 15.0, 15.0)
        ]
        def orderDto = new PurchaseOrderDTO(
            "order-id",
            "CUSTOMER-001",
            products,
            35.0,
            LocalDateTime.now(),
            PurchaseOrderStatus.CREATED
        )

        and: "all products exist"
        productRepository.existsById("product-1") >> true
        productRepository.existsById("product-2") >> true

        and: "sufficient stock available"
        productCommandService.decrementStock("product-1", 2) >> {}
        productCommandService.decrementStock("product-2", 1) >> {}

        and: "order is saved"
        orderRepository.save(_ as Order) >> { Order order ->
            order.status = PurchaseOrderStatus.PENDING
            return order
        }

        when: "creating the order"
        def result = service.createOrder(orderDto)

        then: "products exist check is performed"
        1 * productRepository.existsById("product-1")
        1 * productRepository.existsById("product-2")

        and: "stock is decremented"
        1 * productCommandService.decrementStock("product-1", 2)
        1 * productCommandService.decrementStock("product-2", 1)

        and: "order is saved with PENDING status"
        1 * orderRepository.save({ Order o ->
            o.id == "order-id" &&
            o.customerId == "CUSTOMER-001" &&
            o.total == 35.0 &&
            o.status == PurchaseOrderStatus.PENDING &&
            o.items.size() == 2
        })

        and: "result has PENDING status"
        result.status == PurchaseOrderStatus.PENDING
    }

    def "should throw exception when product does not exist"() {
        given: "a purchase order with non-existent product"
        def products = [new ProductOrderDTO("non-existent", 1, 10.0, 10.0)]
        def orderDto = new PurchaseOrderDTO(
            "order-id",
            "CUSTOMER-001",
            products,
            10.0,
            LocalDateTime.now(),
            PurchaseOrderStatus.CREATED
        )

        and: "product does not exist"
        productRepository.existsById("non-existent") >> false

        when: "trying to create order"
        service.createOrder(orderDto)

        then: "exception is thrown"
        def ex = thrown(RuntimeException)
        ex.message.contains("not found")
    }

    def "should mark order as cancelled when insufficient stock"() {
        given: "a purchase order"
        def products = [new ProductOrderDTO("product-1", 10, 10.0, 100.0)]
        def orderDto = new PurchaseOrderDTO(
            "order-id",
            "CUSTOMER-001",
            products,
            100.0,
            LocalDateTime.now(),
            PurchaseOrderStatus.CREATED
        )

        and: "product exists but insufficient stock"
        productRepository.existsById("product-1") >> true
        productCommandService.decrementStock("product-1", 10) >> {
            throw new RuntimeException("Insufficient stock")
        }

        and: "order is saved as cancelled"
        orderRepository.save(_ as Order) >> { Order order -> order }

        when: "trying to create order"
        service.createOrder(orderDto)

        then: "order is saved with CANCELLED status"
        1 * orderRepository.save({ Order o ->
            o.status == PurchaseOrderStatus.CANCELLED
        })

        and: "exception is thrown"
        thrown(RuntimeException)
    }

    def "should update order status"() {
        given: "an existing order"
        def order = new Order("order-id", "CUSTOMER-001", 50.0, LocalDateTime.now(), PurchaseOrderStatus.PENDING, [])
        orderRepository.findById("order-id") >> Optional.of(order)
        orderRepository.save(_ as Order) >> order

        when: "updating order status"
        service.updateOrderStatus("order-id", PurchaseOrderStatus.COMPLETED)

        then: "order status is updated"
        1 * orderRepository.save({ Order o ->
            o.id == "order-id" &&
            o.status == PurchaseOrderStatus.COMPLETED
        })
    }

    def "should throw exception when updating non-existent order"() {
        given: "order does not exist"
        orderRepository.findById("non-existent") >> Optional.empty()

        when: "trying to update status"
        service.updateOrderStatus("non-existent", PurchaseOrderStatus.COMPLETED)

        then: "exception is thrown"
        def ex = thrown(RuntimeException)
        ex.message.contains("not found")
    }
}
