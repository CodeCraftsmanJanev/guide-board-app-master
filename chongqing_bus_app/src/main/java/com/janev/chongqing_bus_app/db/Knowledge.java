package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class Knowledge {

    @Id
    private Long id;

    @Unique
    private String content;

    @Generated(hash = 994064878)
    public Knowledge(Long id, String content) {
        this.id = id;
        this.content = content;
    }

    @Generated(hash = 2109785241)
    public Knowledge() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
