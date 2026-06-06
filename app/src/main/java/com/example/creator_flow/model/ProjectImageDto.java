package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/**
 * GET /projects/{project_id}/images 응답 항목.
 *
 * 백엔드 ProjectImage SQLAlchemy row가 그대로 직렬화돼서 옴.
 * 2026-06-06 백엔드 업데이트로 추가된 엔드포인트 — simulation_id로
 * 차시별 이미지를 필터링하거나 전체 받아서 클라이언트에서 분배 가능.
 */
public class ProjectImageDto {

    /** 이미지 PK (백엔드에서 "id" 필드로 옴) */
    @SerializedName("id")
    public String id;

    @SerializedName("project_id")
    public String projectId;

    /**
     * 이 이미지가 속한 차시의 simulation id.
     * 어느 차시에도 묶이지 않은 프로젝트 단위 이미지면 null.
     */
    @SerializedName("simulation_id")
    public String simulationId;

    @SerializedName("image_url")
    public String imageUrl;

    /** 한 차시 내 이미지 순서 */
    @SerializedName("order")
    public Integer order;
}
