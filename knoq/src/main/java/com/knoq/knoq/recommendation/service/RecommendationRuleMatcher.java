package com.knoq.knoq.recommendation.service;

import com.knoq.knoq.product.entity.Product;
import com.knoq.knoq.sessions.entity.LifestyleTag;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class RecommendationRuleMatcher {

    private static final Map<LifestyleTag, List<String>> KEYWORDS = createKeywords();
    private static final Map<LifestyleTag, String> TAG_LABELS = Map.of(
            LifestyleTag.MINIMAL, "미니멀",
            LifestyleTag.CLASSIC, "클래식",
            LifestyleTag.CASUAL, "캐주얼",
            LifestyleTag.STREET, "스트릿",
            LifestyleTag.FORMAL, "포멀",
            LifestyleTag.TRENDY, "트렌디"
    );

    public MatchResult match(Product product, List<LifestyleTag> lifestyleTags) {
        String productText = searchableText(product);
        int score = 0;
        Set<LifestyleTag> matchedTags = new LinkedHashSet<>();
        Set<String> matchedKeywords = new LinkedHashSet<>();

        for (LifestyleTag tag : lifestyleTags) {
            for (String keyword : KEYWORDS.getOrDefault(tag, List.of())) {
                if (productText.contains(keyword.toLowerCase(Locale.ROOT))) {
                    score++;
                    matchedTags.add(tag);
                    matchedKeywords.add(keyword);
                }
            }
        }

        return new MatchResult(score, createReason(matchedTags, matchedKeywords));
    }

    public String createSummary(List<LifestyleTag> lifestyleTags, int productCount) {
        String tags = lifestyleTags.stream()
                .map(TAG_LABELS::get)
                .reduce((left, right) -> left + ", " + right)
                .orElse("선택한");
        return tags + " 라이프스타일에 어울리는 제품 " + productCount + "개를 추천했어요.";
    }

    private String createReason(Set<LifestyleTag> matchedTags, Set<String> matchedKeywords) {
        if (matchedTags.isEmpty()) {
            return "선택한 라이프스타일을 바탕으로 함께 살펴볼 만한 제품이에요.";
        }

        String tags = matchedTags.stream()
                .map(TAG_LABELS::get)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        String keyword = matchedKeywords.iterator().next();
        return tags + " 스타일과 잘 어울리는 " + keyword + " 특징의 제품이에요.";
    }

    private String searchableText(Product product) {
        List<String> attributes = new ArrayList<>();
        attributes.add(product.getName());
        attributes.add(product.getCategory());
        attributes.add(product.getMaterial());
        attributes.add(product.getFeatures());
        attributes.add(product.getBrandOfficialDescription());
        attributes.add(product.getAiGeneratedDescription());
        attributes.addAll(product.getColors());

        return attributes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }

    private static Map<LifestyleTag, List<String>> createKeywords() {
        Map<LifestyleTag, List<String>> keywords = new EnumMap<>(LifestyleTag.class);
        keywords.put(LifestyleTag.MINIMAL,
                List.of("미니멀", "심플", "베이직", "무지", "블랙", "화이트", "그레이"));
        keywords.put(LifestyleTag.CLASSIC,
                List.of("클래식", "가죽", "레더", "모노그램", "브라운", "블랙"));
        keywords.put(LifestyleTag.CASUAL,
                List.of("캐주얼", "데일리", "편안", "캔버스", "나일론", "데님"));
        keywords.put(LifestyleTag.STREET,
                List.of("스트릿", "오버사이즈", "그래픽", "로고", "체인", "블랙"));
        keywords.put(LifestyleTag.FORMAL,
                List.of("포멀", "정장", "비즈니스", "가죽", "블랙", "네이비"));
        keywords.put(LifestyleTag.TRENDY,
                List.of("트렌디", "시즌", "신상", "볼드", "메탈", "컬러"));
        return Map.copyOf(keywords);
    }

    public record MatchResult(int score, String reason) {
    }
}
