package com.example.creator_flow;

import java.util.ArrayList;
import java.util.List;

public class GroupItem {
    private String id;
    private String keyword;
    private int backgroundColor;
    private int strokePointColor;
    private List<ProjectItem> projects;
    private boolean isExpanded;

    public GroupItem(String id, String keyword, int backgroundColor, int strokePointColor, List<ProjectItem> projects) {
        this.id = id;
        this.keyword = keyword;
        this.backgroundColor = backgroundColor;
        this.strokePointColor = strokePointColor;
        this.projects = projects;
    }

    public String getId() { return id; }
    public String getKeyword() { return keyword; }
    public int getBackgroundColor() { return backgroundColor; }
    public int getStrokePointColor() { return strokePointColor; }
    public List<ProjectItem> getProjects() { return projects; }
    public void setProjects(List<ProjectItem> projects) { this.projects = projects; }

    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
