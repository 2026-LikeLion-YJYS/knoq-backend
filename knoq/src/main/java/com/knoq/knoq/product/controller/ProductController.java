package com.knoq.knoq.product.controller;

import com.knoq.knoq.product.dto.ProductDetailResponse;
import com.knoq.knoq.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable String productId) {
        return ResponseEntity.ok(productService.getProductDetail(productId));
    }

    // 데모 준비용: 제품 기준 사진을 등록. FR-200 인식 요청 때 이 사진이 GPT 비전 호출에 쓰임
    @PostMapping("/{productId}/reference-image")
    public ResponseEntity<Void> registerReferenceImage(@PathVariable String productId,
                                                       @RequestParam("image") MultipartFile image) {
        productService.registerReferenceImage(productId, image);
        return ResponseEntity.noContent().build();
    }
}