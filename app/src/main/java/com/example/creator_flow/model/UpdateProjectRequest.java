package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** PUT /projects/{id} 요청 바디 */
public class UpdateProjectRequest {

    @SerializedName("name")
    public String name;

    @SerializedName("status")
    public String status;

    public UpdateProjectRequest(String name, String status) {
        this.name   = name;
        this.status = status;
    }
}
