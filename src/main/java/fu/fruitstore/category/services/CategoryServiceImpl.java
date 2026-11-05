package fu.fruitstore.category.services;

import fu.fruitstore.category.dto.CategoryDto;
import fu.fruitstore.category.entity.Category;
import fu.fruitstore.category.repository.CategoryRepository;
import fu.fruitstore.shared.exception.BadRequestException;
import fu.fruitstore.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllActive() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(String keyword, Pageable pageable) {
        return categoryRepository.searchCategories(keyword, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
    }

    @Override
    @Transactional
    public Category create(CategoryDto dto) {
        if (categoryRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Mã danh mục '" + dto.getCode() + "' đã tồn tại!");
        }

        Category category = Category.builder()
                .name(dto.getName())
                .code(dto.getCode().toUpperCase().trim())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .displayOrder(dto.getDisplayOrder())
                .active(dto.isActive())
                .build();

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryDto dto) {
        Category category = getById(id);

        if (!category.getCode().equalsIgnoreCase(dto.getCode()) && categoryRepository.existsByCode(dto.getCode())) {
            throw new BadRequestException("Mã danh mục '" + dto.getCode() + "' đã tồn tại!");
        }

        category.setName(dto.getName());
        category.setCode(dto.getCode().toUpperCase().trim());
        category.setDescription(dto.getDescription());
        category.setImageUrl(dto.getImageUrl());
        category.setDisplayOrder(dto.getDisplayOrder());
        category.setActive(dto.isActive());

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        // Soft delete
        category.setDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }
}

