package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

@Entity(
        indexes = {
                @Index(value = "materialId, startTime, endTime", unique = true)
        }
)
public class Time {

    @Id
    private Long id;

    private String materialId;

    private long startTime;

    private long endTime;


    // 素材连续播放次数，只有视频才有效，其他只播放一次
    private long repeatTimes;

    // 单次播放时长，秒。 0:表示按照视频实际播放时长, 图片按照默认 10 秒播放；其他值: 指定播放时长
    private long duration;

    // 此时段内播放总次数, 0:不限制次数； 总播放次数和总播放时长，只要有一个达到了，就停止播放
    private long totalTimes;

    // 此时段内播放总时长, 秒, 0:不限制播放时长；总播放次数和总播放时长，只要有一个达到了，就停止播放
    private long totalDuration;


    //时间段内的总计次（需要存储）
    private long playTotalTimes;

    //时间段内的总时长（需要存储）
    private long playTotalDuration;

    private long playLastDate;

    @Generated(hash = 1670630475)
    public Time(Long id, String materialId, long startTime, long endTime, long repeatTimes,
            long duration, long totalTimes, long totalDuration, long playTotalTimes,
            long playTotalDuration, long playLastDate) {
        this.id = id;
        this.materialId = materialId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatTimes = repeatTimes;
        this.duration = duration;
        this.totalTimes = totalTimes;
        this.totalDuration = totalDuration;
        this.playTotalTimes = playTotalTimes;
        this.playTotalDuration = playTotalDuration;
        this.playLastDate = playLastDate;
    }

    @Generated(hash = 37380482)
    public Time() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaterialId() {
        return this.materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getRepeatTimes() {
        return this.repeatTimes;
    }

    public void setRepeatTimes(long repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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
