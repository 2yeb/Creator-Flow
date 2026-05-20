package com.example.creator_flow.model;

import android.net.Uri;

public class ProjectFile {

    private String projectName; // 프로젝트명
    private String category;    // 종류 (지류, 아크릴 등)
    private String date;        // 날짜
    private int chasiNumber;    // 차시 번호 (1차시, 2차시 ...)
    private double price;       // 단가
    private int soldQuantity;   // 현재 판매량 (사용자 입력)
    private int targetQuantity; // 목표 판매량 (사용자 입력)
    private String serverId;    // 서버에서 받은 project id (API 연동용)
    private Uri thumbnailUri;   // 차시별 썸네일 이미지 URI

    public ProjectFile(int chasiNumber, String projectName, String category, String date) {
        this.chasiNumber = chasiNumber;
        this.projectName = projectName;
        this.category = category;
        this.date = date;
    }

    public int getChasiNumber() { return chasiNumber; }
    public void setChasiNumber(int chasiNumber) { this.chasiNumber = chasiNumber; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(int soldQuantity) { this.soldQuantity = soldQuantity; }

    public int getTargetQuantity() { return targetQuantity; }
    public void setTargetQuantity(int targetQuantity) { this.targetQuantity = targetQuantity; }

    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }

    public Uri getThumbnailUri() { return thumbnailUri; }
    public void setThumbnailUri(Uri thumbnailUri) { this.thumbnailUri = thumbnailUri; }
}
