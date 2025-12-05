package com.system.brands.Repository;

import com.system.brands.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByBrandId(Integer brandId);

    List<Product> findByCategoryId(Integer categoryId);

    @Query(value = "SELECT * FROM products ORDER BY priority ASC NULLS LAST, id ASC", nativeQuery = true)
    List<Product> findAllOrderedByProductOrder();

    @Query(value = "SELECT * FROM products WHERE brand_id = :brandId ORDER BY priority ASC NULLS LAST, id ASC", nativeQuery = true)
    List<Product> findByBrandIdOrderedByProductOrder(@Param("brandId") Integer brandId);

    @Query(value = "SELECT * FROM products WHERE category_id = :categoryId ORDER BY priority ASC NULLS LAST, id ASC", nativeQuery = true)
    List<Product> findByCategoryIdOrderedByProductOrder(@Param("categoryId") Integer categoryId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.productOrder = p.productOrder + 1 WHERE p.productOrder >= :minOrder AND p.productOrder < :maxOrder")
    void shiftOrdersUp(@Param("minOrder") Integer minOrder, @Param("maxOrder") Integer maxOrder);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Product p SET p.productOrder = p.productOrder - 1 WHERE p.productOrder > :minOrder AND p.productOrder <= :maxOrder")
    void shiftOrdersDown(@Param("minOrder") Integer minOrder, @Param("maxOrder") Integer maxOrder);
}
