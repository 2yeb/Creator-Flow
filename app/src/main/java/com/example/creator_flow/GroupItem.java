package com.example.creator_flow;

import java.util.ArrayList;
import java.util.List;

public class GroupItem {
    private String id;
    private String keyword;
    private int colorResId;
    private List<ProjectItem> projects;
    private boolean isExpanded;

    public GroupItem(String id, String keyword, int colorResId, List<ProjectItem> projects) {
        this.id = id;
        this.keyword = keyword;
        this.colorResId = colorResId;
        this.projects = projects != null ? projects : new ArrayList<>();
        this.isExpanded = true;
    }

    public String getId() { return id; }
    public String getKeyword() { return keyword; }
    public int getColorResId() { return colorResId; }
    public List<ProjectItem> getProjects() { return projects; }
    public void setProjects(List<ProjectItem> projects) { this.projects = projects; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
