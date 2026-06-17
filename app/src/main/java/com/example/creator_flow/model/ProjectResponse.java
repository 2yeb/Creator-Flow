package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** GET/PUT /projects/{id} 응답 모델 */
public class ProjectResponse {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("status")
    public String status;

    @SerializedName("created_at")
    public String createdAt;

    /** 이 프로젝트의 차시 리스트 (= 백엔드 simulation row 들의 요약) */
    @SerializedName("simulations")
    public List<SimulationSummary> simulations;

    /**
     * 첨부 이미지 — 백엔드 GET /projects/{id} 응답에는 안 들어있지만,
     * 이전 코드 호환을 위해 필드는 남겨둠 (null이 정상).
     * 이미지는 POST/DELETE /projects/{id}/images 로 별도 관리.
     */
    @SerializedName("images")
    public List<ProjectImage> images;

    public static class ProjectImage {
        @SerializedName("image_id")
        public String imageId;

        @SerializedName("image_url")
        public String imageUrl;
    }

    /**
     * GET /projects/{id} 응답의 simulations[] 항목 — 차시 1개에 해당.
     * 2026-06 백엔드 업데이트로 vendor_name, platform_plan, unit_cost 등이 추가되어
     * 차시당 별도 GET /simulations 호출 없이 차시 카드를 채울 수 있음.
     */
    public static class SimulationSummary {
        @SerializedName("id")
        public String id;

        @SerializedName("quantity")
        public Integer quantity;

        @SerializedName("selling_price")
        public Integer sellingPrice;

        @SerializedName("model_type")
        public String modelType;

        @SerializedName("shipping_type")
        public String shippingType;

        @SerializedName("target_quantity")
        public Integer targetQuantity;

        @SerializedName("actual_quantity")
        public Integer actualQuantity;

        @SerializedName("created_at")
        public String createdAt;

        // ===== 2026-06 백엔드 업데이트로 추가된 필드들 =====

        @SerializedName("vendor_name")
        public String vendorName;

        @SerializedName("platform_plan")
        public String platformPlan;

        @SerializedName("fee_rate")
        public Double feeRate;

        @SerializedName("unit_cost")
        public Integer unitCost;

        @SerializedName("total_cost")
        public Integer totalCost;

        @SerializedName("net_profit")
        public Integer netProfit;
    }
}
