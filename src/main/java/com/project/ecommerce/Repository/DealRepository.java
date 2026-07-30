package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Long> {
}
