package com.example.creator_flow;

import com.google.gson.annotations.SerializedName;

public class CreateGroupRequest {
    @SerializedName("keyword")
    private String keyword;

    public CreateGroupRequest() {
    }

    public CreateGroupRequest(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
