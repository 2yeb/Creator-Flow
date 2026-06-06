package com.example.creator_flow;

import com.google.gson.annotations.SerializedName;

/** GET /todos 응답 모델 */
// 전체 to-do 리스트를 불러옴
public class TodoListResponse {
    @SerializedName("id")
    private String id;
    @SerializedName("project_id")
    private String project_id;
    @SerializedName("project_name")
    private String project_name;
    @SerializedName("content")  // to-do 내용
    private String content;
    @SerializedName("is_done")  // 완료 상태 체크
    private boolean is_done;


    // Getter& Setter
    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public String getProjectId() {return project_id;}

    public void setProject_id(String project_id) {this.project_id = project_id;}

    public String getProjectName() {return project_name;}

    public void setProject_name(String project_name) {this.project_name = project_name;}

    public String getContent() {return content;}

    public void setContent(String content) {this.content = content;}

    public boolean isDone() {return is_done;}

    public void setDone(boolean done) {is_done = done;}
}