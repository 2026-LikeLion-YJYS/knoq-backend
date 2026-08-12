package com.knoq.knoq.saved.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SaveProductRequest {

    @Schema(example = "prod_12")
    @NotBlank
    private String productId;

}