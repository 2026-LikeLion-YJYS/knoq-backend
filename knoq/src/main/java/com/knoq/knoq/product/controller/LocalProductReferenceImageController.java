package com.knoq.knoq.product.controller;

import com.knoq.knoq.product.service.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Hidden
@Profile("local")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class LocalProductReferenceImageController {

    private final ProductService productService;

    // 데모 준비용: 제품 인식에 사용할 기준 사진을 local 환경에서만 등록한다.
    @PostMapping("/{productId}/reference-image")
    public ResponseEntity<Void> registerReferenceImage(
            @PathVariable String productId,
            @RequestParam("image") MultipartFile image
    ) {
        productService.registerReferenceImage(productId, image);
        return ResponseEntity.noContent().build();
    }
}
