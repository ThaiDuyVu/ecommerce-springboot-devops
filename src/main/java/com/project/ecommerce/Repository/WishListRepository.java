package com.project.ecommerce.Repository;

import com.project.ecommerce.Model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<Wishlist,Long> {
    Wishlist findByUserId(Long userId);

}
