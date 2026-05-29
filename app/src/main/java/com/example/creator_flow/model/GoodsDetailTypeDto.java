package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /goods-types/{id}/details 응답 항목 (세부 유형) */
public class GoodsDetailTypeDto {
    @SerializedName("id")
    public String id;

    @SerializedName("goods_type_id")
    public String goodsTypeId;

    @SerializedName("name")
    public String name;
}
