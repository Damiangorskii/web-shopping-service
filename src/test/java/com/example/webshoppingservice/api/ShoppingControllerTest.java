package com.example.webshoppingservice.api;

import com.example.webshoppingservice.model.*;
import com.example.webshoppingservice.service.ShoppingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ShoppingControllerTest {

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
    private static final String NOT_UUID = "some-not-uuid-string";
    private static final ResponseStatusException ERROR = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "some-error");
    private static final ShoppingCartRequestBody BODY = new ShoppingCartRequestBody(List.of(UUID.randomUUID()));
    @Mock
    private ShoppingService shoppingService;

    @InjectMocks
    private ShoppingController shoppingController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(shoppingController).build();
    }

    @Test
    void should_return_create_shopping_cart() throws Exception {
        when(shoppingService.createShoppingCart(any()))
                .thenReturn(SHOPPING_CART);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBodyJson = objectMapper.writeValueAsString(BODY);

        mockMvc.perform(post("/shopping/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(SHOPPING_CART)));
    }

    @Test
    void should_return_error_if_wrong_shopping_cart_url() throws Exception {
        when(shoppingService.createShoppingCart(any()))
                .thenReturn(SHOPPING_CART);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(BODY);

        mockMvc.perform(post("/shopping/cat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void should_return_error_if_create_cart_returned_error() throws Exception {
        when(shoppingService.createShoppingCart(any()))
                .thenThrow(ERROR);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(BODY);

        mockMvc.perform(post("/shopping/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void should_return_bad_request_for_create_cart() throws Exception {
        when(shoppingService.createShoppingCart(any()))
                .thenReturn(SHOPPING_CART);

        mockMvc.perform(post("/shopping/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_empty_cart() throws Exception {
        ShoppingCart emptyShoppingCart = new ShoppingCart(UUID.randomUUID(), Collections.emptyList());
        when(shoppingService.createShoppingCart(any()))
                .thenReturn(emptyShoppingCart);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(BODY);

        mockMvc.perform(post("/shopping/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(emptyShoppingCart)));
    }

    @Test
    void should_return_shopping_cart() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.retrieveShoppingCart(any()))
                .thenReturn(SHOPPING_CART);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc.perform(get("/shopping/cart/{cartId}", cartId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(SHOPPING_CART)));
    }

    @Test
    void should_return_error_if_get_cart_returned_error() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.retrieveShoppingCart(any()))
                .thenThrow(ERROR);

        mockMvc.perform(get("/shopping/cart/{cartId}", cartId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void should_return_bad_request_if_not_uuid() throws Exception {
        when(shoppingService.retrieveShoppingCart(any()))
                .thenReturn(SHOPPING_CART);

        mockMvc.perform(get("/shopping/cart/{cartId}", NOT_UUID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_updated_shopping_cart() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.editShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String requestBodyJson = objectMapper.writeValueAsString(BODY);

        mockMvc.perform(put("/shopping/cart/{cartId}", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(SHOPPING_CART)));
    }

    @Test
    void should_return_error_if_edit_returned_error() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.editShoppingCart(any(), any()))
                .thenThrow(ERROR);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(new ShoppingCartRequestBody(List.of(UUID.randomUUID())));

        mockMvc.perform(put("/shopping/cart/{cartId}", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void should_return_bad_request_if_cart_id_edit_not_uuid() throws Exception {
        when(shoppingService.editShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBodyJson = objectMapper.writeValueAsString(new ShoppingCartRequestBody(List.of(UUID.randomUUID())));

        mockMvc.perform(put("/shopping/cart/{cartId}", NOT_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_bad_request_if_edit_body_not_valid() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.editShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        mockMvc.perform(put("/shopping/cart/{cartId}", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_shopping_cart_with_added_products() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.addProductsToShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc.perform(patch("/shopping/cart/{cartId}/add?productIds=0073bddf-dcd5-4715-b914-eb48c35b9016", cartId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(SHOPPING_CART)));
    }

    @Test
    void should_return_error_id_add_products_returned_error() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.addProductsToShoppingCart(any(), any()))
                .thenThrow(ERROR);

        mockMvc.perform(patch("/shopping/cart/{cartId}/add?productIds=0073bddf-dcd5-4715-b914-eb48c35b9016", cartId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void should_return_bad_request_if_query_params_not_uuid() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.addProductsToShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        mockMvc.perform(patch("/shopping/cart/{cartId}/add?productIds=8c35b9016", cartId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_bad_request_if_add_products_path_variable_not_uuid() throws Exception {
        when(shoppingService.addProductsToShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        mockMvc.perform(patch("/shopping/cart/{cartId}/add?productIds=0073bddf-dcd5-4715-b914-eb48c35b9016", NOT_UUID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_shopping_cart_with_removed_products() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.removeProductsFromShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc.perform(patch("/shopping/cart/{cartId}/remove?productIds=0073bddf-dcd5-4715-b914-eb48c35b9016", cartId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(SHOPPING_CART)));
    }

    @Test
    void should_return_error_id_remove_products_returned_error() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.removeProductsFromShoppingCart(any(), any()))
                .thenThrow(ERROR);

        mockMvc.perform(patch("/shopping/cart/{cartId}/remove?productIds=0073bddf-dcd5-4715-b914-eb48c35b9016", cartId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void should_return_bad_request_if_query_params_not_uuid_for_remove_products() throws Exception {
        UUID cartId = UUID.randomUUID();
        when(shoppingService.removeProductsFromShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        mockMvc.perform(patch("/shopping/cart/{cartId}/remove?productIds=8c35b9016", cartId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_bad_request_if_remove_products_path_variable_not_uuid() throws Exception {
        when(shoppingService.removeProductsFromShoppingCart(any(), any()))
                .thenReturn(SHOPPING_CART);

        mockMvc.perform(patch("/shopping/cart/{cartId}/remove?productIds=0073bddf-dcd5-4715-b914-eb48c35b9016", NOT_UUID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_empty_for_removed_cart() throws Exception {
        UUID cartId = UUID.randomUUID();

        mockMvc.perform(delete("/shopping/cart/{cartId}", cartId))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_bad_request_for_delete_when_not_uuid() throws Exception {
        mockMvc.perform(delete("/shopping/cart/{cartId}", NOT_UUID))
                .andExpect(status().isBadRequest());
    }

}
