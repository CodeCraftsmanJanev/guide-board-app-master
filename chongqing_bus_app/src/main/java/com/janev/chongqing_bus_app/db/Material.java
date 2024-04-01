package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@Entity(
        indexes = {
                @Index(value = "programId, id", unique = true)
        }
)
public class Material {

    @Id
    private Long _id;

    private String programId;

    private String id;

    private int materialType;

    private String name;

    private long size;

    private String md5;

    private String url;

    private int order;

    // 一天内播放总次数, 0:不限制次数； 总播放次数和总播放时长，只要有一个达到了，就停止播放
    private long totalTimes;

    // 一天内播放总时长, 秒, 0:不限制播放时长；总播放次数和总播放时长，只要有一个达到了，就停止播放
    private long totalDuration;

    private long playTotalTimes;

    private long playTotalDuration;

    //根据此值来判断播放次数和播放时长是否有效
    private long playLastDate;

    @Generated(hash = 1727563352)
    public Material(Long _id, String programId, String id, int materialType,
            String name, long size, String md5, String url, int order,
            long totalTimes, long totalDuration, long playTotalTimes,
            long playTotalDuration, long playLastDate) {
        this._id = _id;
        this.programId = programId;
        this.id = id;
        this.materialType = materialType;
        this.name = name;
        this.size = size;
        this.md5 = md5;
        this.url = url;
        this.order = order;
        this.totalTimes = totalTimes;
        this.totalDuration = totalDuration;
        this.playTotalTimes = playTotalTimes;
        this.playTotalDuration = playTotalDuration;
        this.playLastDate = playLastDate;
    }

    @Generated(hash = 1176792654)
    public Material() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getProgramId() {
        return this.programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaterialType() {
        return this.materialType;
    }

    public void setMaterialType(int materialType) {
        this.materialType = materialType;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public long getTotalTimes() {
        return this.totalTimes;
    }

    public void setTotalTimes(long totalTimes) {
        this.totalTimes = totalTimes;
    }

    public long getTotalDuration() {
        return this.totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public long getPlayTotalTimes() {
        return this.playTotalTimes;
    }

    public void setPlayTotalTimes(long playTotalTimes) {
        this.playTotalTimes = playTotalTimes;
    }

    public long getPlayTotalDuration() {
        return this.playTotalDuration;
    }

    public void setPlayTotalDuration(long playTotalDuration) {
        this.playTotalDuration = playTotalDuration;
    }

    public long getPlayLastDate() {
        return this.playLastDate;
    }

    public void setPlayLastDate(long playLastDate) {
        this.playLastDate = playLastDate;
    }


}
