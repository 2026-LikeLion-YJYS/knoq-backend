package com.knoq.knoq.product.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
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

    @Column(length = 100)
    private String category;

    @Column(length = 200)
    private String material;

    // FR-201 "특징" 항목. material/descriptions와 별도 텍스트 필드
    @Column(length = 500)
    private String features;

    private Long price;

    // FR-200 후보 목록 카드에 쓰는 썸네일
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

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

    // FR-200 임베딩 유사도 매칭용 기준 벡터. 사진 등록 전엔 비어있음 (더 이상 안 씀 — OpenAI 비전 방식으로 대체)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_embedding", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position")
    @Column(name = "value")
    private List<Double> embedding = new ArrayList<>();

    // FR-200 인식용 기준 사진들(base64). 정면/측면/윗면처럼 여러 장 누적 등록 가능 — GPT 비전 호출 시 등록된 사진 전부를 보냄
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_reference_image", joinColumns = @JoinColumn(name = "product_id"))
    @OrderColumn(name = "position")
    @Column(name = "image_base64", columnDefinition = "LONGTEXT")
    private List<String> referenceImages = new ArrayList<>();

    private Product(String id, String productCode, String name, String material, String features, Long price,
                    List<String> sizes, List<String> colors, String thumbnailUrl,
                    String brandOfficialDescription, String aiGeneratedDescription) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.material = material;
        this.features = features;
        this.price = price;
        this.sizes = sizes;
        this.colors = colors;
        this.thumbnailUrl = thumbnailUrl;
        this.brandOfficialDescription = brandOfficialDescription;
        this.aiGeneratedDescription = aiGeneratedDescription;
    }

    public static Product of(String id, String productCode, String name, String material, String features, Long price,
                             List<String> sizes, List<String> colors, String thumbnailUrl,
                             String brandOfficialDescription, String aiGeneratedDescription) {
        return new Product(id, productCode, name, material, features, price, sizes, colors, thumbnailUrl,
                brandOfficialDescription, aiGeneratedDescription);
    }


    // 사진 등록 시 기준 벡터를 갱신(최초 등록/재등록 둘 다 이걸로 처리) — 더 이상 안 씀
    public void updateEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }

    // 인식용 기준 사진 추가 등록 (정면/측면/윗면 등 호출할 때마다 누적됨)
    public void addReferenceImage(String imageBase64) {
        this.referenceImages.add(imageBase64);
    }

    // 기준 사진 전체 초기화 (다시 처음부터 등록하고 싶을 때)
    public void clearReferenceImages() {
        this.referenceImages.clear();
    }

    // AI 요약(스타일/기능/수납/잠금) 생성 결과를 저장
    public void updateAiGeneratedDescription(String aiGeneratedDescription) {
        this.aiGeneratedDescription = aiGeneratedDescription;
    }
}
