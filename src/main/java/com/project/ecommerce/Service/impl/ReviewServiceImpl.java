package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Exceptions.DuplicateResourceException;
import com.project.ecommerce.Exceptions.InvalidOperationException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.Review;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.ProductRepository;
import com.project.ecommerce.Repository.ReviewRepository;
import com.project.ecommerce.Request.CreateReviewRequest;
import com.project.ecommerce.Response.ReviewResponse;
import com.project.ecommerce.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    private final ProductRepository productRepository;


    @Override
    public ReviewResponse createReview(
            CreateReviewRequest request,
            User user,
            Product product
    ) {

        Review existed =
                reviewRepository
                        .findByUser_IdAndProduct_Id(
                                user.getId(),
                                product.getId()
                        )
                        .orElse(null);

        if (existed != null) {
            throw new DuplicateResourceException(
                    "You have already reviewed this product"
            );
        }

        if (request.getRating() < 1
                || request.getRating() > 5) {

            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );
        }

        Review review = new Review();

        review.setReviewText(
                request.getReviewText()
        );

        review.setRating(
                request.getRating()
        );

        review.setImages(
                request.getImages() == null
                        ? new ArrayList<>()
                        : request.getImages()
        );

        review.setUser(user);

        review.setProduct(product);

        Review saved =
                reviewRepository.save(review);

        recalculateProductRating(product);

        return mapToResponse(saved);
    }

    @Override
    public ReviewResponse updateReview(
            Long reviewId,
            CreateReviewRequest request,
            Long userId
    ) {

        Review review =
                reviewRepository.findById(reviewId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Review not found"
                                )
                        );

        if (!review.getUser().getId().equals(userId)) {

            throw new InvalidOperationException(
                    "You cannot update this review"
            );
        }

        if (request.getRating() < 1
                || request.getRating() > 5) {

            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );
        }

        review.setReviewText(
                request.getReviewText()
        );

        review.setRating(
                request.getRating()
        );

        if (request.getImages() != null) {

            review.setImages(request.getImages());

        }

        Review updated =
                reviewRepository.save(review);

        recalculateProductRating(
                review.getProduct()
        );

        return mapToResponse(updated);

    }

    @Override
    public void deleteReview(
            Long reviewId,
            Long userId
    ) {

        Review review =
                reviewRepository.findById(reviewId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Review not found"
                                )
                        );

        if (!review.getUser().getId().equals(userId)) {

            throw new InvalidOperationException(
                    "You cannot delete this review"
            );
        }

        Product product = review.getProduct();

        reviewRepository.delete(review);

        recalculateProductRating(product);

    }

    @Override
    public ReviewResponse getReviewById(
            Long reviewId
    ) {

        Review review =
                reviewRepository.findById(reviewId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Review not found"
                                )
                        );

        return mapToResponse(review);

    }

    @Override
    public List<ReviewResponse> getReviewsByProduct(
            Long productId
    ) {

        if (!productRepository.existsById(productId)) {

            throw new ResourceNotFoundException(
                    "Product not found"
            );

        }

        return reviewRepository
                .findByProduct_Id(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }


    private void recalculateProductRating(Product product) {

        List<Review> reviews =
                reviewRepository.findByProduct_Id(
                        product.getId()
                );

        product.setNumRatings(
                reviews.size()
        );

        if(reviews.isEmpty()){

            product.setAverageRating(0.0);

        }else{

            double average =
                    reviews.stream()
                            .mapToDouble(
                                    Review::getRating
                            )
                            .average()
                            .orElse(0.0);

            product.setAverageRating(average);

        }

        productRepository.save(product);

    }

    private ReviewResponse mapToResponse(
            Review review
    ) {

        return ReviewResponse.builder()
                .id(review.getId())
                .reviewText(review.getReviewText())
                .rating(review.getRating())
                .images(review.getImages())

                .userId(
                        review.getUser().getId()
                )

                .userName(
                        review.getUser().getFullName()
                )

                .productId(
                        review.getProduct().getId()
                )

                .createdAt(
                        review.getCreatedAt()
                )

                .build();

    }
}
