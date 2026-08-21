<a href="https://knoq-frontend.vercel.app/?storeCode=TEST-001" target="_blank">
<img width="1920" height="1080" alt="githubreadme" src="https://github.com/user-attachments/assets/0fd022ae-79bd-425e-9335-1da395418426" width="100%"/>
</a>
<p align="center">
  <a href="https://knoq-frontend.vercel.app/?storeCode=TEST-001">
    <b>👜 KNOQ 서비스 보기</b>
  </a>
</p>

# KNOQ

## Background
럭셔리 패션 쇼핑 환경에서 고객은 매장 방문 후에도 본인 취향이 일관되게 반영된 쇼핑 경험을 얻기 어려웠고,  
탐색 기록 관리·추천 근거 해석·빠른 상품 재발견이 단절되어 있었습니다.

KNOQ는 **고객별 탐색 컨텍스트(세션/저장/선호/인식 결과)** 를 한 번에 묶어,  
카메라 기반 쇼핑 여정을 빠르게 이어가고 AI로 실전성 있는 니즈 분석까지 제공하는 서비스를 목표로 합니다.

## Solution
KNOQ는 고객의 방문 세션을 중심으로  
- 매장 입장부터 온보딩(닉네임/라이프스타일)  
- 카메라 기반 제품 인식(비전 매칭)  
- 저장/재방문 아카이브  
- 상담/니즈 분석 흐름  

을 통합해 하나의 탐색 파이프라인으로 구성했습니다.  
또한 카카오 계정 기반 인증 흐름(ACCOUNT/PENDING/KAKAO)과 PRIVATE 흐름을 분리해,  
신규 고객/재방문 고객의 UX를 각각 안정적으로 처리합니다.

## Key Features
- **모바일 전용 탐색 흐름**
  - 매장 QR 기반 세션 생성 → 온보딩 → 스캔/추천/저장까지 연속 동선 설계  
- **카메라 기반 제품 인식**
  - 촬영 이미지 기반 GPT-4o-mini Vision 매칭으로 후보 제품 도출  
  - 인식 실패 시 재시도/후보 선택 UX 지원  
- **탐색 아카이브**
  - 카카오 계정당 일일 아카이브 정책으로 재방문 시 저장 이력 조회 최적화  
- **AI 니즈 분석**
  - 저장된 상품 속성(소재/색상/사이즈/카테고리 등) 기반 핵심 구매 니즈 문장 생성  
- **직원 연동/상담 흐름**
  - 직원 페이지를 통한 요청 접수 및 매장/상담 응답 연동

## 스크린샷 / 시연

<div align="center" style="display: flex; flex-wrap: wrap; justify-content: center;">
<img width="1200" height="800" alt="1" src="https://github.com/user-attachments/assets/eb1f6816-1e23-45fa-9104-5ef121856b26" />
<img width="1200" height="800" alt="2" src="https://github.com/user-attachments/assets/c7691406-ecf3-45e8-85ea-404ff5e43b78" />
<img width="1200" height="800" alt="3" src="https://github.com/user-attachments/assets/6ec10246-9690-43cd-9448-d846339029d8" />
<img width="1200" height="800" alt="4" src="https://github.com/user-attachments/assets/ddd3eceb-8cc3-4b1c-b409-196aec0b751e" />
<img width="1200" height="800" alt="5" src="https://github.com/user-attachments/assets/6e5e3b90-80d2-47b7-b69c-86dccec5e0a7" />
</div>

## 버전 정보

