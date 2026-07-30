package com.project.ecommerce.Request;

import lombok.Data;

@Data
public class UpdateCategoryRequest {

    private String name;

    private String slug;
    

    private Integer displayOrder;

    private Boolean active;

}