package com.project.ecommerce.Controller;

import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.ProductRepository;
import com.project.ecommerce.Request.CreateReviewRequest;
import com.project.ecommerce.Response.ReviewResponse;
import com.project.ecommerce.Service.ReviewService;
import com.project.ecommerce.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    private final UserService userService;

    private final ProductRepository productRepository;

    @PostMapping("/products/{productId}")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long productId,
            @RequestBody CreateReviewRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Product not found with id: "
                                                + productId
                                )
                        );

        ReviewResponse review =
                reviewService.createReview(
                        request,
                        user,
                        product
                );

        return new ResponseEntity<>(
                review,
                HttpStatus.CREATED
        );

    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody CreateReviewRequest request,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        ReviewResponse review =
                reviewService.updateReview(
                        reviewId,
                        request,
                        user.getId()
                );

        return ResponseEntity.ok(review);

    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {

        User user =
                userService.findUserByJwtToken(jwt);

        reviewService.deleteReview(
                reviewId,
                user.getId()
        );

        return ResponseEntity.noContent().build();

    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(
            @PathVariable Long reviewId
    ) {

        ReviewResponse review =
                reviewService.getReviewById(reviewId);

        return ResponseEntity.ok(review);

    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByProduct(
            @PathVariable Long productId
    ) {

        List<ReviewResponse> reviews =
                reviewService.getReviewsByProduct(productId);

        return ResponseEntity.ok(reviews);

    }

}