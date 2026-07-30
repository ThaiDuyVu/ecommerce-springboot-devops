package com.project.ecommerce.Specification;

import com.project.ecommerce.Model.Product;
import com.project.ecommerce.Request.ProductFilterRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;


public class ProductSpecification {


    public static Specification<Product> filter(
            ProductFilterRequest request
    ) {


        return (root, query, cb) -> {


            List<Predicate> predicates =
                    new ArrayList<>();

            predicates.add(
                    cb.equal(
                            root.get("active"),
                            true
                    )
            );


            if(request.getKeyword() != null
                    && !request.getKeyword().isBlank()) {


                String keyword =
                        "%" +
                                request.getKeyword()
                                        .toLowerCase()
                                +
                                "%";


                Join<Product, ?> category =
                        root.join(
                                "category",
                                JoinType.LEFT
                        );


                Predicate title =
                        cb.like(
                                cb.lower(
                                        root.get("title")
                                ),
                                keyword
                        );


                Predicate description =
                        cb.like(
                                cb.lower(
                                        root.get("description")
                                ),
                                keyword
                        );


                Predicate categoryName =
                        cb.like(
                                cb.lower(
                                        category.get("name")
                                ),
                                keyword
                        );


                predicates.add(
                        cb.or(
                                title,
                                description,
                                categoryName
                        )
                );
            }

            if(request.getCategoryId() != null) {


                predicates.add(
                        cb.equal(
                                root
                                        .get("category")
                                        .get("id"),
                                request.getCategoryId()
                        )
                );

            }

            if(request.getMinPrice() != null) {


                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("sellingPrice"),
                                request.getMinPrice()
                        )
                );

            }

            if(request.getMaxPrice() != null) {


                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("sellingPrice"),
                                request.getMaxPrice()
                        )
                );

            }

            if(request.getColor() != null
                    && !request.getColor().isBlank()) {


                predicates.add(
                        cb.equal(
                                cb.lower(
                                        root.get("color")
                                ),
                                request.getColor()
                                        .toLowerCase()
                        )
                );

            }

            if(request.getSize() != null
                    && !request.getSize().isBlank()) {


                Join<Product, String> sizeJoin =
                        root.join(
                                "sizes"
                        );


                predicates.add(
                        cb.equal(
                                sizeJoin,
                                request.getSize()
                        )
                );


                query.distinct(true);

            }



            /*
             * Discount
             */
            if(request.getMinDiscount() != null) {


                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("discountPercent"),
                                request.getMinDiscount()
                        )
                );

            }


            if(Boolean.TRUE.equals(
                    request.getInStock()
            )) {


                predicates.add(
                        cb.greaterThan(
                                root.get("quantity"),
                                0
                        )
                );

            }


            return cb.and(
                    predicates.toArray(
                            new Predicate[0]
                    )
            );
        };

    }

}