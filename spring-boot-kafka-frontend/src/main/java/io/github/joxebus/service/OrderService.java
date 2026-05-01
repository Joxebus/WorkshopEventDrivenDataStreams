package io.github.joxebus.service;

import io.github.joxebus.dto.PurchaseOrderDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${backend.api.url}")
    private String backendUrl;

    public List<PurchaseOrderDTO> getAllOrders() {
        String url = backendUrl + "/orders";
        PurchaseOrderDTO[] orders = restTemplate.getForObject(url, PurchaseOrderDTO[].class);
        return orders != null ? Arrays.asList(orders) : List.of();
    }

    public PurchaseOrderDTO getOrderById(String id) {
        String url = backendUrl + "/orders/" + id;
        return restTemplate.getForObject(url, PurchaseOrderDTO.class);
    }
}
