package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    boolean existsByParent(Category parent);

//    Optional<Category> findByName(String name);
//
//    List<Category> findByLevel(Integer level);
//
//    List<Category> findByParent(Category parent);

    List<Category> findByParentIsNullOrderByDisplayOrderAscNameAsc();

    List<Category> findByParentOrderByDisplayOrderAscNameAsc(Category parent);
    List<Category> findAllByOrderByLevelAscDisplayOrderAscNameAsc();
}