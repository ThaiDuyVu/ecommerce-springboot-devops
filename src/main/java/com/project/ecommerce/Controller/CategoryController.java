package com.project.ecommerce.Controller;

import com.project.ecommerce.Response.CategoryResponse;
import com.project.ecommerce.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );

    }

    @GetMapping("/root")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {

        return ResponseEntity.ok(
                categoryService.getRootCategories()
        );

    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(id)
        );

    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getChildren(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                categoryService.getChildren(id)
        );

    }

}