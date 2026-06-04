package com.example.creator_flow;

public class ProjectStateItem {
    private String id;
    private String name;
    private String status;

    // 생성자
    public ProjectStateItem(String id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    // getter와 setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
