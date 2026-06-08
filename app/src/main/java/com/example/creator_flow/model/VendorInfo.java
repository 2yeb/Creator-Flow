package com.example.creator_flow.model;

import java.util.Map;
import java.util.Set;

/**
 * 제작업체 정보 + 세부 유형별 지원 옵션 매핑.
 * capabilities: subType → (optionCategory → 지원하는 값들)
 * 예) 레드프린팅 - "완칼 스티커" - {용지=[유광,무광,펄], 코팅=[라미,없음], 사이즈=[소,중,대]}
 */
public class VendorInfo {
    public final String name;
    public final int brandColor;       // 좌상단 색상 박스 (로고 없을 때 fallback)
    public final int cardBgColor;      // 카드 자체 배경 틴트
    public int basePrice;              // 기본 가격 (바텀시트에서 옵션 변경 시 재계산하므로 mutable)
    public final int shippingFee;      // 배송비
    public final boolean freeShipping; // 무료 배송 여부
    /** subType → optionCategory → 지원 값들 */
    public final Map<String, Map<String, Set<String>>> capabilities;
    /** 백엔드에서 받은 로고 URL (mock은 null) */
    public String logoUrl;
    /** 상품명 (VendorProduct.name) — vendor 이름 밑에 작게 표시 */
    public String productName;
    /**
     * 옵션 가격표: optionCategory → optionValue → extra_price.
     * 바텀시트에서 칩 변경 시 단가 재계산용. detail1에서 ProductOptionDto로 채움.
     * null이면 가격 재계산 안 함 (mock 모드).
     */
    public Map<String, Map<String, Integer>> optionPrices;
    /** 단가 × 수량 계산용 — detail1에서 채움 */
    public int quantity = 1;

    public VendorInfo(String name, int brandColor, int cardBgColor,
                      int basePrice, int shippingFee, boolean freeShipping,
                      Map<String, Map<String, Set<String>>> capabilities) {
        this.name = name;
        this.brandColor = brandColor;
        this.cardBgColor = cardBgColor;
        this.basePrice = basePrice;
        this.shippingFee = shippingFee;
        this.freeShipping = freeShipping;
        this.capabilities = capabilities;
    }

    /** 해당 세부 유형을 제작할 수 있는 업체인지 */
    public boolean supports(String subType) {
        return capabilities.containsKey(subType);
    }

    /** 세부 유형 + 옵션 카테고리에서 특정 값을 지원하는지 */
    public boolean supportsOption(String subType, String category, String value) {
        Map<String, Set<String>> opts = capabilities.get(subType);
        if (opts == null) return false;
        Set<String> values = opts.get(category);
        return values != null && values.contains(value);
    }
}
