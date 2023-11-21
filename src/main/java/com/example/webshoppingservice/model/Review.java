package com.example.webshoppingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    private String reviewerName;
    private String comment;
    private int rating;
    private LocalDateTime reviewDate;
}

