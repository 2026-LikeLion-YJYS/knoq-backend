package com.knoq.knoq.product.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @Column(length = 64)
    private String id;

    // FR-200 수동 조회("PD-0091" 같은 코드)에서 씀
    @Column(name = "product_code", nullable = false, unique = true, length = 30)
    private String productCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 200)
    private String material;

    private Long price;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_size", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size")
    private List<String> sizes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_color", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "color")
    private List<String> colors = new ArrayList<>();

    @Column(name = "brand_official_description", columnDefinition = "TEXT")
    private String brandOfficialDescription;

    @Column(name = "ai_generated_description", columnDefinition = "TEXT")
    private String aiGeneratedDescription;

    private Product(String id, String productCode, String name, String material, Long price,
                    List<String> sizes, List<String> colors,
                    String brandOfficialDescription, String aiGeneratedDescription) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.material = material;
        this.price = price;
        this.sizes = sizes;
        this.colors = colors;
        this.brandOfficialDescription = brandOfficialDescription;
        this.aiGeneratedDescription = aiGeneratedDescription;
    }

    public static Product of(String id, String productCode, String name, String material, Long price,
                             List<String> sizes, List<String> colors,
                             String brandOfficialDescription, String aiGeneratedDescription) {
        return new Product(id, productCode, name, material, price, sizes, colors,
                brandOfficialDescription, aiGeneratedDescription);
    }
}