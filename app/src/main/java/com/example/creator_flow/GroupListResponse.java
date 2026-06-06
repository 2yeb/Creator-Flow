package com.example.creator_flow;

import com.google.gson.annotations.SerializedName;

public class GroupListResponse {
    @SerializedName("id")
    private String id;

    @SerializedName("keyword")
    private String keyword;

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
