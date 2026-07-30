package com.project.ecommerce.Service.impl;

import com.project.ecommerce.Exceptions.DuplicateResourceException;
import com.project.ecommerce.Exceptions.ResourceNotFoundException;
import com.project.ecommerce.Model.Category;
import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Model.Seller;
import com.project.ecommerce.Repository.CategoryRepository;
import com.project.ecommerce.Repository.ProductRepository;
import com.project.ecommerce.Request.CreateProductRequest;
import com.project.ecommerce.Request.ProductFilterRequest;
import com.project.ecommerce.Request.UpdateProductRequest;
import com.project.ecommerce.Response.ProductResponse;
import com.project.ecommerce.Service.ProductService;
import com.project.ecommerce.Specification.ProductSpecification;
import com.project.ecommerce.Utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
// còn thương thức chưa add vào : Không cho xóa nếu category còn Product
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(
            CreateProductRequest request,
            Seller seller
    ) {


        if(request.getMrpPrice() == null
                || request.getSellingPrice() == null){

            throw new IllegalArgumentException(
                    "Price cannot be null"
            );
        }


        if(request.getMrpPrice() <= 0
                || request.getSellingPrice() <= 0){

            throw new IllegalArgumentException(
                    "Price must be greater than zero"
            );
        }


        if(request.getSellingPrice()
                > request.getMrpPrice()){

            throw new IllegalArgumentException(
                    "Selling price cannot be greater than MRP price"
            );
        }



        Category category =
                categoryRepository.findById(
                                request.getCategoryId()
                        )
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Category not found with id: "
                                                + request.getCategoryId()
                                )
                        );



        String slug =
                SlugUtil.toSlug(
                        request.getTitle()
                );



        Optional<Product> oldProduct =
                productRepository.findBySlugAndSeller(
                        slug,
                        seller
                );



    /*
        Restore product cũ
     */
        if(oldProduct.isPresent()){


            Product old = oldProduct.get();


            if(!old.getActive()){


                old.setActive(true);

                old.setTitle(
                        request.getTitle().trim()
                );


                old.setDescription(
                        request.getDescription()
                );


                old.setMrpPrice(
                        request.getMrpPrice()
                );


                old.setSellingPrice(
                        request.getSellingPrice()
                );


                old.setQuantity(
                        request.getQuantity() == null
                                ? 0
                                : request.getQuantity()
                );


                old.setColor(
                        request.getColor()
                );


                old.setImages(
                        request.getImages() == null
                                ? new ArrayList<>()
                                : request.getImages()
                );


                old.setSizes(
                        request.getSizes() == null
                                ? new ArrayList<>()
                                : request.getSizes()
                );


                old.setCategory(
                        category
                );


                old.setDiscountPercent(
                        calculateDiscount(
                                request.getMrpPrice(),
                                request.getSellingPrice()
                        )
                );


                return mapToResponse(
                        productRepository.save(old)
                );

            }


            throw new DuplicateResourceException(
                    "Product already exists"
            );
        }




    /*
        Create product mới
     */

        Product product = new Product();


        product.setTitle(
                request.getTitle().trim()
        );


        product.setSlug(
                slug
        );


        product.setDescription(
                request.getDescription()
        );


        product.setMrpPrice(
                request.getMrpPrice()
        );


        product.setSellingPrice(
                request.getSellingPrice()
        );


        product.setQuantity(
                request.getQuantity() == null
                        ? 0
                        : request.getQuantity()
        );


        product.setColor(
                request.getColor()
        );


        product.setImages(
                request.getImages() == null
                        ? new ArrayList<>()
                        : request.getImages()
        );


        product.setSizes(
                request.getSizes() == null
                        ? new ArrayList<>()
                        : request.getSizes()
        );


        product.setCategory(
                category
        );


        product.setSeller(
                seller
        );


        product.setDiscountPercent(
                calculateDiscount(
                        request.getMrpPrice(),
                        request.getSellingPrice()
                )
        );


        Product saved =
                productRepository.save(product);


        return mapToResponse(saved);

    }

    @Override
    public ProductResponse updateProduct(
            Long productId,
            UpdateProductRequest request
    ) {


        Product product =
                productRepository.findById(productId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Product not found with id: "
                                                + productId
                                )
                        );



        /*
         * Update title + slug
         */
        if(request.getTitle() != null
                && !request.getTitle().isBlank()){


            String newTitle =
                    request.getTitle().trim();


            String newSlug =
                    SlugUtil.toSlug(newTitle);



            Optional<Product> duplicate =
                    productRepository.findBySlugAndSeller(
                            newSlug,
                            product.getSeller()
                    );


            if(duplicate.isPresent()
                    && !duplicate.get().getId().equals(productId)){

                throw new DuplicateResourceException(
                        "Product already exists for this seller"
                );
            }


            product.setTitle(newTitle);
            product.setSlug(newSlug);

        }



        /*
         * Update description
         */
        if(request.getDescription() != null){

            product.setDescription(
                    request.getDescription()
            );

        }



        /*
         * Update price
         */
        if(request.getMrpPrice() != null){

            product.setMrpPrice(
                    request.getMrpPrice()
            );

        }


        if(request.getSellingPrice() != null){

            product.setSellingPrice(
                    request.getSellingPrice()
            );

        }



        /*
         * Validate price
         */
        if(product.getMrpPrice() == null
                || product.getSellingPrice() == null){

            throw new IllegalArgumentException(
                    "Price cannot be null"
            );

        }


        if(product.getMrpPrice() <= 0
                || product.getSellingPrice() <= 0){


            throw new IllegalArgumentException(
                    "Price must be greater than zero"
            );

        }



        if(product.getSellingPrice()
                > product.getMrpPrice()){


            throw new IllegalArgumentException(
                    "Selling price cannot be greater than MRP price"
            );

        }



        /*
         * Update discount
         */
        product.setDiscountPercent(
                calculateDiscount(
                        product.getMrpPrice(),
                        product.getSellingPrice()
                )
        );




        /*
         * Update quantity
         */
        if(request.getQuantity() != null){


            if(request.getQuantity() < 0){

                throw new IllegalArgumentException(
                        "Quantity cannot be negative"
                );

            }


            product.setQuantity(
                    request.getQuantity()
            );

        }




        /*
         * Update color
         */
        if(request.getColor() != null){

            product.setColor(
                    request.getColor()
            );

        }




        /*
         * Update images
         */
        if(request.getImages() != null){

            product.setImages(
                    request.getImages()
            );

        }




        /*
         * Update sizes
         */
        if(request.getSizes() != null){

            product.setSizes(
                    request.getSizes()
            );

        }




        /*
         * Update category
         */
        if(request.getCategoryId() != null){


            Category category =
                    categoryRepository.findById(
                                    request.getCategoryId()
                            )
                            .orElseThrow(
                                    () -> new ResourceNotFoundException(
                                            "Category not found with id: "
                                                    + request.getCategoryId()
                                    )
                            );


            if(!category.getActive()){

                throw new IllegalArgumentException(
                        "Cannot assign inactive category"
                );

            }


            product.setCategory(category);

        }




        /*
         * Update active status
         */
        if(request.getActive() != null){

            product.setActive(
                    request.getActive()
            );

        }



        Product updated =
                productRepository.save(product);



        return mapToResponse(updated);

    }

    @Override
    public void deleteProduct(Long productId){

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Product not found"
                                )
                        );


        if(!product.getActive()){

            return;
        }


        product.setActive(false);


        productRepository.save(product);

    }

    @Override
    public ProductResponse getProductById(Long productId) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Product not found with id: "
                                                + productId
                                )
                        );


        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> getProductsBySeller(Long sellerId) {


        return productRepository
                .findBySeller_Id(sellerId)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    @Override
    public Page<ProductResponse> getProducts(
            ProductFilterRequest filter
    ) {


        Pageable pageable =
                PageRequest.of(
                        filter.getPageNumber(),
                        filter.getPageSize(),
                        getSort(filter.getSort())
                );


        return productRepository
                .findAll(
                        ProductSpecification.filter(filter),
                        pageable
                )
                .map(this::mapToResponse);

    }

    @Override
    public Product findProductEntityById(
            Long productId
    ) {

        return productRepository.findById(productId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Product not found with id: "
                                        + productId
                        )
                );
    }

    private Sort getSort(String sort) {


        if(sort == null){
            return Sort.by(
                    "createdAt"
            ).descending();
        }


        return switch(sort){


            case "price_asc" ->
                    Sort.by(
                            "sellingPrice"
                    ).ascending();


            case "price_desc" ->
                    Sort.by(
                            "sellingPrice"
                    ).descending();


            case "rating" ->
                    Sort.by(
                            "averageRating"
                    ).descending();



            default ->
                    Sort.by(
                            "createdAt"
                    ).descending();

        };

    }
    private ProductResponse mapToResponse(Product product) {

        return ProductResponse.builder()
                .id(product.getId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .description(product.getDescription())

                .mrpPrice(product.getMrpPrice())
                .sellingPrice(product.getSellingPrice())
                .discountPercent(product.getDiscountPercent())

                .quantity(product.getQuantity())

                .color(product.getColor())

                .sizes(product.getSizes())
                .images(product.getImages())

                .averageRating(product.getAverageRating())
                .numRatings(product.getNumRatings())

                .active(product.getActive())

                .categoryId(
                        product.getCategory() == null
                                ? null
                                : product.getCategory().getId()
                )

                .categoryName(
                        product.getCategory() == null
                                ? null
                                : product.getCategory().getName()
                )

                .sellerId(
                        product.getSeller() == null
                                ? null
                                : product.getSeller().getId()
                )

                .sellerName(
                        product.getSeller() == null
                                ? null
                                : product.getSeller().getSellerName()
                )

                .createdAt(product.getCreatedAt())

                .build();
    }
    private Integer calculateDiscount(
            Integer mrpPrice,
            Integer sellingPrice
    ){

        if(mrpPrice == null
                || sellingPrice == null
                || mrpPrice == 0){

            return 0;
        }


        return (int)(
                (
                        (double)
                                (mrpPrice - sellingPrice)
                                /
                                mrpPrice
                )
                        * 100
        );

    }
}
