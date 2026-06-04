package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /vendor-products/{id}/options 응답 항목 */
public class ProductOptionDto {
    @SerializedName("id")
    public String id;

    @SerializedName("vendor_product_id")
    public String vendorProductId;

    @SerializedName("option_name")
    public String optionName;     // 예: "용지", "코팅"

    @SerializedName("option_value")
    public String optionValue;    // 예: "유광", "라미"

    @SerializedName("extra_price")
    public Integer extraPrice;
}
