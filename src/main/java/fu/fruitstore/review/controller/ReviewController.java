package fu.fruitstore.review.controller;

import fu.fruitstore.review.dto.ReviewDto;
import fu.fruitstore.review.entity.Review;
import fu.fruitstore.review.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public String myReviews(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        List<Review> reviews = reviewService.getReviewsByUser(userDetails.getUsername());
        model.addAttribute("reviews", reviews);
        return "store/my-reviews";
    }

    // 1. Giữ nguyên endpoint đánh giá từ trang Chi tiết sản phẩm (product-detail.html)
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated()")
    public String addReview(@AuthenticationPrincipal UserDetails userDetails,
                            @Valid @ModelAttribute("reviewDto") ReviewDto reviewDto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu đánh giá không hợp lệ!");
            return "redirect:/products/" + reviewDto.getProductId();
        }

        try {
            reviewService.addReview(userDetails.getUsername(), reviewDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá cho sản phẩm!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/products/" + reviewDto.getProductId();
    }

    // 2. Endpoint cho UC-15: Đánh giá & Gửi ảnh khiếu nại quả hỏng từ Chi tiết đơn hàng (order-detail.html)
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public String submitReviewOrComplaint(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @ModelAttribute("reviewDto") ReviewDto reviewDto,
                                          BindingResult bindingResult,
                                          @RequestParam(value = "orderNumber", required = false) String orderNumber,
                                          @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                          RedirectAttributes redirectAttributes) {
        String redirectUrl = (orderNumber != null && !orderNumber.isBlank())
                ? "redirect:/orders/" + orderNumber
                : "redirect:/products/" + reviewDto.getProductId();

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn số sao và nhập nội dung nhận xét/khiếu nại hợp lệ!");
            return redirectUrl;
        }

        try {
            // Xử lý lưu file ảnh báo cáo quả bị dập nát / hư hỏng (nếu khách có đính kèm)
            if (imageFile != null && !imageFile.isEmpty()) {
                String savedImageUrl = saveComplaintImage(imageFile);
                reviewDto.setImageUrl(savedImageUrl);
            }

            reviewService.addReview(userDetails.getUsername(), reviewDto);

            // ĐÃ SỬA Ở ĐÂY: Bỏ kiểm tra != null vì getRating() là kiểu int
            boolean isComplaint = (reviewDto.getRating() <= 2)
                    || (imageFile != null && !imageFile.isEmpty());

            if (isComplaint) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã gửi báo cáo khiếu nại quả hỏng kèm minh chứng thành công! Cửa hàng sẽ liên hệ đổi bù/hoàn tiền sớm nhất.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Cảm ơn bạn đã gửi đánh giá chất lượng sản phẩm!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return redirectUrl;
    }

    // Hàm lưu file ảnh vào thư mục static/uploads/reviews/
    private String saveComplaintImage(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "damage.jpg"
        );
        String fileName = UUID.randomUUID() + "_" + originalFilename.replaceAll("\\s+", "_");

        Path uploadPath = Paths.get("src/main/resources/static/uploads/reviews");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Path targetUploadPath = Paths.get("target/classes/static/uploads/reviews");
        if (!Files.exists(targetUploadPath)) {
            Files.createDirectories(targetUploadPath);
        }
        Files.copy(filePath, targetUploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/reviews/" + fileName;
    }
}
