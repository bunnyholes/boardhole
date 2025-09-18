# HTML/CSS 극한 최적화 전략 메모리

## 발견된 최적화 패턴

### Bootstrap 잔재 클래스 제거 목록
- 모달 관련: data-bs-toggle, data-bs-target, modal-dialog, modal-content, modal-header, modal-footer, modal-body, btn-close
- 레이아웃: d-flex, justify-content-between, align-items-center, text-center, row, col-*
- 유틸리티: mb-, mt-, p-, bg-, text-muted, small, h3, h4, h5

### SVG → 이모지 대체 매핑
- 사용자 아이콘 → 👤 (이미 적용됨)
- 시계/시간 → ⏰ 📅 
- 조회수 → 👁 (이미 적용됨)
- 편집 → ✏️ (이미 적용됨)
- 뒤로가기 화살표 → ←
- 드롭다운 화살표 → ▼
- 닫기 버튼 → ❌
- 문서 → 📄
- 검색 → 🔍
- 설정 → ⚙️

### CSS 최적화 전략 (141줄 → 25-30줄)
1. 모달 시스템을 HTML `<dialog>` 요소로 대체 (-45줄)
2. 히어로 섹션 단순화 (-30줄) 
3. 최신 CSS 선택자 활용: :is(), :has(), :where()
4. Container Queries 도입
5. CSS Nesting 활용

### 최종 목표 CSS 구조
```css
:root { /* CSS 변수들 */ }
:is(main,header,footer,nav).container { /* 컨테이너 */ }
.hero { /* 히어로 섹션 */ }
dialog { /* 네이티브 모달 */ }
.flex-between { /* 유틸리티 */ }
.avatar { /* 아바타 */ }
@media (prefers-reduced-motion: reduce) { /* 접근성 */ }
```

### 예상 성과
- CSS 80% 감소
- HTML Bootstrap 클래스 156개 제거
- SVG 30-40% 이모지 대체
- DOM 복잡도 크게 감소