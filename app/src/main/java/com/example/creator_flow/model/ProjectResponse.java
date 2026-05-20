package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** GET/PUT /projects/{id} 응답 모델 */
public class ProjectResponse {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("status")
    public String status;

    @SerializedName("images")
    public List<ProjectImage> images;

    public static class ProjectImage {
        @SerializedName("image_id")
        public String imageId;

        @SerializedName("image_url")
        public String imageUrl;
    }
}
