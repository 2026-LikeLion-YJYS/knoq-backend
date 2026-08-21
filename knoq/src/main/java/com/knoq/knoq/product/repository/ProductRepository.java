package com.knoq.knoq.product.repository;

import com.knoq.knoq.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByProductCode(String productCode);
}