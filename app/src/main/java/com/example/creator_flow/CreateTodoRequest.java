package com.example.creator_flow;

import com.google.gson.annotations.SerializedName;

public class  CreateTodoRequest {
    @SerializedName("content")
    private String content;

    public CreateTodoRequest(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
}
