package com.project.ecommerce.Service;

import com.project.ecommerce.Model.Category;
import com.project.ecommerce.Request.CreateCategoryRequest;
import com.project.ecommerce.Request.UpdateCategoryRequest;
import com.project.ecommerce.Response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long id,
                                    UpdateCategoryRequest request);

    CategoryResponse getCategoryById(Long id);
    void deleteCategory(Long id);
    List<CategoryResponse> getRootCategories();

    List<CategoryResponse> getChildren(Long parentId);

    List<CategoryResponse> getAllCategories();
}