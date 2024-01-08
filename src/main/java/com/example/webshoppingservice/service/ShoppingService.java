package com.example.webshoppingservice.service;

import com.example.webshoppingservice.client.ProductClient;
import com.example.webshoppingservice.model.Product;
import com.example.webshoppingservice.model.ShoppingCart;
import com.example.webshoppingservice.model.ShoppingCartRequestBody;
import com.example.webshoppingservice.repository.ShoppingCartRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@Slf4j
public class ShoppingService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductClient productClient;

    public ShoppingCart retrieveShoppingCart(final UUID id) {
        return shoppingCartRepository.findShoppingCartById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found"));
    }

    public ShoppingCart createShoppingCart(final ShoppingCartRequestBody requestBody) {
        List<Product> products = productClient.getAllProducts();
        List<Product> filteredProducts = products.stream()
                .filter(product -> requestBody.products().contains(product.getId()))
                .toList();

        if (filteredProducts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No available products found");
        }

        ShoppingCart shoppingCart = new ShoppingCart(UUID.randomUUID(), filteredProducts, LocalDateTime.now());
        return shoppingCartRepository.save(shoppingCart);
    }

    public ShoppingCart editShoppingCart(final UUID cartId, final ShoppingCartRequestBody requestBody) {
        List<Product> products = productClient.getAllProducts();
        List<Product> filteredProducts = products.stream()
                .filter(product -> requestBody.products().contains(product.getId()))
                .toList();

        if (filteredProducts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No products found");
        }

        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found"));

        shoppingCart.setProducts(filteredProducts);
        return shoppingCartRepository.save(shoppingCart);
    }

    public ShoppingCart addProductsToShoppingCart(final UUID cartId, final List<UUID> productIds) {
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found"));

        List<Product> products = productClient.getAllProducts();
        List<Product> newProducts = products.stream()
                .filter(product -> productIds.contains(product.getId()))
                .toList();

        if (CollectionUtils.isNotEmpty(newProducts)) {
            List<Product> combinedProducts = Stream.concat(shoppingCart.getProducts().stream(), newProducts.stream())
                    .distinct()
                    .toList();
            shoppingCart.setProducts(combinedProducts);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No products found");
        }
        return shoppingCartRepository.save(shoppingCart);
    }

    public ShoppingCart removeProductsFromShoppingCart(final UUID cartId, final List<UUID> productIds) {
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found"));

        if (CollectionUtils.isNotEmpty(productIds)) {
            shoppingCart.setProducts(shoppingCart.getProducts().stream()
                    .filter(product -> !productIds.contains(product.getId()))
                    .toList());
        }

        return shoppingCartRepository.save(shoppingCart);
    }

    public void deleteShoppingCart(final UUID id) {
        ShoppingCart shoppingCart = shoppingCartRepository.findShoppingCartById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found"));

        shoppingCartRepository.delete(shoppingCart);
    }

    public void deleteOldCarts() {
        LocalDateTime oneMinuteAgo = LocalDateTime.now(ZoneId.systemDefault()).minusMinutes(1);
        shoppingCartRepository.deleteShoppingCartsByInsertDateTimeIsBefore(oneMinuteAgo);
        log.info("Old shopping carts removed.");

    }
}
