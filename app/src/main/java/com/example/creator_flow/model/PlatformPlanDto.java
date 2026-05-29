package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /platforms/{id}/plans 응답 항목 */
public class PlatformPlanDto {
    @SerializedName("id")
    public String id;

    @SerializedName("platform_id")
    public String platformId;

    @SerializedName("plan_name")
    public String planName;

    @SerializedName("fee_rate")
    public Double feeRate;        // 백분율 (예: 5.0 = 5%)

    @SerializedName("description")
    public String description;
}
