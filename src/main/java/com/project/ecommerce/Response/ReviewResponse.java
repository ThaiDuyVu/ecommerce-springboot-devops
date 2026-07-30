package com.project.ecommerce.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;

    private String reviewText;

    private Integer rating;

    private List<String> images;

    private Long userId;

    private String userName;

    private Long productId;

    private LocalDateTime createdAt;

}