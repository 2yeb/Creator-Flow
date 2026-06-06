package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** POST /projects/{id}/images 응답 모델 */
public class ImageUploadResponse {

    @SerializedName("image_id")
    public String imageId;

    @SerializedName("image_url")
    public String imageUrl;

    /** 차시 연결 정보 (2026-06 백엔드 업데이트로 추가). 차시 안 묶고 업로드하면 null */
    @SerializedName("simulation_id")
    public String simulationId;
}
