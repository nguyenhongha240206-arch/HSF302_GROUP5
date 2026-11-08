package fu.fruitstore.review.services;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.services.UserService;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.services.ProductService;
import fu.fruitstore.review.dto.ReviewDto;
import fu.fruitstore.review.entity.Review;
import fu.fruitstore.review.repository.ReviewRepository;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public List<Review> getReviewsByUser(String email) {
        return reviewRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        if (avg == null) {
            return 0.0;
        }
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

        // Kiểm tra xem user đã có đánh giá cho sản phẩm này chưa
        // Nếu đã có (ví dụ khách cập nhật lại đánh giá sang Khiếu nại quả hỏng kèm ảnh), ta cập nhật bản ghi đó
        List<Review> existingReviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(product.getId());
        Optional<Review> userExistingReview = existingReviews.stream()
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .findFirst();

        if (userExistingReview.isPresent()) {
            Review review = userExistingReview.get();
            review.setRating(dto.getRating());
            review.setComment(dto.getComment());
            if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
                review.setImageUrl(dto.getImageUrl());
            }
            return reviewRepository.save(review);
        }

        // Tạo mới đánh giá / báo cáo quả hỏng (UC-15)
        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .imageUrl(dto.getImageUrl()) // Lưu đường dẫn hình ảnh quả hỏng
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
