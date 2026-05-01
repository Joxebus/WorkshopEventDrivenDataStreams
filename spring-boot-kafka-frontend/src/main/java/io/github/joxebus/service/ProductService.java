package io.github.joxebus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.joxebus.dto.PageResponse;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${backend.api.url}")
    private String backendUrl;

    public List<ProductDTO> getAllProducts() {
        String url = backendUrl + "/products";
        ProductDTO[] products = restTemplate.getForObject(url, ProductDTO[].class);
        return products != null ? Arrays.asList(products) : List.of();
    }

    public PageResponse<ProductDTO> getAllProductsPaginated(int page, int size) {
        return getAllProductsPaginated(page, size, null);
    }

    public PageResponse<ProductDTO> getAllProductsPaginated(int page, int size, String category) {
        StringBuilder urlBuilder = new StringBuilder(backendUrl + "/products/paginated?page=" + page + "&size=" + size);
        if (category != null && !category.isEmpty() && !category.equals("All")) {
            urlBuilder.append("&category=").append(category);
        }
        String url = urlBuilder.toString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            List<ProductDTO> content = objectMapper.convertValue(
                rootNode.get("content"),
                new TypeReference<List<ProductDTO>>() {}
            );

            PageResponse<ProductDTO> pageResponse = new PageResponse<>();
            pageResponse.setContent(content);
            pageResponse.setPageNumber(rootNode.get("number").asInt());
            pageResponse.setPageSize(rootNode.get("size").asInt());
            pageResponse.setTotalElements(rootNode.get("totalElements").asLong());
            pageResponse.setTotalPages(rootNode.get("totalPages").asInt());
            pageResponse.setFirst(rootNode.get("first").asBoolean());
            pageResponse.setLast(rootNode.get("last").asBoolean());
            pageResponse.setEmpty(rootNode.get("empty").asBoolean());

            return pageResponse;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching paginated products", e);
        }
    }

    public List<String> getAllCategories() {
        String url = backendUrl + "/products/categories";
        String[] categories = restTemplate.getForObject(url, String[].class);
        return categories != null ? Arrays.asList(categories) : List.of();
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
