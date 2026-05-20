package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** POST /projects/{id}/images 응답 모델 */
public class ImageUploadResponse {

    @SerializedName("image_id")
    public String imageId;

    @SerializedName("image_url")
    public String imageUrl;
}
