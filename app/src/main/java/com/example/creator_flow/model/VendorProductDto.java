package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /vendors/{vendor_id}/products 응답 항목 */
public class VendorProductDto {
    @SerializedName("id")
    public String id;

    @SerializedName("vendor_id")
    public String vendorId;

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
