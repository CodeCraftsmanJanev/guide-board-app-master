package com.janev.chongqing_bus_app.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

@Entity(indexes = {@Index(value = "lineName DESC,direction DESC,index DESC",unique = true)})
public class Site {

    @Id
    private Long id;

    private String lineName;

    private int direction;

    private int index;

    private int count;

    private String name;

    private String enName;

    private boolean isResponsive;

    @Generated(hash = 1136322986)
    public Site() {
    }

    @Generated(hash = 405091960)
    public Site(Long id, String lineName, int direction, int index, int count, String name,
            String enName, boolean isResponsive) {
        this.id = id;
        this.lineName = lineName;
        this.direction = direction;
        this.index = index;
        this.count = count;
        this.name = name;
        this.enName = enName;
        this.isResponsive = isResponsive;
    }

    public boolean isResponsive() {
        return isResponsive;
    }

    public void setResponsive(boolean responsive) {
        isResponsive = responsive;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getIsResponsive() {
        return this.isResponsive;
    }

    public void setIsResponsive(boolean isResponsive) {
        this.isResponsive = isResponsive;
    }
}
