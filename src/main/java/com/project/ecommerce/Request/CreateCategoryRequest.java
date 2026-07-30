package com.project.ecommerce.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank
    private String name;

    private Long parentId;

    private Integer displayOrder = 0;

}