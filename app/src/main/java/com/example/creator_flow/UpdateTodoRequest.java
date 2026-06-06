package com.example.creator_flow;

import com.google.gson.annotations.SerializedName;

public class UpdateTodoRequest {

    @SerializedName("content")
    private String content;
    @SerializedName("isDone")
    private boolean isDone;

    public UpdateTodoRequest(String content, boolean isDone) {
        this.content = content;
        this.isDone = isDone;
    }
}
