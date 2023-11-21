package com.example.webshoppingservice.model;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ShoppingCartRequestBody(@NotNull List<UUID> products) {
}
