package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/**
 * POST /simulations 응답.
 * 백엔드가 계산한 단가/총원가/추천가/예상매출/마진율/손익분기 수량 반환.
 */
public class SimulationResultDto {
    @SerializedName("simulation_id")
    public String simulationId;

    @SerializedName("unit_cost")
    public Integer unitCost;

    @SerializedName("total_cost")
    public Integer totalCost;

    @SerializedName("recommended_price")
    public Integer recommendedPrice;

    @SerializedName("expected_revenue")
    public Integer expectedRevenue;

    @SerializedName("revenue_rate")
    public Double revenueRate;

    @SerializedName("break_even_quantity")
    public Integer breakEvenQuantity;
}
