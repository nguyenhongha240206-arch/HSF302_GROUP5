package fu.fruitstore.review.services;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.review.dto.ReviewDto;
import fu.fruitstore.review.entity.Review;
import fu.fruitstore.review.repository.ReviewRepository;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserService userService;
    private final ProductService productService;

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        return Math.round(avg * 10.0) / 10.0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByProduct(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    @Override
    @Transactional
    public Review addReview(String userEmail, ReviewDto dto) {
        User user = userService.findByEmail(userEmail);
        Product product = productService.getById(dto.getProductId());

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new BadRequestException("Bạn đã đánh giá sản phẩm này rồi!");
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá"));
        // Soft delete
        review.setDeleted(true);
        review.setDeletedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }
}

