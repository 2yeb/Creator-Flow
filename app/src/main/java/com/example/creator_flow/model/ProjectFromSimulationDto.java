package com.example.creator_flow.model;

import com.google.gson.annotations.SerializedName;

/** POST /simulations/{id}/project 응답 */
public class ProjectFromSimulationDto {
    @SerializedName("project_id")
    public String projectId;

    @SerializedName("name")
    public String name;

    @SerializedName("simulation_id")
    public String simulationId;
}
