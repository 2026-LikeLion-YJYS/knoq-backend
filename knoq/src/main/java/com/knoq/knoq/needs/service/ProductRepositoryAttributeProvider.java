package com.knoq.knoq.needs.service;

import com.knoq.knoq.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAttributeProvider implements ProductAttributeProvider {

    private final ProductRepository productRepository;

    @Override
    public List<ProductAttributes> getAttributes(List<String> productIds) {
        return productRepository.findAllById(productIds).stream()
                .map(product -> new ProductAttributes(
                        product.getId(),
                        null,
                        product.getMaterial(),
                        product.getColors(),
                        product.getSizes()
                ))
                .toList();
    }
}