package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;

@Entity(indexes = {@Index(value = "resourceId DESC,programId DESC,materialId DESC",unique = true)})
public class MaterialTotal {

    @Id
    private Long id;

    private String resourceId;

    private String programId;

    private String materialId;

    private long totalTimes;

    private long totalDuration;

    @Generated(hash = 93341173)
    public MaterialTotal(Long id, String resourceId, String programId,
            String materialId, long totalTimes, long totalDuration) {
        this.id = id;
        this.resourceId = resourceId;
        this.programId = programId;
        this.materialId = materialId;
        this.totalTimes = totalTimes;
        this.totalDuration = totalDuration;
    }

    @Generated(hash = 1873105666)
    public MaterialTotal() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public long getTotalTimes() {
        return totalTimes;
    }

    public void setTotalTimes(long totalTimes) {
        this.totalTimes = totalTimes;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }
}
