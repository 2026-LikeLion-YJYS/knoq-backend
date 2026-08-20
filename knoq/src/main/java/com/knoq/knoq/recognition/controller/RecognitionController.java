package com.knoq.knoq.recognition.controller;

import com.knoq.knoq.recognition.dto.ConfirmRecognitionRequest;
import com.knoq.knoq.recognition.dto.ConfirmRecognitionResponse;
import com.knoq.knoq.recognition.dto.ProductLookupRequest;
import com.knoq.knoq.recognition.dto.ProductLookupResponse;
import com.knoq.knoq.recognition.dto.RecognitionResponse;
import com.knoq.knoq.recognition.service.RecognitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sessions/{sessionId}")
@RequiredArgsConstructor
public class RecognitionController {

    private final RecognitionService recognitionService;

    // 2.1 카메라 인식 요청 (FR-200)
    // consumes를 명시 안 하면 springdoc이 Swagger에 application/json + binary로 잘못 표시하는 경우가 있어서
    // multipart/form-data라는 걸 명확히 해줌 (실제 바인딩 동작 자체는 원래도 multipart였음)
    @PostMapping(value = "/recognitions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecognitionResponse> recognize(@PathVariable String sessionId,
                                                         @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(recognitionService.recognize(sessionId, image));
    }

    // 2.2 인식 결과 확인 (FR-200). confirmed=false면 폐기 처리만 하고 204
    @PostMapping("/recognitions/{recognitionId}/confirm")
    public ResponseEntity<ConfirmRecognitionResponse> confirm(@PathVariable String sessionId,
                                                              @PathVariable String recognitionId,
                                                              @Valid @RequestBody ConfirmRecognitionRequest request) {
        ConfirmRecognitionResponse response = recognitionService.confirm(sessionId, recognitionId, request);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    // 2.3 카메라 권한 거부 시 수동 조회 (FR-200 예외)
    @PostMapping("/products/lookup")
    public ResponseEntity<ProductLookupResponse> lookupProduct(@PathVariable String sessionId,
                                                               @Valid @RequestBody ProductLookupRequest request) {
        return ResponseEntity.ok(recognitionService.lookupProduct(sessionId, request));
    }
}