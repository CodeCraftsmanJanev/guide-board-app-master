package com.yunbiao.publicity_guideboard.db;

import android.text.TextUtils;

import com.blankj.utilcode.util.FileUtils;
import com.yunbiao.publicity_guideboard.net.PublicityResponse;
import com.yunbiao.publicity_guideboard.system.Path;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Unique;

import java.io.File;

@Entity
public class Advert {

    @Id
    private Long id;

    @Unique
    private String number;

    private String filePath;

    private int fileType;

    private int whetherIssued;

    private String fileName;

    private String localPath;

    private int result;

    private String message;

    @Generated(hash = 1735085677)
    public Advert(Long id, String number, String filePath, int fileType,
            int whetherIssued, String fileName, String localPath, int result,
            String message) {
        this.id = id;
        this.number = number;
        this.filePath = filePath;
        this.fileType = fileType;
        this.whetherIssued = whetherIssued;
        this.fileName = fileName;
        this.localPath = localPath;
        this.result = result;
        this.message = message;
    }

    @Generated(hash = 1260794334)
    public Advert() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getFileType() {
        return this.fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public int getWhetherIssued() {
        return this.whetherIssued;
    }

    public void setWhetherIssued(int whetherIssued) {
        this.whetherIssued = whetherIssued;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Advert create(PublicityResponse.Advertise advertise){
        this.id = advertise.getId();
        this.number = advertise.getAdvertisingNumber();
        this.filePath = advertise.getFilePath();
        this.whetherIssued = advertise.getWhetherIssued();
        this.fileType = advertise.getFileType();
        //生成本地数据信息
        this.fileName = getFileName(this.filePath);
        this.localPath = new File(Path.getResourcePath(),this.fileName).getPath();
        return this;
    }

    public void update(PublicityResponse.Advertise advertise) {
        //如果文件地址不同或文件类型不同则更新并重置本地数据
        if(!TextUtils.equals(advertise.getFilePath(),this.filePath)
                || this.fileType != advertise.getFileType()){
            //删除原本的数据
            if(!TextUtils.isEmpty(this.filePath) && FileUtils.isFileExists(this.filePath)){
                FileUtils.delete(this.filePath);
            }
            this.fileType = advertise.getFileType();
            this.filePath = advertise.getFilePath();
            //重置本地数据信息
            this.fileName = getFileName(this.filePath);
            this.localPath = new File(Path.getResourcePath(),fileName).getPath();
            this.result = 0;
            this.message = "";
        }
        whetherIssued = advertise.getWhetherIssued();
    }

    public boolean compare(PublicityResponse.Advertise advertise){
        return this.fileType == advertise.getFileType() //文件类型相同
                && TextUtils.equals(this.filePath,advertise.getFilePath())//文件地址相同
                && this.whetherIssued == advertise.getWhetherIssued();//发布情况相同
    }

    private String getFileName(String filePath){
        int last1 = filePath.lastIndexOf("/");
        int last2 = filePath.lastIndexOf("=");
        //说明/为分隔符
        if(last1 > last2){
            return filePath.substring(last1 + 1);
        } else {
            return filePath.substring(last2 + 1);
        }
    }

    @Override
    public String toString() {
        return "Advert{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileType=" + fileType +
                ", whetherIssued=" + whetherIssued +
                ", fileName='" + fileName + '\'' +
                ", localPath='" + localPath + '\'' +
                ", result=" + result +
                ", message='" + message + '\'' +
                '}';
    }
}
