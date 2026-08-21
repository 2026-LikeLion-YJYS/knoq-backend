package com.knoq.knoq.global.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(
                securityConfig,
                "allowedOrigins",
                "http://localhost:5173, https://frontend.example.com"
        );
    }

    @Test
    @DisplayName("설정한 프론트엔드 Origin과 필요한 요청 항목을 허용한다")
    void allowsConfiguredFrontendOrigins() {
        CorsConfiguration configuration = getConfiguration("http://localhost:5173");

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://localhost:5173"))
                .isEqualTo("http://localhost:5173");
        assertThat(configuration.checkOrigin("https://frontend.example.com"))
                .isEqualTo("https://frontend.example.com");
        assertThat(configuration.checkHttpMethod(HttpMethod.OPTIONS))
                .contains(HttpMethod.OPTIONS);
        assertThat(configuration.checkHeaders(List.of("Authorization", "Content-Type")))
                .containsExactly("Authorization", "Content-Type");
    }

    @Test
    @DisplayName("설정하지 않은 Origin은 허용하지 않는다")
    void rejectsUnconfiguredOrigin() {
        CorsConfiguration configuration = getConfiguration("https://unknown.example.com");

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("https://unknown.example.com")).isNull();
    }

    private CorsConfiguration getConfiguration(String origin) {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/sessions");
        request.addHeader("Origin", origin);
        return source.getCorsConfiguration(request);
    }
}
