package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** GET /vendors 응답 항목 */
public class VendorDto {
    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("site_url")
    public String siteUrl;

    @SerializedName("logo_url")
    public String logoUrl;

    @SerializedName("crawled_at")
    public String crawledAt;
}
