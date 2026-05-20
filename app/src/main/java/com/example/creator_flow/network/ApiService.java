package com.example.creator_flow.network;

import com.example.creator_flow.model.ImageUploadResponse;
import com.example.creator_flow.model.ProjectResponse;
import com.example.creator_flow.model.UpdateProjectRequest;
import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    // ── 프로젝트 ──────────────────────────────────────────────────────────────

    /** 프로젝트 상세 조회 */
    @GET("projects/{id}")
    Call<ProjectResponse> getProject(@Path("id") String projectId);

    /** 프로젝트 이름/상태 수정 */
    @PUT("projects/{id}")
    Call<ProjectResponse> updateProject(
            @Path("id") String projectId,
            @Body UpdateProjectRequest body);

    // ── 프로젝트 이미지 ───────────────────────────────────────────────────────

    /** 이미지 업로드 (multipart/form-data) */
    @Multipart
    @POST("projects/{id}/images")
    Call<ImageUploadResponse> uploadImage(
            @Path("id") String projectId,
            @Part MultipartBody.Part image);

    /** 이미지 삭제 */
    @DELETE("projects/{id}/images/{image_id}")
    Call<JsonObject> deleteImage(
            @Path("id") String projectId,
            @Path("image_id") String imageId);
}
