package com.project.ecommerce.Request;

import lombok.Data;

import java.util.List;
@Data
public class CreateReviewRequest {
    String reviewText ;
    private Integer rating;
    private List<String> images;
}
