package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Exceptions.DuplicateResourceException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.Category;
import com.project.ecommerce.Repository.CategoryRepository;
import com.project.ecommerce.Request.CreateCategoryRequest;
import com.project.ecommerce.Request.UpdateCategoryRequest;
import com.project.ecommerce.Response.CategoryResponse;
import com.project.ecommerce.Service.CategoryService;
import com.project.ecommerce.Utils.SlugUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        String name = request.getName().trim();

        String slug = SlugUtil.toSlug(name);

        categoryRepository.findBySlug(slug)
                .ifPresent(category -> {
                    throw new DuplicateResourceException(
                            "Category already exists"
                    );
                });

        Category category = new Category();

        category.setName(name);
        category.setSlug(slug);
        category.setDisplayOrder(
                request.getDisplayOrder() == null
                        ? 0
                        : request.getDisplayOrder()
        );

        if (request.getParentId() == null) {

            category.setParent(null);
            category.setLevel(1);

        } else {

            Category parent =
                    findCategoryEntity(request.getParentId());

            if (parent.getLevel() >= 2) {
                throw new IllegalArgumentException(
                        "Maximum category depth is 2"
                );
            }
            category.setParent(parent);
            category.setLevel(parent.getLevel() + 1);

        }

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }
    @Override
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request){

        Category category = findCategoryEntity(id);

        if (request.getName() != null
                && !request.getName().isBlank()) {

            category.setName(request.getName());
        }
        if (request.getSlug() != null
                && !request.getSlug().isBlank()) {

            String slug = request.getSlug().trim().toLowerCase();

            categoryRepository.findBySlug(slug)
                    .ifPresent(existing -> {

                        if (!existing.getId().equals(id)) {

                            throw new IllegalArgumentException(
                                    "Slug already exists"
                            );

                        }

                    });
            category.setSlug(slug);
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(
                    request.getDisplayOrder()
            );

        }
        if (request.getActive() != null) {

            category.setActive(
                    request.getActive()
            );

        }

        Category updated = categoryRepository.save(category);

        return mapToResponse(updated);
    }

    @Override
    public void deleteCategory(Long id) {

        Category category = findCategoryEntity(id);

        if (categoryRepository.existsByParent(category)) {

            throw new IllegalArgumentException(
                    "Cannot delete category because it has child categories"
            );

        }
        // Không cho xóa nếu category còn Product

        categoryRepository.delete(category);

    }
    @Override
    public CategoryResponse getCategoryById(Long id) {

        return mapToResponse(
                findCategoryEntity(id)
        );

    }

    @Override
    public List<CategoryResponse> getRootCategories() {

        return categoryRepository
                .findByParentIsNullOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public List<CategoryResponse> getChildren(Long parentId) {

        Category parent = findCategoryEntity(parentId);

        return categoryRepository
                .findByParentOrderByDisplayOrderAscNameAsc(parent)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository
                .findAllByOrderByLevelAscDisplayOrderAscNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    private Category findCategoryEntity(Long id) {

        return categoryRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Category not found with id: " + id
                        )
                );

    }
    private CategoryResponse mapToResponse(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .level(category.getLevel())
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .parentId(
                        category.getParent() == null
                                ? null
                                : category.getParent().getId()
                )
                .build();
    }
}
