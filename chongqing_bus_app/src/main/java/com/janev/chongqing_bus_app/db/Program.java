package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class Program {

    @Id
    private Long _id;

    @Unique
    private String id;

    private String name;

    private int state;

    private int priority;

    private long startDate;

    private long endDate;

    @Generated(hash = 484452008)
    public Program(Long _id, String id, String name, int state, int priority,
            long startDate, long endDate) {
        this._id = _id;
        this.id = id;
        this.name = name;
        this.state = state;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Generated(hash = 775603163)
    public Program() {
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

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getStartDate() {
        return this.startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return this.endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }


}
