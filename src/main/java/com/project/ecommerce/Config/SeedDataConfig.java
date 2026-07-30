package com.project.ecommerce.Config;

import com.project.ecommerce.Enums.AccountStatus;
import com.project.ecommerce.Enums.USER_ROLE;
import com.project.ecommerce.Model.Category;
import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Model.User;
import com.project.ecommerce.Repository.CategoryRepository;
import com.project.ecommerce.Repository.ProductRepository;
import com.project.ecommerce.Repository.SellerRepository;
import com.project.ecommerce.Repository.UserRepository;
import com.project.ecommerce.Utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Profile("dev")
@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {
    private final UserRepository userRepository;

    private final SellerRepository sellerRepository;

    private final CategoryRepository categoryRepository;

    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    @Bean
    CommandLineRunner seedDatabase() {
        return args -> {

            seedAdmin();

            seedCustomer();

            seedSeller();

            seedCategories();

            seedProducts();
        };
    }
    private void seedAdmin() {

        if (userRepository.findByEmail("admin@gmail.com") != null) {
            return;
        }

        User admin = new User();

        admin.setUsername("admin");

        admin.setFullName("System Administrator");

        admin.setEmail("admin@gmail.com");

        admin.setPassword(
                passwordEncoder.encode("12345678")
        );

        admin.setRole(USER_ROLE.ROLE_ADMIN);

        userRepository.save(admin);

    }
    private void seedCustomer() {

        if (userRepository.findByEmail(
                "duykimthao010101@gmail.com.com"
        ) != null) {
            return;
        }

        User customer = new User();

        customer.setUsername("customer");

        customer.setFullName("Demo Customer");

        customer.setEmail(
                "duykimthao010101@gmail.com"
        );

        customer.setPassword(
                passwordEncoder.encode("12345678")
        );

        customer.setRole(USER_ROLE.ROLE_CUSTOMER);

        userRepository.save(customer);

    }
    private void seedSeller() {

        if (sellerRepository.findByEmail(
                "nguyenthithuyduong021105@gmail.com"
        ) != null) {
            return;
        }

        Seller seller = new Seller();

        seller.setSellerName("Demo Seller");

        seller.setEmail(
                "nguyenthithuyduong021105@gmail.com"
        );

        seller.setPassword(
                passwordEncoder.encode("12345678")
        );

        seller.setCreatedAt(LocalDateTime.now());

        seller.setEmailVerified(true);

        seller.setAccountStatus(
                AccountStatus.ACTIVE
        );

        sellerRepository.save(seller);

    }
    private void seedCategories() {

        if (categoryRepository.count() > 0) {
            return;
        }

        Category electronics = new Category();

        electronics.setName("Electronics");

        electronics.setSlug("electronics");

        electronics.setLevel(1);

        electronics.setActive(true);

        electronics.setDisplayOrder(0);

        electronics = categoryRepository.save(electronics);

        Category laptop = new Category();

        laptop.setName("Laptop");

        laptop.setSlug("laptop");

        laptop.setLevel(2);

        laptop.setParent(electronics);

        laptop.setActive(true);

        laptop.setDisplayOrder(0);

        categoryRepository.save(laptop);

    }
    private void seedProducts() {

        Seller seller =
                sellerRepository.findByEmail(
                        "nguyenthithuyduong021105@gmail.com"
                );

        if (seller == null) {
            return;
        }

        Category laptop =
                categoryRepository.findBySlug("laptop")
                        .orElse(null);

        if (laptop == null) {
            return;
        }

        seedProduct(
                seller,
                laptop,
                "MacBook Air M3 13\"",
                30000000,
                27990000,
                20,
                "Silver",
                List.of("13 inch"),
                List.of(
                        "https://example.com/macbook-air-m3-front.jpg",
                        "https://example.com/macbook-air-m3-side.jpg"
                )
        );

        seedProduct(
                seller,
                laptop,
                "Dell XPS 15",
                42000000,
                39990000,
                10,
                "Black",
                List.of("15 inch"),
                List.of(
                        "https://example.com/dell-xps15-front.jpg",
                        "https://example.com/dell-xps15-side.jpg"
                )
        );

    }
    private void seedProduct(

            Seller seller,
            Category category,

            String title,

            Integer mrpPrice,
            Integer sellingPrice,

            Integer quantity,

            String color,

            List<String> sizes,

            List<String> images

    ) {

        String slug = SlugUtil.toSlug(title);

        if (productRepository.findBySlugAndSeller(slug, seller).isPresent()) {
            return;
        }

        Product product = new Product();

        product.setTitle(title);

        product.setSlug(slug);

        product.setDescription(
                title + " Demo Product"
        );

        product.setMrpPrice(mrpPrice);

        product.setSellingPrice(sellingPrice);

        product.setQuantity(quantity);

        product.setColor(color);

        product.setSizes(
                new ArrayList<>(sizes)
        );

        product.setImages(
                new ArrayList<>(images)
        );

        product.setCategory(category);

        product.setSeller(seller);

        product.setDiscountPercent(
                calculateDiscount(
                        mrpPrice,
                        sellingPrice
                )
        );

        product.setAverageRating(0.0);

        product.setNumRatings(0);

        product.setActive(true);

        productRepository.save(product);

    }
    private Integer calculateDiscount(
            Integer mrpPrice,
            Integer sellingPrice
    ) {

        if (mrpPrice == null
                || sellingPrice == null
                || mrpPrice == 0) {

            return 0;
        }

        return (int) (
                ((double) (mrpPrice - sellingPrice) / mrpPrice) * 100
        );

    }
}