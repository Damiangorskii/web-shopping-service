package com.example.webshoppingservice.service;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@AllArgsConstructor
public class ShoppingCartCleanupScheduler {

    private final ShoppingService shoppingService;

    @Scheduled(cron = "0 * * * * *")
    public void cleanUpOldCarts() {
        shoppingService.deleteOldCarts();
    }
}
