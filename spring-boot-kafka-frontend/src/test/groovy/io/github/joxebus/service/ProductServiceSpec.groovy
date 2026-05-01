package io.github.joxebus.service

import io.github.joxebus.dto.ProductDTO
import org.springframework.web.client.RestTemplate
import spock.lang.Specification
import spock.lang.Subject

class ProductServiceSpec extends Specification {

    RestTemplate restTemplate = Mock()

    @Subject
    ProductService service = new ProductService(
            restTemplate: restTemplate,
            backendUrl: "http://localhost:8080/api"
    )

    def "should get all products"() {
        given: "backend returns products"
        def products = [
                new ProductDTO("id1", "Product 1", "Desc 1", 10.0, 5, "Cat1"),
                new ProductDTO("id2", "Product 2", "Desc 2", 20.0, 10, "Cat2")
        ] as ProductDTO[]
        restTemplate.getForObject("http://localhost:8080/api/products", ProductDTO[].class) >> products

        when: "getting all products"
        def result = service.getAllProducts()

        then: "products are returned"
        result.size() == 2
        result[0].name == "Product 1"
        result[1].name == "Product 2"
    }

    def "should return empty list when backend returns null"() {
        given: "backend returns null"
        restTemplate.getForObject("http://localhost:8080/api/products", ProductDTO[].class) >> null

        when: "getting all products"
        def result = service.getAllProducts()

        then: "empty list is returned"
        result.isEmpty()
    }

    def "should get product by ID"() {
        given: "backend returns product"
        def product = new ProductDTO("id1", "Product", "Description", 15.0, 8, "Category")
        restTemplate.getForObject("http://localhost:8080/api/products/id1", ProductDTO.class) >> product

        when: "getting product by ID"
        def result = service.getProductById("id1")

        then: "product is returned"
        result.id == "id1"
        result.name == "Product"
    }

    def "should create product"() {
        given: "product to create"
        def newProduct = new ProductDTO(null, "New Product", "New Desc", 25.0, 15, "New Cat")
        def createdProduct = new ProductDTO("generated-id", "New Product", "New Desc", 25.0, 15, "New Cat")
        restTemplate.postForObject("http://localhost:8080/api/products", newProduct, ProductDTO.class) >> createdProduct

        when: "creating product"
        def result = service.createProduct(newProduct)

        then: "created product is returned"
        result.id == "generated-id"
        result.name == "New Product"
    }

    def "should update product"() {
        given: "product to update"
        def product = new ProductDTO("id1", "Updated Product", "Updated Desc", 30.0, 20, "Updated Cat")

        when: "updating product"
        service.updateProduct("id1", product)

        then: "REST template put is called"
        1 * restTemplate.put("http://localhost:8080/api/products/id1", product)
    }

    def "should delete product"() {
        when: "deleting product"
        service.deleteProduct("id1")

        then: "REST template delete is called"
        1 * restTemplate.delete("http://localhost:8080/api/products/id1")
    }
}
