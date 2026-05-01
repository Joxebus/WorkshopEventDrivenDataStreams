package io.github.joxebus.repository;

import io.github.joxebus.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findByCategory(String category);

    List<Product> findByNameContainingIgnoreCase(String name);
}
