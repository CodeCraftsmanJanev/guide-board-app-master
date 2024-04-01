package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@Entity(
        indexes = {
                @Index(value = "routeNum, direction, stationNum", unique = true)
        }
)
public class StationPicture {

    @Id
    private Long _id;

    private String routeNum;

    private int direction;

    private int stationNum;

    private long duration;

    private String id;

    private int type;

    private String name;

    private long size;

    private String md5;

    private String url;

@Generated(hash = 311546148)
public StationPicture(Long _id, String routeNum, int direction, int stationNum,
        long duration, String id, int type, String name, long size, String md5,
        String url) {
    this._id = _id;
    this.routeNum = routeNum;
    this.direction = direction;
    this.stationNum = stationNum;
    this.duration = duration;
    this.id = id;
    this.type = type;
    this.name = name;
    this.size = size;
    this.md5 = md5;
    this.url = url;
}

@Generated(hash = 679337739)
public StationPicture() {
}

public Long get_id() {
    return this._id;
}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void set_id(Long _id) {
    this._id = _id;
}

public String getRouteNum() {
    return this.routeNum;
}

public void setRouteNum(String routeNum) {
    this.routeNum = routeNum;
}

public int getStationNum() {
    return this.stationNum;
}

public void setStationNum(int stationNum) {
    this.stationNum = stationNum;
}

public int getDirection() {
    return this.direction;
}

public void setDirection(int direction) {
    this.direction = direction;
}

public int getType() {
    return this.type;
}

public void setType(int type) {
    this.type = type;
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

}
