package io.github.joxebus.service

import io.github.joxebus.entity.Product
import io.github.joxebus.repository.ProductRepository
import spock.lang.Specification
import spock.lang.Subject

class ProductQueryServiceSpec extends Specification {

    ProductRepository productRepository = Mock()

    @Subject
    ProductQueryService service = new ProductQueryService(productRepository: productRepository)

    def "should get all products"() {
        given: "products exist in database"
        def products = [
            new Product("id1", "Product 1", "Desc 1", 10.0, 5, "Cat1", null, null),
            new Product("id2", "Product 2", "Desc 2", 20.0, 10, "Cat2", null, null)
        ]
        productRepository.findAll() >> products

        when: "getting all products"
        def result = service.getAllProducts()

        then: "all products are returned"
        result.size() == 2
        result[0].name == "Product 1"
        result[1].name == "Product 2"
    }

    def "should return empty list when no products exist"() {
        given: "no products in database"
        productRepository.findAll() >> []

        when: "getting all products"
        def result = service.getAllProducts()

        then: "empty list is returned"
        result.isEmpty()
    }

    def "should get product by ID"() {
        given: "a product exists"
        def product = new Product("id1", "Product", "Description", 15.0, 8, "Category", null, null)
        productRepository.findById("id1") >> Optional.of(product)

        when: "getting product by ID"
        def result = service.getProductById("id1")

        then: "product is returned"
        result.id == "id1"
        result.name == "Product"
        result.price == 15.0
    }

    def "should throw exception when product not found by ID"() {
        given: "product does not exist"
        productRepository.findById("non-existent") >> Optional.empty()

        when: "trying to get product"
        service.getProductById("non-existent")

        then: "exception is thrown"
        def ex = thrown(RuntimeException)
        ex.message.contains("not found")
    }

    def "should get products by category"() {
        given: "products in specific category"
        def products = [
            new Product("id1", "Product 1", "Desc", 10.0, 5, "Electronics", null, null),
            new Product("id2", "Product 2", "Desc", 20.0, 10, "Electronics", null, null)
        ]
        productRepository.findByCategory("Electronics") >> products

        when: "getting products by category"
        def result = service.getProductsByCategory("Electronics")

        then: "products in category are returned"
        result.size() == 2
        result.every { it.category == "Electronics" }
    }

    def "should search products by name"() {
        given: "products matching search term"
        def products = [
            new Product("id1", "Laptop Computer", "Desc", 1000.0, 5, "Electronics", null, null),
            new Product("id2", "Gaming Laptop", "Desc", 1500.0, 3, "Electronics", null, null)
        ]
        productRepository.findByNameContainingIgnoreCase("laptop") >> products

        when: "searching by name"
        def result = service.searchProductsByName("laptop")

        then: "matching products are returned"
        result.size() == 2
        result.every { it.name.toLowerCase().contains("laptop") }
    }

    def "should return empty list when no products match search"() {
        given: "no matching products"
        productRepository.findByNameContainingIgnoreCase("nonexistent") >> []

        when: "searching by name"
        def result = service.searchProductsByName("nonexistent")

        then: "empty list is returned"
        result.isEmpty()
    }
}
