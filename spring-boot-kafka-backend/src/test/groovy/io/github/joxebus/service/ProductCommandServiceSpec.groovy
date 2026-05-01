package io.github.joxebus.service

import io.github.joxebus.dto.ProductDTO
import io.github.joxebus.entity.Product
import io.github.joxebus.repository.ProductRepository
import spock.lang.Specification
import spock.lang.Subject

class ProductCommandServiceSpec extends Specification {

    ProductRepository productRepository = Mock()

    @Subject
    ProductCommandService service = new ProductCommandService(productRepository: productRepository)

    def "should create product with generated UUID"() {
        given: "a product DTO without ID"
        def dto = new ProductDTO(null, "Laptop", "Gaming laptop", 1299.99, 10, "Electronics")

        when: "creating the product"
        def result = service.createProduct(dto)

        then: "product is saved with generated ID"
        1 * productRepository.save({ Product p ->
            p.name == "Laptop" &&
            p.description == "Gaming laptop" &&
            p.price == 1299.99 &&
            p.stock == 10 &&
            p.category == "Electronics" &&
            p.id != null
        }) >> { Product p -> p }

        and: "result contains the saved product"
        result.id != null
        result.name == "Laptop"
        result.price == 1299.99
    }

    def "should update existing product"() {
        given: "an existing product"
        def productId = "existing-id"
        def existingProduct = new Product(productId, "Old Name", "Old Desc", 100.0, 5, "Old Cat", null, null)
        productRepository.findById(productId) >> Optional.of(existingProduct)

        and: "updated product DTO"
        def updateDto = new ProductDTO(productId, "New Name", "New Desc", 200.0, 15, "New Cat")

        when: "updating the product"
        def result = service.updateProduct(productId, updateDto)

        then: "product is updated"
        1 * productRepository.save({ Product p ->
            p.id == productId &&
            p.name == "New Name" &&
            p.description == "New Desc" &&
            p.price == 200.0 &&
            p.stock == 15 &&
            p.category == "New Cat"
        }) >> { Product p -> p }

        and: "result is returned"
        result.name == "New Name"
    }

    def "should throw exception when updating non-existent product"() {
        given: "product does not exist"
        productRepository.findById("non-existent") >> Optional.empty()

        when: "trying to update"
        service.updateProduct("non-existent", new ProductDTO("id", "Name", "Desc", 10.0, 1, "Cat"))

        then: "exception is thrown"
        thrown(RuntimeException)
    }

    def "should delete product by ID"() {
        given: "a product ID"
        def productId = "product-to-delete"

        when: "deleting the product"
        service.deleteProduct(productId)

        then: "repository delete is called"
        1 * productRepository.deleteById(productId)
    }

    def "should decrement stock successfully"() {
        given: "a product with sufficient stock"
        def productId = "product-id"
        def product = new Product(productId, "Product", "Desc", 10.0, 20, "Cat", null, null)
        productRepository.findById(productId) >> Optional.of(product)
        productRepository.save(_ as Product) >> product

        when: "decrementing stock"
        service.decrementStock(productId, 5)

        then: "stock is reduced"
        1 * productRepository.save({ Product p ->
            p.stock == 15
        })
    }

    def "should throw exception when insufficient stock"() {
        given: "a product with low stock"
        def productId = "product-id"
        def product = new Product(productId, "Product", "Desc", 10.0, 3, "Cat", null, null)
        productRepository.findById(productId) >> Optional.of(product)

        when: "trying to decrement more than available"
        service.decrementStock(productId, 5)

        then: "exception is thrown"
        def ex = thrown(RuntimeException)
        ex.message.contains("Insufficient stock")
    }

    def "should throw exception when decrementing stock for non-existent product"() {
        given: "product does not exist"
        productRepository.findById("non-existent") >> Optional.empty()

        when: "trying to decrement stock"
        service.decrementStock("non-existent", 1)

        then: "exception is thrown"
        def ex = thrown(RuntimeException)
        ex.message.contains("not found")
    }
}
