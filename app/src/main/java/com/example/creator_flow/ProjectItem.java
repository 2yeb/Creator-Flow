package com.example.creator_flow;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProjectItem {
    @SerializedName("id")       // 프로젝트 아이디
    private String id;
    @SerializedName("name")     // 프로젝트 이름
    private String name;
    @SerializedName("status")   // 프로젝트 상태
    private String status;
    @SerializedName("groups")   // 태그 되어있는 그룹 이름
    private List<String> groups;

    // 기본 생성자 (statistics 화면에서 신규 프로젝트를 생성할 때 사용)
    public ProjectItem(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.groups = new ArrayList<>();
    }

    // 그룹을 포함하는 생성자
    public ProjectItem(String id, String name, String status, List<String> groups) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.groups = groups != null ? groups : new ArrayList<>();
    }


    // getter & setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getGroups() { return groups; }
}
