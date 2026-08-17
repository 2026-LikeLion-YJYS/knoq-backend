package com.knoq.knoq.saved.controller;

import com.knoq.knoq.saved.dto.request.SaveProductRequest;
import com.knoq.knoq.saved.dto.response.SavedProductListResponse;
import com.knoq.knoq.saved.dto.response.SavedProductResponse;
import com.knoq.knoq.saved.service.SavedProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Saved Product", description = "저장한 제품 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/sessions/{sessionId}/saved-products")
public class SavedProductController {

    private final SavedProductService savedProductService;

    @Operation(summary = "제품 저장")
    @PostMapping
    public ResponseEntity<SavedProductResponse> saveProduct(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId,
            @Valid @RequestBody SaveProductRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        SavedProductResponse.from(
                                savedProductService.saveFromCamera(
                                        sessionId,
                                        request.getProductId()
                                )
                        )
                );
    }

    @Operation(summary = "저장 목록 조회")
    @GetMapping
    public ResponseEntity<SavedProductListResponse> getSavedProducts(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId
    ) {

        return ResponseEntity.ok(
                SavedProductListResponse.from(
                        savedProductService.findAll(sessionId)
                )
        );
    }

    @Operation(summary = "저장 제품 삭제")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteSavedProduct(
            @Parameter(example = "sess_abc123") @PathVariable String sessionId,
            @PathVariable String productId
    ) {
        savedProductService.delete(sessionId, productId);
        return ResponseEntity.noContent().build();
    }
}