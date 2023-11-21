package com.example.webshoppingservice.repository;

import com.example.webshoppingservice.model.ShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends MongoRepository<ShoppingCart, String> {
    Optional<ShoppingCart> findShoppingCartById(UUID id);

    void deleteShoppingCartById(UUID id);
}
