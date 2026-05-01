package io.github.joxebus.service;

import io.github.joxebus.dto.ProductDTO;
import io.github.joxebus.entity.Product;
import io.github.joxebus.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ProductCommandService {

    @Autowired
    private ProductRepository productRepository;

    public ProductDTO createProduct(ProductDTO dto) {
        // Generate UUID for new product
        String productId = UUID.randomUUID().toString();

        Product product = new Product();
        product.setId(productId);
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());

        Product saved = productRepository.save(product);

        return mapToDTO(saved);
    }

    public ProductDTO updateProduct(String id, ProductDTO dto) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(dto.getCategory());

        Product updated = productRepository.save(product);

        return mapToDTO(updated);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    public void decrementStock(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory()
        );
    }
}
