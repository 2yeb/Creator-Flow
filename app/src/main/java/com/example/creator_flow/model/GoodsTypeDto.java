package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /goods-types 응답 항목 */
public class GoodsTypeDto {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;
}
