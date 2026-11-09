package fu.fruitstore.wishlist.services;

import fu.fruitstore.auth.entity.User;
import fu.fruitstore.auth.repository.UserRepository;
import fu.fruitstore.product.entity.Product;
import fu.fruitstore.product.repository.ProductRepository;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import fu.fruitstore.wishlist.entity.WishlistItem;
import fu.fruitstore.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<WishlistItem> getItems(String email) {
        return wishlistRepository.findItemsByEmail(email);
    }

    @Transactional(readOnly = true)
    public Set<Long> getProductIds(String email) {
        return getItems(email).stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public boolean contains(String email, Long productId) {
        return wishlistRepository.existsByUser_EmailAndProduct_Id(email, productId);
    }

    @Transactional
    public void toggle(String email, Long productId) {
        var existing = wishlistRepository.findByUser_EmailAndProduct_Id(email, productId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);
        wishlistRepository.save(item);
    }
}
