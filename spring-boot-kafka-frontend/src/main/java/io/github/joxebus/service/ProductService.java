package io.github.joxebus.service;

import io.github.joxebus.dto.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${backend.api.url}")
    private String backendUrl;

    public List<ProductDTO> getAllProducts() {
        String url = backendUrl + "/products";
        ProductDTO[] products = restTemplate.getForObject(url, ProductDTO[].class);
        return products != null ? Arrays.asList(products) : List.of();
    }

    public ProductDTO getProductById(String id) {
        String url = backendUrl + "/products/" + id;
        return restTemplate.getForObject(url, ProductDTO.class);
    }

    public ProductDTO createProduct(ProductDTO product) {
        String url = backendUrl + "/products";
        return restTemplate.postForObject(url, product, ProductDTO.class);
    }

    public void updateProduct(String id, ProductDTO product) {
        String url = backendUrl + "/products/" + id;
        restTemplate.put(url, product);
    }

    public void deleteProduct(String id) {
        String url = backendUrl + "/products/" + id;
        restTemplate.delete(url);
    }
}
