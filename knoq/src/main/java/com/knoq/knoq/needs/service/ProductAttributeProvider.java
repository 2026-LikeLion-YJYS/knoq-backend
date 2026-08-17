package com.knoq.knoq.needs.service;

import java.util.List;

public interface ProductAttributeProvider {
    List<ProductAttributes> getAttributes(List<String> productIds);
}