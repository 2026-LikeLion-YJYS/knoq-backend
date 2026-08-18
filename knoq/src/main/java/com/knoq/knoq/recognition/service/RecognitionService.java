package com.knoq.knoq.recognition.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.global.exception.ErrorCode;
import com.knoq.knoq.global.util.IdGenerator;
import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.product.repository.ProductRepository;
import com.knoq.knoq.recognition.client.OpenAiVisionClient;
import com.knoq.knoq.recognition.dto.ConfirmRecognitionRequest;
import com.knoq.knoq.recognition.dto.ConfirmRecognitionResponse;
import com.knoq.knoq.recognition.dto.ProductLookupRequest;
import com.knoq.knoq.recognition.dto.ProductLookupResponse;
import com.knoq.knoq.recognition.dto.RecognitionResponse;
import com.knoq.knoq.recognition.entity.MatchType;
import com.knoq.knoq.recognition.entity.Recognition;
import com.knoq.knoq.recognition.entity.RecognitionCandidate;
import com.knoq.knoq.recognition.entity.RecognitionStatus;
import com.knoq.knoq.recognition.repository.RecognitionRepository;
import com.knoq.knoq.saved.entity.SavedProduct;
import com.knoq.knoq.saved.service.SavedProductService;
import com.knoq.knoq.sessions.service.SessionExpirationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecognitionService {

    private static final Logger log = LoggerFactory.getLogger(RecognitionService.class);

    // 80% 이상이면 단일 확정, 미만이면 후보 목록 (명세서 2.1 기준)
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    private static final SecureRandom RANDOM = new SecureRandom();

    // 8/13 폴백 정책: false로 내리면 확신도와 무관하게 항상 CANDIDATES
    @Value("${knoq.recognition.threshold-enabled:true}")
    private boolean thresholdEnabled;

    private final RecognitionRepository recognitionRepository;
    private final ProductRepository productRepository;
    private final SessionExpirationService sessionExpirationService;
    private final OpenAiVisionClient openAiVisionClient;
    private final SavedProductService savedProductService;

    @Transactional
    public RecognitionResponse recognize(String sessionId, MultipartFile image) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);
        if (image == null || image.isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR);
        }

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            throw new ApiException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        MatchResult matchResult = tryVisionMatch(products, image);
        if (matchResult == null) {
            // GPT 비전 호출 실패 또는 기준 사진이 등록된 제품이 없음 → mock 랜덤 매칭으로 폴백
            matchResult = mockMatch(products);
        }

        Recognition recognition = Recognition.of(
                IdGenerator.generate("rec"), sessionId, matchResult.matchType(), matchResult.candidates());
        recognitionRepository.save(recognition);

        List<RecognitionResponse.CandidateResponse> candidateResponses = new ArrayList<>();
        for (int i = 0; i < matchResult.products().size(); i++) {
            Product product = matchResult.products().get(i);
            candidateResponses.add(new RecognitionResponse.CandidateResponse(
                    product.getId(), product.getName(), product.getThumbnailUrl(),
                    matchResult.candidates().get(i).getConfidence()
            ));
        }

        return new RecognitionResponse(recognition.getId(), matchResult.matchType(), candidateResponses);
    }

    // GPT 비전으로 후보 결정. 실패하거나 기준 사진이 등록된 제품이 하나도 없으면 null 반환 → 호출부에서 mock 폴백
    private MatchResult tryVisionMatch(List<Product> products, MultipartFile image) {
        List<Product> withReferenceImage = products.stream()
                .filter(p -> p.getReferenceImages() != null && !p.getReferenceImages().isEmpty())
                .toList();
        if (withReferenceImage.isEmpty()) {
            return null;
        }

        String capturedBase64;
        try {
            capturedBase64 = Base64.getEncoder().encodeToString(image.getBytes());
        } catch (IOException e) {
            return null;
        }

        List<OpenAiVisionClient.VisionMatch> matches;
        try {
            matches = openAiVisionClient.recognize(capturedBase64, withReferenceImage);
        } catch (ApiException e) {
            log.warn("GPT 비전 호출 자체가 실패해서 mock으로 폴백함");
            return null;
        }
        if (matches == null || matches.isEmpty()) {
            log.warn("GPT가 매치를 하나도 안 돌려줘서 mock으로 폴백함");
            return null;
        }
        log.info("GPT가 준 원본 매치 목록: {} (참고 사진 등록된 제품 ID: {})", matches,
                withReferenceImage.stream().map(Product::getId).toList());

        Map<String, Product> productById = withReferenceImage.stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, p -> p));

        // GPT가 모르는 productId를 만들어 반환할 수도 있으니 실제로 존재하는 것만 사용
        List<OpenAiVisionClient.VisionMatch> validMatches = matches.stream()
                .filter(m -> productById.containsKey(m.productId()))
                .sorted(Comparator.comparingDouble(OpenAiVisionClient.VisionMatch::confidence).reversed())
                .toList();
        if (validMatches.isEmpty()) {
            log.warn("GPT가 준 매치가 전부 등록 안 된(할루시네이션) productId라서 mock으로 폴백함: {}", matches);
            return null;
        }

        double topConfidence = round(validMatches.get(0).confidence());
        boolean isSingleMatch = thresholdEnabled && topConfidence >= CONFIDENCE_THRESHOLD;

        List<OpenAiVisionClient.VisionMatch> chosenMatches = isSingleMatch
                ? validMatches.subList(0, 1)
                : validMatches.subList(0, Math.min(3, validMatches.size()));

        List<Product> chosen = new ArrayList<>();
        List<RecognitionCandidate> candidateEntities = new ArrayList<>();
        for (OpenAiVisionClient.VisionMatch match : chosenMatches) {
            chosen.add(productById.get(match.productId()));
            candidateEntities.add(RecognitionCandidate.of(match.productId(), round(match.confidence())));
        }

        MatchType matchType = isSingleMatch ? MatchType.SINGLE : MatchType.CANDIDATES;
        return new MatchResult(matchType, chosen, candidateEntities);
    }

    // GPT 호출을 못 쓰거나 기준 사진이 아직 없을 때 쓰는 목업 랜덤 매칭
    private MatchResult mockMatch(List<Product> products) {
        List<Product> shuffled = new ArrayList<>(products);
        Collections.shuffle(shuffled);

        double primaryConfidence = round(0.5 + RANDOM.nextDouble() * 0.5);
        boolean isSingleMatch = thresholdEnabled && primaryConfidence >= CONFIDENCE_THRESHOLD;

        List<Product> chosen;
        List<RecognitionCandidate> candidateEntities = new ArrayList<>();

        if (isSingleMatch) {
            chosen = List.of(shuffled.get(0));
            candidateEntities.add(RecognitionCandidate.of(shuffled.get(0).getId(), primaryConfidence));
        } else {
            int count = Math.min(3, shuffled.size());
            chosen = shuffled.subList(0, count);
            double confidence = thresholdEnabled ? Math.min(primaryConfidence, 0.79) : round(0.5 + RANDOM.nextDouble() * 0.3);
            for (Product product : chosen) {
                candidateEntities.add(RecognitionCandidate.of(product.getId(), confidence));
                confidence = round(Math.max(0.2, confidence - (0.1 + RANDOM.nextDouble() * 0.1)));
            }
        }

        MatchType matchType = isSingleMatch ? MatchType.SINGLE : MatchType.CANDIDATES;
        return new MatchResult(matchType, chosen, candidateEntities);
    }

    @Transactional
    public ConfirmRecognitionResponse confirm(String sessionId, String recognitionId, ConfirmRecognitionRequest request) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);
        Recognition recognition = findValidRecognition(sessionId, recognitionId);

        if (!request.confirmed()) {
            recognition.discard();
            return null; // 컨트롤러에서 204로 처리
        }

        if (!recognition.hasCandidate(request.productId())) {
            throw new ApiException(ErrorCode.INVALID_RECOGNITION_CANDIDATE);
        }

        recognition.confirm();

        // confirm과 저장을 같은 트랜잭션 안에서 처리 — 저장이 실패하면 confirm도 같이 롤백됨
        SavedProduct savedProduct = savedProductService.saveFromCamera(sessionId, request.productId());
        // savedProduct.getId()가 Long이라 String으로 변환 (다른 ID들처럼 응답은 문자열로 통일)
        return new ConfirmRecognitionResponse(request.productId(), true, String.valueOf(savedProduct.getId()));
    }

    @Transactional
    public ProductLookupResponse lookupProduct(String sessionId, ProductLookupRequest request) {
        sessionExpirationService.getValidSessionAndRefresh(sessionId);

        Product product = productRepository.findByProductCode(request.productCode())
                .orElseThrow(() -> new ApiException(ErrorCode.PRODUCT_NOT_FOUND));

        return new ProductLookupResponse(product.getId(), product.getName());
    }

    private Recognition findValidRecognition(String sessionId, String recognitionId) {
        Recognition recognition = recognitionRepository.findById(recognitionId)
                .orElseThrow(() -> new ApiException(ErrorCode.RECOGNITION_NOT_FOUND));
        if (!recognition.getSessionId().equals(sessionId)) {
            throw new ApiException(ErrorCode.RECOGNITION_NOT_FOUND);
        }
        if (recognition.getStatus() != RecognitionStatus.PENDING) {
            throw new ApiException(ErrorCode.RECOGNITION_ALREADY_PROCESSED);
        }
        return recognition;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record MatchResult(MatchType matchType, List<Product> products, List<RecognitionCandidate> candidates) {}
}
