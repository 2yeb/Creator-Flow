package com.example.creator_flow.network;

import com.example.creator_flow.CreateGroupRequest;
import com.example.creator_flow.GroupListResponse;
import com.example.creator_flow.model.ImageUploadResponse;
import com.example.creator_flow.model.ProjectResponse;
import com.example.creator_flow.model.UpdateProjectRequest;
// projectList
import com.example.creator_flow.ProjectListResponse;
// Group
// to-do
import com.example.creator_flow.TodoListResponse;
import com.example.creator_flow.UpdateTodoRequest;
import com.example.creator_flow.CreateTodoRequest;
import com.google.gson.JsonObject;

import java.util.List;

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

    // ── To-Do 리스트 ──────────────────────────────────────────────────────────

    /** To-Do 전체 조회 */
    @GET("todos")
    Call<List<TodoListResponse>> getTodos();

    /** 특정 프로젝트에 To-Do 생성 */
    @POST("projects/{id}/todos")
    Call<TodoListResponse> createTodo(
            @Path("id") String projectId,
            @Body CreateTodoRequest body
    );

    /** To-Do 수정 / 완료 처리 */
    @PUT("todos/{id}")
    Call<TodoListResponse> updateTodo(
            @Path("id") String todoId,
            @Body UpdateTodoRequest body
    );

    /** To-Do 삭제 */
    @DELETE("todos/{id}")
    Call<JsonObject> deleteTodo(@Path("id") String todoId);

    // ── 그룹 ──────────────────────────────────────────────────────────

    /** 그룹 전체 조회 */
    @GET("groups")
    Call<List<GroupListResponse>> getGroups();

    /** 그룹 생성 */
    @POST("groups")
    Call<GroupListResponse> createGroup(@Body CreateGroupRequest body);

    /** 그룹 삭제 */
    @DELETE("groups/{id}")
    Call<JsonObject> deleteGroup(@Path("id") String GroupId);

    /** 프로젝트에 그룹 태그 */
    //@POST("projects/{id}/groups")
    /**Call<GroupTagResponse> attachGroupToProject(
            @Path("id") String projectId,
            @Body AttachGroupRequest body
    );*/

    // ── 프로젝트 상태 ──────────────────────────────────────────────────────────

    /** 전체 프로젝트 목록 조회 */
    @GET("projects")
    Call<List<ProjectListResponse>> getProjects();

    @POST("projects")
    Call<ProjectListResponse> createProject(@Body ProjectListResponse project);
}
