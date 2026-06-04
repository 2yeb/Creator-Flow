package com.example.creator_flow;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/** GET /projects 응답 모델 */
public class ProjectListResponse {
    @SerializedName("id")       // 프로젝트 아이디
    private String id;
    @SerializedName("name")     // 프로젝트 이름
    private String name;
    @SerializedName("status")   // 프로젝트 상태
    private String status;
    @SerializedName("groups")   // 태그 되어있는 그룹 이름
    private List<String> groups;

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getGroups() { return groups; }
}
