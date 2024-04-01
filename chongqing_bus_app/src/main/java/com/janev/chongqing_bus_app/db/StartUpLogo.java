package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class StartUpLogo {

    @Id
    private Long _id;

    @Unique
    private String id;

    private String name;

    private int type;

    private long size;

    private String md5;

    private String url;

    @Generated(hash = 967878674)
    public StartUpLogo(Long _id, String id, String name, int type, long size,
            String md5, String url) {
        this._id = _id;
        this.id = id;
        this.name = name;
        this.type = type;
        this.size = size;
        this.md5 = md5;
        this.url = url;
    }

    @Generated(hash = 67242041)
    public StartUpLogo() {
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
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
