package com.example.creator_flow.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.List;

/**
 * 시뮬레이션 흐름 중 사용자가 선택/입력한 값을 누적 보관.
 * 각 fragment가 자기가 받은 값을 여기에 set하고, 마지막 요약 화면(Business)이 read.
 *
 * 향후 백엔드 API로 대체될 때:
 * - set()들은 그대로 두되, 요약 화면에서 API 응답을 받으면 이 클래스를 덮어쓰거나 무시
 * - reset()으로 새 시뮬레이션 시작 시 초기화
 */
public class SimulationData {

    /**
     * 기존 프로젝트에 차시(simulation)를 추가하는 모드일 때 그 프로젝트의 ID.
     * - null = 새 프로젝트 만드는 모드 (기본 흐름)
     * - non-null = 이 ID의 프로젝트에 차시 attach (ProjectPageFragment의 + 버튼에서 진입)
     * 시뮬레이션 완료 시 POST /simulations 호출에 project_id로 같이 보냄.
     */
    public static String targetProjectId;

    // ===== 모델 =====
    public static String modelType;        // "Business" | "Fanart"

    // ===== Detail1 =====
    public static String goodsType;        // 굿즈 유형 (스티커 등)
    public static String subType;          // 세부 유형 (완칼 스티커 등)
    public static String optionSummary;    // 옵션 요약 ("유광 · 라미 · 칼선")
    public static Integer quantity;        // 수량
    public static String vendorName;       // 제작 업체 (표시용)
    /** POST /simulations 에 사용할 vendor_product의 실제 UUID. mock 모드에선 null */
    public static String vendorProductId;
    /** POST /simulations 에 사용할 옵션들의 실제 UUID 리스트. mock 모드에선 빈 리스트 */
    public static List<String> selectedOptionIds;

    // ===== Platform =====
    public static String platformName;     // 판매 업체 (윗치폼/텀블벅 등) — 표시용
    public static Integer platformFee;     // 수수료(%) — Business일 때만 의미 있음
    /** POST /simulations 에 사용할 platform_plan의 실제 UUID. mock 모드에선 null */
    public static String platformPlanId;

    // ===== Delivery =====
    public static String deliveryMethod;   // 택배 옵션 (편의점 택배 등)
    public static String shippingFeeType;  // 배송비 (구매자 부담 / 판매자 택배)
    public static String packagingType;    // 포장재
    /** 구매자 부담 선택 시 사용자가 입력한 배송비 금액 (원). 판매자 택배면 null */
    public static Integer shippingFeeBuyer;

    // ===== Detail2 (입력값) =====
    public static Integer profit;          // 희망 순이익 (Business 전용)
    public static Integer marginRate;      // 희망 마진율 (Business 전용)
    public static Integer targetQuantity;  // 목표 판매량
    public static Integer minStock;        // 최소 재고량

    // ===== 계산값 (백엔드 또는 클라이언트가 산출) =====
    public static Integer estimatedCost;   // 예상 원가

    /**
     * 단가 (개당 원). 시뮬레이터 detail1에서 vendor 확정 시 클라이언트가 계산한 값 보관.
     * LOCAL_PROJECT_MODE에서 백엔드 unit_cost가 없을 때 ProjectPage 첫 차시에 채울 용도.
     */
    public static Integer unitCost;

    /**
     * 시연용 로컬 모드: POST /simulations 응답을 통째로 보관.
     * ProjectPageFragment 진입 시 첫 차시에 자동 채울 때 사용.
     * 일반 reset()에서는 비우지 않음 — ProjectPageFragment가 한 번 읽고 직접 null로 비움.
     */
    public static com.example.creator_flow.model.SimulationResultDto lastResult;

    public static void reset() {
        targetProjectId = null;
        modelType = null;
        goodsType = null;
        subType = null;
        optionSummary = null;
        quantity = null;
        vendorName = null;
        vendorProductId = null;
        selectedOptionIds = null;
        platformName = null;
        platformFee = null;
        platformPlanId = null;
        deliveryMethod = null;
        shippingFeeType = null;
        packagingType = null;
        shippingFeeBuyer = null;
        profit = null;
        marginRate = null;
        targetQuantity = null;
        minStock = null;
        estimatedCost = null;
        // unitCost는 ProjectPage가 읽고 직접 비우므로 reset()에선 유지 (lastResult와 동일 패턴)
    }

    private SimulationData() {}
}
