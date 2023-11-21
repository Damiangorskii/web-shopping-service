package com.example.webshoppingservice.client;

import com.example.webshoppingservice.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductClient {

    private final RestTemplate restTemplate;

    private final ProductConfig config;

    @Autowired
    public ProductClient(RestTemplate restTemplate, ProductConfig config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public List<Product> getAllProducts() {
        return Arrays.asList(restTemplate.getForObject(config.getUrl() + "/products", Product[].class));
    }
}
