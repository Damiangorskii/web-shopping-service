package com.example.webshoppingservice.service;

import com.example.webshoppingservice.ProductDataProvider;
import com.example.webshoppingservice.client.ProductClient;
import com.example.webshoppingservice.model.*;
import com.example.webshoppingservice.repository.ShoppingCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShoppingServiceTest {

    private static final ShoppingCart SHOPPING_CART = ShoppingCart.builder()
            .id(UUID.randomUUID())
            .products(List.of(Product.builder()
                    .id(UUID.randomUUID())
                    .name("Test product")
                    .description("Test description")
                    .price(BigDecimal.TEN)
                    .manufacturer(Manufacturer.builder()
                            .id(UUID.randomUUID())
                            .name("manufacturer name")
                            .address("address")
                            .contact("contact")
                            .build())
                    .categories(List.of(Category.BABY_PRODUCTS))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .reviews(List.of(Review.builder()
                            .reviewerName("Name")
                            .comment("Comment")
                            .rating(5)
                            .reviewDate(LocalDateTime.now())
                            .build()))
                    .build()))
            .build();

    @Mock
    private ShoppingCartRepository shoppingCartRepository;
    @Mock
    private ProductClient productClient;
    private ShoppingService shoppingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        shoppingService = new ShoppingService(shoppingCartRepository, productClient);
    }

    @Test
    void should_return_cart() {
        UUID cartId = UUID.randomUUID();
        when(shoppingCartRepository.findShoppingCartById(any())).thenReturn(Optional.of(SHOPPING_CART));

        ShoppingCart result = shoppingService.retrieveShoppingCart(cartId);
        assertThat(result).isEqualTo(SHOPPING_CART);
    }

    @Test
    void should_return_not_found_for_non_existing_cart() {
        UUID cartId = UUID.randomUUID();
        when(shoppingCartRepository.findShoppingCartById(any())).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.retrieveShoppingCart(cartId)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Shopping cart not found");
    }

    @Test
    void should_create_shopping_cart() {
        Product product1 = ProductDataProvider.getSimpleProduct();
        Product product2 = ProductDataProvider.getSimpleProduct();
        ShoppingCartRequestBody requestBody = new ShoppingCartRequestBody(List.of(product1.getId(), product2.getId()));
        List<Product> productList = Arrays.asList(product1, product2);
        ShoppingCart shoppingCart = new ShoppingCart(UUID.randomUUID(), productList, LocalDateTime.now());

        when(productClient.getAllProducts()).thenReturn(productList);
        when(shoppingCartRepository.save(any())).thenReturn(shoppingCart);

        ShoppingCart result = shoppingService.createShoppingCart(requestBody);
        assertNotNull(result.getId());
        assertThat(result.getProducts()).containsExactlyInAnyOrderElementsOf(productList);
    }

    @Test
    void testCreateShoppingCart_NoMatchingProducts() {
        ShoppingCartRequestBody requestBody = new ShoppingCartRequestBody(List.of(UUID.randomUUID()));

        when(productClient.getAllProducts()).thenReturn(Collections.emptyList());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.createShoppingCart(requestBody)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No available products found");
    }

    @Test
    void should_successfully_edit_shopping_cart() {
        UUID cartId = UUID.randomUUID();
        Product product1 = ProductDataProvider.getSimpleProduct();
        Product product2 = ProductDataProvider.getSimpleProduct();
        List<Product> productList = Arrays.asList(product1, product2);
        ShoppingCart existingCart = new ShoppingCart(cartId, Collections.emptyList(), LocalDateTime.now());
        ShoppingCart updatedCart = new ShoppingCart(cartId, productList, LocalDateTime.now());

        when(productClient.getAllProducts()).thenReturn(productList);
        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.of(existingCart));
        when(shoppingCartRepository.save(any())).thenReturn(updatedCart);

        ShoppingCart result = shoppingService.editShoppingCart(cartId, new ShoppingCartRequestBody(List.of(product1.getId(), product2.getId())));
        assertThat(result.getId()).isEqualTo(cartId);
        assertThat(result.getProducts()).containsExactlyInAnyOrderElementsOf(productList);
    }

    @Test
    void should_return_error_when_no_products_found() {
        UUID cartId = UUID.randomUUID();
        ShoppingCart existingCart = new ShoppingCart(cartId, Collections.emptyList(), LocalDateTime.now());

        when(productClient.getAllProducts()).thenReturn(Collections.emptyList());
        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.of(existingCart));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.editShoppingCart(cartId, new ShoppingCartRequestBody(List.of(UUID.randomUUID())))
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No products found");
    }

    @Test
    void should_return_error_when_cart_does_not_exist() {
        UUID cartId = UUID.randomUUID();
        Product product1 = ProductDataProvider.getSimpleProduct();
        Product product2 = ProductDataProvider.getSimpleProduct();
        List<Product> productList = Arrays.asList(product1, product2);

        when(productClient.getAllProducts()).thenReturn(productList);
        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.editShoppingCart(cartId, new ShoppingCartRequestBody(List.of(UUID.randomUUID())))
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No products found");
    }

    @Test
    void should_add_products_to_shopping_cart() {
        UUID cartId = UUID.randomUUID();
        Product product1 = ProductDataProvider.getSimpleProduct();
        Product product2 = ProductDataProvider.getSimpleProduct();
        List<Product> productList = Arrays.asList(product1, product2);
        ShoppingCart existingCart = new ShoppingCart(cartId, Collections.singletonList(product1), LocalDateTime.now());
        ShoppingCart updatedCart = new ShoppingCart(cartId, productList, LocalDateTime.now());

        when(productClient.getAllProducts()).thenReturn(productList);
        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.of(existingCart));
        when(shoppingCartRepository.save(any())).thenReturn(updatedCart);

        ShoppingCart result = shoppingService.addProductsToShoppingCart(cartId, List.of(product1.getId(), product2.getId()));
        assertThat(result.getId()).isEqualTo(cartId);
        assertThat(result.getProducts()).containsExactlyInAnyOrderElementsOf(productList);
    }

    @Test
    void should_return_add_not_found_when_no_matching_products_found() {
        UUID cartId = UUID.randomUUID();
        List<Product> productList = List.of(ProductDataProvider.getSimpleProduct());
        ShoppingCart existingCart = new ShoppingCart(cartId, productList, LocalDateTime.now());

        when(productClient.getAllProducts()).thenReturn(Collections.emptyList());
        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.of(existingCart));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.addProductsToShoppingCart(cartId, Arrays.asList(UUID.randomUUID(), UUID.randomUUID()))
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("No products found");
    }

    @Test
    void should_return_add_error_not_found_when_shopping_cart_not_found() {
        UUID cartId = UUID.randomUUID();
        List<Product> productList = List.of(ProductDataProvider.getSimpleProduct());
        List<UUID> productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());

        when(productClient.getAllProducts()).thenReturn(productList);
        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.addProductsToShoppingCart(cartId, productIds)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Shopping cart not found");
    }

    @Test
    void should_remove_products_from_cart() {
        UUID cartId = UUID.randomUUID();
        List<UUID> productIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        Product product1 = ProductDataProvider.getSimpleProduct();
        List<Product> productList = Collections.singletonList(product1);
        List<UUID> productsToRemove = Collections.singletonList(productIds.get(0));
        ShoppingCart existingCart = new ShoppingCart(cartId, productList, LocalDateTime.now());
        ShoppingCart updatedCart = new ShoppingCart(cartId, Collections.emptyList(), LocalDateTime.now());

        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.of(existingCart));
        when(shoppingCartRepository.save(any())).thenReturn(updatedCart);

        ShoppingCart result = shoppingService.removeProductsFromShoppingCart(cartId, productsToRemove);
        assertThat(result.getId()).isEqualTo(cartId);
        assertThat(result.getProducts()).isEmpty();
    }

    @Test
    void should_return_unchanged_cart_if_products_to_remove_empty() {
        UUID cartId = UUID.randomUUID();
        List<UUID> productsToRemove = Collections.emptyList();
        ShoppingCart existingCart = new ShoppingCart(cartId, List.of(ProductDataProvider.getSimpleProduct()), LocalDateTime.now());

        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.of(existingCart));
        when(shoppingCartRepository.save(any())).thenReturn(existingCart);

        ShoppingCart result = shoppingService.removeProductsFromShoppingCart(cartId, productsToRemove);
        assertThat(result).isEqualTo(existingCart);
    }

    @Test
    void should_return_not_found_for_remove_products_when_cart_does_not_exist() {
        UUID cartId = UUID.randomUUID();
        List<UUID> productsToRemove = Collections.singletonList(UUID.randomUUID());

        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.removeProductsFromShoppingCart(cartId, productsToRemove)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Shopping cart not found");
    }

    @Test
    void should_return_not_found_for_delete_if_cart_does_not_exist() {
        UUID cartId = UUID.randomUUID();
        when(shoppingCartRepository.findShoppingCartById(cartId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                shoppingService.deleteShoppingCart(cartId)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Shopping cart not found");

        verify(shoppingCartRepository).findShoppingCartById(cartId);
        verify(shoppingCartRepository, never()).deleteShoppingCartById(cartId);
    }

}