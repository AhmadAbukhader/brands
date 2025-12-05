package com.system.brands.Service;

import com.system.brands.Dto.CategoryRequestDto;
import com.system.brands.Dto.CategoryResponseDto;
import com.system.brands.Exception.DuplicateResourceException;
import com.system.brands.Exception.ResourceNotFoundException;
import com.system.brands.Model.Category;
import com.system.brands.Repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::convertToCategoryResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDto getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return convertToCategoryResponseDto(category);
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto requestDto) {
        if (categoryRepository.existsByName(requestDto.getName())) {
            throw new DuplicateResourceException("Category", "name", requestDto.getName());
        }

        Category category = Category.builder()
                .name(requestDto.getName())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return convertToCategoryResponseDto(savedCategory);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Integer id, CategoryRequestDto requestDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if new name already exists for another category
        if (!category.getName().equals(requestDto.getName()) &&
                categoryRepository.existsByName(requestDto.getName())) {
            throw new DuplicateResourceException("Category", "name", requestDto.getName());
        }

        category.setName(requestDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return convertToCategoryResponseDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }

    private CategoryResponseDto convertToCategoryResponseDto(Category category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
