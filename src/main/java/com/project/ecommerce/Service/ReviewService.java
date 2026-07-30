package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.Review;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Request.CreateReviewRequest;
import com.project.ecommerce.Response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse createReview(
            CreateReviewRequest request,
            User user,
            Product product
    );

    ReviewResponse updateReview(
            Long reviewId,
            CreateReviewRequest request,
            Long userId
    );

    void deleteReview(
            Long reviewId,
            Long userId
    );

    ReviewResponse getReviewById(Long reviewId);

    List<ReviewResponse> getReviewsByProduct(
            Long productId
    );

}