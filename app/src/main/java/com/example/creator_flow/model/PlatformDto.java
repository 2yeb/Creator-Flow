package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /platforms 응답 항목 */
public class PlatformDto {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("supports_fulfillment")
    public Boolean supportsFulfillment;

    @SerializedName("supports_pod")
    public Boolean supportsPod;
}
