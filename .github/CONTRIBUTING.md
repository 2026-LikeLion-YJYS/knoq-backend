# Contributing Guide

프로젝트에 기여하기 전 아래 규칙을 확인해주세요.

---

# 1. Commit Convention

- 기본적으로 다음 커밋 메시지 규칙을 따른다.

| **태그이름** | **내용** |
| --- | --- |
| `feat` | 새로운 기능 (파일 추가도 포함)을 추가할 경우 |
| `refactor` | 코드 수정, 프로덕션 코드 리팩토링 |
| `fix` | 버그를 고친 경우 |
| `!HOTFIX` | 급하게 치명적인 버그를 고쳐야하는 경우 |
| `style` | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우 |
| `comment` | 필요한 주석 추가 및 변경 |
| `docs` | 문서를 수정한 경우 |
| `test` | 테스트 추가, 테스트 리팩토링(프로덕션 코드 변경 X) |
| `chore` | 빌드 테스트 업데이트, 패키지 매니저를 설정하는 경우(프로덕션 코드 변경 X) |
| `rename` | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 |
| `remove` | 파일을 삭제하는 작업만 수행한 경우 |

- **커밋 메시지 형식**

```text
[커밋메시지-소문자로]: 구현 기능설명
ex. feat: 회원 가입 기능 구현 #(이슈번호)
```

---

# 2. Git Branch Convention

- 기본적으로 `git flow` 전략을 따른다.

### 세부 브랜치 전략

#### 브랜치 종류

- `main` : 우리가 최종 개발 시 Merge 하는 곳
- `develop` : 개발 중 merge하는 최상위 브랜치
- `태그/#이슈번호-기능명` : 기능을 개발하면서 각자가 사용할 브랜치

예시

```text
feat/#1-kakao-oauth
```

- `hotfix` : 급한 수정사항 및 QA를 반영할 때 사용할 브랜치

#### 브랜치 전략

다음 분기 규칙을 따른다.

```text
main
 └── develop
      └── 태그/#이슈번호-기능명
```

예시

```text
main
 └── develop
      ├── feat/#1-kakao-oauth
      └── refactor/#2-login
```

---

# 3. Issue & PR

## Issue

- **Issue Template**
    - Issue 제목: **`[도메인명(camelCase)] 구현할 기능`**
        - ex. `[login] 카카오 로그인 구현`

```md
## 🌈 어떤 기능인가요?

## ✅ To Dos

- [ ]
- [ ]
- [ ]
```

- Issue 먼저 생성 후 작업
- Assignees에 본인 추가, Labels에 작업 관련 라벨을 추가한다.

---

## PR

- **PR Template**
    - PR 제목: **`[도메인명(camelCase)] 구현한 기능`**
        - ex. `[login] 로그인 기능 구현`

```md
## 🔥 Related Issues
- close #IssueNumber

## 💻 작업 내용
- [x] ~ 기능 구현
- [x] ~ 페이지 구조화 및 스타일링

## ✅ PR Point
- 소스코드 설명

## 😡 Trouble Shooting
- 어떤 어려움이 있었고 어떻게 해결했는지

## ☀️ 스크린샷 / GIF / 화면 녹화

## 📚 Reference
- 구현에 참고한 링크 (필요한 경우만 작성하고 없으면 지우기)
```

- 트러블 슈팅 및 고민사항을 공유한다.
- Assignees에 본인 추가, Labels에 작업 관련 라벨을 추가한다.
- Reviewers에 본인을 제외한 팀원을 추가
    - 다른 팀원이 review & approve → merge하는 방향으로
    - 급한 버그 수정시 본인이 바로 merge 후 다른 팀원에게 공유