[![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)]()
[![Spring_Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)]()
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=for-the-badge&logo=gradle&logoColor=white)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)]()
[![React](https://img.shields.io/badge/React-19.x-61DAFB?style=for-the-badge&logo=react&logoColor=white)]()
[![Vite](https://img.shields.io/badge/Vite-6.x-646CFF?style=for-the-badge&logo=vite&logoColor=white)]()

## Skills & Tech Stack

### Backend
[![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)]()
[![Spring_Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)]()
[![Spring_Data_JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)]()
[![Spring_Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)]()
[![OAuth2](https://img.shields.io/badge/OAuth2_Client-000000?style=for-the-badge)]()
[![JPA](https://img.shields.io/badge/JPA-59666A?style=for-the-badge)]()
[![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)]()
[![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?style=for-the-badge&logo=openapiinitiative&logoColor=white)]()
[![JJWT](https://img.shields.io/badge/JJWT-0.12.6-000000?style=for-the-badge)]()
[![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=java&logoColor=white)]()
[![Mockito](https://img.shields.io/badge/Mockito-7F52FF?style=for-the-badge)]()

### Frontend
[![JavaScript](https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)]()
[![React](https://img.shields.io/badge/React-19.x-61DAFB?style=for-the-badge&logo=react&logoColor=white)]()
[![Vite](https://img.shields.io/badge/Vite-6.x-646CFF?style=for-the-badge&logo=vite&logoColor=white)]()
[![CSS3](https://img.shields.io/badge/CSS-1572B6?style=for-the-badge&logo=css3&logoColor=white)]()

### Infra / Collaboration
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)]()
[![GitHub](https://img.shields.io/badge/GitHub-A6A9AA?style=for-the-badge&logo=github&logoColor=white)]()
[![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)]()
[![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)]()

---

## Backend (KNOQ Backend)

### 개발 환경

| 구분 | 내용 |
| --- | --- |
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.0 |
| ORM | Spring Data JPA |
| DB | MySQL (Docker) |
| 빌드 도구 | Gradle |
| 인증/보안 | Spring Security, Spring Security OAuth2 Client |
| 문서화 | springdoc-openapi (Swagger UI) 2.8.9 |
| 코드 생산성 | Lombok |
| 토큰 | JJWT 0.12.6 (직원 인증용 JWT) |
| 테스트 | JUnit5, AssertJ, Mockito (spring-boot-starter-test) |
| IDE | IntelliJ IDEA |

### 파트별 아키텍처

- **인증/세션**
  - 고객용 세션(`sessionId`, `expiresAt`) 기반 운영
  - 직원용 JWT 인증 분리 설계

- **제품 인식 (AI/Tech)**
  - 카메라 이미지 업로드 → GPT-4o-mini Vision으로 후보 매칭
  - 제품 후보군 제시 및 실패 처리 전략 포함

- **저장/니즈 분석**
  - 세션 기반 `SavedProduct` 관리
  - UNIQUE 제약(`session_id + product_id`)으로 중복 저장 방지
  - 저장 데이터 집계 기반 니즈 분석 재생성(Upsert)

- **공통 인프라**
  - GlobalExceptionHandler + ErrorCode enum 기반 공통 에러 응답
  - Swagger 문서 자동화 (`@Tag`, `@Operation`)

---

## Frontend (KNOQ Frontend)

### 개발 환경

| 구분 | 내용 |
| --- | --- |
| 언어 | JavaScript |
| 라이브러리 | React |
| 빌드 도구 | Vite |
| 스타일링 | CSS |
| IDE | Visual Studio Code |
| 버전 관리 | Git · GitHub |
| 디자인 | Figma |

### 파트별 구현 포인트

- **UI/UX 구성**
  - React 컴포넌트 구조 기반의 모바일 중심 화면 구성
  - 공통 컴포넌트 분리(`MainHeader`, `BottomNav` 등)

- **상태/인터랙션**
  - useState 기반 화면 상태 관리
  - 로그인/온보딩/탐색/저장 흐름 분기 제어

- **API 연동**
  - 제품 조회, 저장, 니즈 분석, 상담 요청 API 연동
  - 오류 응답 처리 및 화면 복구 UX 반영

---

## API 문서
- Swagger UI: `/swagger-ui.html`
- OpenAPI: `/v3/api-docs`

## 기획/디자인 자료
- Notion (기획/요구사항 정리)
- Figma (화면/컴포넌트/플로우)
- FigJam (브레인스토밍/협업 노트)

---

## 팀 / Contributors

<div align="center">
<table>
  <tr>
    <td align="center" style="padding: 10px 30px;">
      <a href="https://github.com/hiseyeon">
        <img alt="Frame 1707482222" src="https://github.com/user-attachments/assets/4ad4ac0d-ba2c-4daa-9c39-ebf12e7a3576" width="200" style="border-radius: 12px;"/><br/>
        <b>Leader·Backend 황세연</b>
      </a><br/>
    </td>
    <td align="center" style="padding: 10px 30px;">
      <a href="https://github.com/dahyun0431">
        <img src="https://github.com/user-attachments/assets/099a4351-c50f-4cbd-b060-9faa8a8b00d1" width="200" style="border-radius: 12px;"/><br/>
        <b>PM·Design 김다현</b>
      </a><br/>
    </td>
    <td align="center" style="padding: 10px 30px;">
      <a href="https://github.com/co0000oo000l">
        <img alt="Frame 1707485953" src="https://github.com/user-attachments/assets/d6106a1b-237d-4b62-ba27-57fe9f7f8e0e" width="200" style="border-radius: 12px;"/><br/>
        <b>PM·Design 이시원</b>
      </a><br/>
    </td>
  </tr>
  <tr>
    <td align="center" style="padding: 10px 30px;">
      <a href="https://github.com/xhar1otte">
        <img alt="Frame 1707485956" src="https://github.com/user-attachments/assets/3187ad7a-f75d-4c46-8b7c-c66c4122694f" width="200" style="border-radius: 12px;"/><br/>
        <b>Frontend 박서연</b>
      </a><br/>
    </td>
    <td align="center" style="padding: 10px 30px;">
      <a href="https://github.com/veee">
        <img alt="Frame 1707485955" src="https://github.com/user-attachments/assets/67a2ec45-68b7-4860-b226-d75bab41c1ff" width="200" style="border-radius: 12px;"/><br/>
        <b>Frontend 송윤서</b>
      </a><br/>
    </td>
    <td align="center" style="padding: 10px 30px;">
      <a href="https://github.com/hyunji726">
        <img alt="Frame 1707485954" src="https://github.com/user-attachments/assets/8881ce67-182e-455e-87cd-11f72fb1a6b0" width="200" style="border-radius: 12px;"/><br/>
        <b>Backend 장현지</b>
      </a><br/>
    </td>
  </tr>
</table>
</div>


## 협업 컨벤션
자세한 내용은 [CONTRIBUTING.md](.github/CONTRIBUTING.md)를 참고해주세요.
