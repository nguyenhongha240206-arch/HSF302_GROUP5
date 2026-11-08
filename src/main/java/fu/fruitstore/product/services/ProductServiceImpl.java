package fu.fruitstore.product.services;

import fu.fruitstore.category.entity.Category;
import fu.fruitstore.category.repository.CategoryRepository;
import fu.fruitstore.product.dto.ProductDto;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import fu.fruitstore.product.repository.ProductRepository;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Product> filterProducts(Long categoryId, ProductType productType, String keyword, String origin,
                                        BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.filterProducts(categoryId, productType, keyword, origin, minPrice, maxPrice, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getOrigins() {
        return productRepository.findActiveOrigins();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchForAdmin(String keyword, Pageable pageable) {
        return productRepository.searchForAdmin(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getFeaturedProducts() {
        return productRepository.findByFeaturedTrueAndActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getNewestProducts() {
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getGiftBaskets() {
        return productRepository.findByProductTypeAndActiveTrue(ProductType.GIFT_BASKET);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getById(Long id) {
        return productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Override
    @Transactional
    public Product create(ProductDto dto) {
        if (productRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Mã sản phẩm '" + dto.getCode() + "' đã tồn tại!");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục được chọn"));

        Product product = Product.builder()
                .name(dto.getName())
                .code(dto.getCode().toUpperCase().trim())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .originalPrice(dto.getOriginalPrice())
                .stockQuantity(dto.getStockQuantity())
                .unit(dto.getUnit())
                .origin(dto.getOrigin())
                .imageUrl(dto.getImageUrl())
                .receivedDate(dto.getReceivedDate())
                .productType(dto.getProductType())
                .category(category)
                .featured(dto.isFeatured())
                .active(dto.isActive())
                .build();

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Long id, ProductDto dto) {
        Product product = getById(id);

        if (!product.getCode().equalsIgnoreCase(dto.getCode()) && productRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Mã sản phẩm '" + dto.getCode() + "' đã tồn tại!");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục được chọn"));

        product.setName(dto.getName());
        product.setCode(dto.getCode().toUpperCase().trim());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setUnit(dto.getUnit());
        product.setOrigin(dto.getOrigin());
        product.setImageUrl(dto.getImageUrl());
        product.setReceivedDate(dto.getReceivedDate());
        product.setProductType(dto.getProductType());
        product.setCategory(category);
        product.setFeatured(dto.isFeatured());
        product.setActive(dto.isActive());

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        // Soft delete
        product.setDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void adjustStock(Long id, int quantityChange) {
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        int newQuantity = product.getStockQuantity() + quantityChange;
        if (newQuantity < 0) {
            throw new BadRequestException("Số lượng tồn kho không đủ để thực hiện (còn lại: " + product.getStockQuantity() + ")");
        }
        product.setStockQuantity(newQuantity);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateStock(Long id, int quantity) {
        if (quantity < 0) {
            throw new BadRequestException("Số lượng tồn kho không được nhỏ hơn 0");
        }
        Product product = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        product.setStockQuantity(quantity);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public long countLowStock(int threshold) {
        return productRepository.countLowStockProducts(threshold);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThanEqualAndActiveTrue(threshold);
    }
}

