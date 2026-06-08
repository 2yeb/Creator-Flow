package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /vendors/{vendor_id}/products 응답 항목 */
public class VendorProductDto {
    @SerializedName("id")
    public String id;

    @SerializedName("vendor_id")
    public String vendorId;

    /** 상품명 (2026-06-06 백엔드 추가 컬럼) — 예: "완칼 스티커 100매 라미" */
    @SerializedName("name")
    public String name;

    @SerializedName("goods_type_id")
    public String goodsTypeId;

    @SerializedName("goods_detail_type_id")
    public String goodsDetailTypeId;

    @SerializedName("min_quantity")
    public Integer minQuantity;

    @SerializedName("shipping_fee")
    public Integer shippingFee;

    @SerializedName("free_shipping_min")
    public Integer freeShippingMin;
}
