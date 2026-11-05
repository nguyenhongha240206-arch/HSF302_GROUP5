package fu.fruitstore.product.services;

import fu.fruitstore.product.dto.ProductDto;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    Page<Product> filterProducts(Long categoryId, ProductType productType, String keyword,
                                 BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<Product> searchForAdmin(String keyword, Pageable pageable);
    List<Product> getFeaturedProducts();
    List<Product> getNewestProducts();
    List<Product> getGiftBaskets();
    Product getById(Long id);
    Product create(ProductDto dto);
    Product update(Long id, ProductDto dto);
    void delete(Long id);
    void adjustStock(Long id, int quantityChange);
    long countLowStock(int threshold);
    List<Product> getLowStockProducts(int threshold);
}

