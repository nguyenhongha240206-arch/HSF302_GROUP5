package fu.fruitstore.review.services;

import fu.fruitstore.review.dto.ReviewDto;
import fu.fruitstore.review.entity.Review;

import java.util.List;

public interface ReviewService {
    List<Review> getReviewsByProduct(Long productId);
    Double getAverageRating(Long productId);
    long countReviewsByProduct(Long productId);
    Review addReview(String userEmail, ReviewDto dto);
    void deleteReview(Long reviewId);
}

