package com.example.creator_flow;

/** POST /projects/{id}/todos */
public class TodoItem {

    private String id;
    private String project_id;
    private String project_name;
    private String content;
    private boolean is_done;

    // 생성자 (필요에 따라 생성)
    public TodoItem(String content) {
        this.id = "";
        this.project_id = "";
        this.project_name = "";
        this.content = content;
        this.is_done = false;
    }

    public String getId() { return id; }
    public void setId(String id) {this.id = id;}
    public String getProjectId() { return project_id; }
    public void setProject_id(String project_id) { this.project_id = project_id; }
    public String getProjectName() { return project_name; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isDone() { return is_done; }
    public void setDone(boolean done) { is_done = done; }
}
