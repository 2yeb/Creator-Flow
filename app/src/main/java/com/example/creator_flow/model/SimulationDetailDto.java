package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/**
 * GET /simulations/{simulation_id} 응답.
 * 한 차시(시뮬레이션 row) 상세 데이터 — vendor_name, plan_name 등 조인 결과 포함.
 */
public class SimulationDetailDto {

    @SerializedName("simulation_id")
    public String simulationId;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("vendor_name")
    public String vendorName;

    @SerializedName("platform_plan")
    public String platformPlan;

    @SerializedName("fee_rate")
    public Double feeRate;

    @SerializedName("model_type")
    public String modelType;

    @SerializedName("quantity")
    public Integer quantity;

    @SerializedName("selling_price")
    public Integer sellingPrice;

    @SerializedName("shipping_type")
    public String shippingType;

    @SerializedName("target_quantity")
    public Integer targetQuantity;

    @SerializedName("actual_quantity")
    public Integer actualQuantity;

    @SerializedName("unit_cost")
    public Integer unitCost;

    @SerializedName("total_cost")
    public Integer totalCost;

    @SerializedName("expected_revenue")
    public Integer expectedRevenue;

    @SerializedName("total_fee")
    public Integer totalFee;

    @SerializedName("net_profit")
    public Integer netProfit;

    @SerializedName("revenue_rate")
    public Double revenueRate;

    @SerializedName("break_even_quantity")
    public Integer breakEvenQuantity;
}
