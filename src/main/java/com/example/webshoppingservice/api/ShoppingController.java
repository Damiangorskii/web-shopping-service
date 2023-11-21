package com.example.webshoppingservice.api;

import com.example.webshoppingservice.model.ShoppingCart;
import com.example.webshoppingservice.model.ShoppingCartRequestBody;
import com.example.webshoppingservice.service.ShoppingService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shopping/cart")
@AllArgsConstructor
public class ShoppingController {

    private final ShoppingService shoppingService;

    @PostMapping
    public ShoppingCart createShoppingCart(final @RequestBody @Valid ShoppingCartRequestBody requestBody) {
        return shoppingService.createShoppingCart(requestBody);
    }

    @GetMapping("{cartId}")
    public ShoppingCart getShoppingCart(final @PathVariable UUID cartId) {
        return shoppingService.retrieveShoppingCart(cartId);
    }

    @PutMapping("{cartId}")
    public ShoppingCart updateShoppingCart(final @PathVariable UUID cartId, final @RequestBody @Valid ShoppingCartRequestBody requestBody) {
        return shoppingService.editShoppingCart(cartId, requestBody);
    }

    @PatchMapping("{cartId}/add")
    public ShoppingCart addProductsToShoppingCart(final @PathVariable UUID cartId, final @RequestParam List<UUID> productIds) {
        return shoppingService.addProductsToShoppingCart(cartId, productIds);
    }

    @PatchMapping("{cartId}/remove")
    public ShoppingCart removeProductsFromShoppingCart(final @PathVariable UUID cartId, final @RequestParam List<UUID> productIds) {
        return shoppingService.removeProductsFromShoppingCart(cartId, productIds);
    }

    @DeleteMapping("{cartId}")
    public void deleteShoppingCart(final @PathVariable UUID cartId) {
        shoppingService.deleteShoppingCart(cartId);
    }
}
