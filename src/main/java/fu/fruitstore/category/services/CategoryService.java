package fu.fruitstore.category.services;

import fu.fruitstore.category.dto.CategoryDto;
import fu.fruitstore.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    List<Category> getAllActive();
    Page<Category> getAllCategories(String keyword, Pageable pageable);
    Category getById(Long id);
    Category create(CategoryDto dto);
    Category update(Long id, CategoryDto dto);
    void delete(Long id);
}

