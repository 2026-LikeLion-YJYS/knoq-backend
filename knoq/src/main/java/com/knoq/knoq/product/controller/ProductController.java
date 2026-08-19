package com.knoq.knoq.product.controller;

import com.knoq.knoq.product.dto.ProductDetailResponse;
import com.knoq.knoq.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product", description = "제품 상세 조회 API")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    @Operation(summary = "제품 상세 조회", description = "제품의 소재, 특징, 가격, 사이즈, 색상, 대표 이미지와 AI 요약을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제품 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "제품을 찾을 수 없음")
    })
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @Parameter(example = "prod_1") @PathVariable String productId
    ) {
        return ResponseEntity.ok(productService.getProductDetail(productId));
    }
}
