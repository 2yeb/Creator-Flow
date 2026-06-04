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

    /** GET /projects/{id} 응답의 simulations[] 항목 — 차시 1개에 해당 */
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
    }
}
