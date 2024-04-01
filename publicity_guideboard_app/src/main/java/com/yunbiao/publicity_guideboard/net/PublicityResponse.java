package com.yunbiao.publicity_guideboard.net;

import java.util.List;

public class PublicityResponse {

    private boolean success;
    private String message;
    private int code;
    private Result result;
    private Result result2;
    private Result result3;
    private Result result4;
    private long timestamp;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Result getResult2() {
        return result2;
    }

    public void setResult2(Result result2) {
        this.result2 = result2;
    }

    public Result getResult3() {
        return result3;
    }

    public void setResult3(Result result3) {
        this.result3 = result3;
    }

    public Result getResult4() {
        return result4;
    }

    public void setResult4(Result result4) {
        this.result4 = result4;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PublicityResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", code=" + code +
                ", result=" + result +
                ", result2=" + result2 +
                ", result3=" + result3 +
                ", result4=" + result4 +
                ", timestamp=" + timestamp +
                '}';
    }

    public static class Result{
        private long id;//主键id
        private String busCode;//车号
        private String taskNumber;//任务编号
        private String taskName;//任务名称
        private int state;//广告状态0已发送1已接收
        private String releaseDate;//发布日期
        private String takeOverDate;//接收日期
        private String createBy;//创建人
        private String createTime;//创建时间
        private String updateBy;//更新人
        private String updateTime;//更新时间
        private int delFlag;//是否删除0未1删
        private List<Advertise> hyAdvertisingReleaseList;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getBusCode() {
            return busCode;
        }

        public void setBusCode(String busCode) {
            this.busCode = busCode;
        }

        public String getTaskNumber() {
            return taskNumber;
        }

        public void setTaskNumber(String taskNumber) {
            this.taskNumber = taskNumber;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getTakeOverDate() {
            return takeOverDate;
        }

        public void setTakeOverDate(String takeOverDate) {
            this.takeOverDate = takeOverDate;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getUpdateBy() {
            return updateBy;
        }

        public void setUpdateBy(String updateBy) {
            this.updateBy = updateBy;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public int getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(int delFlag) {
            this.delFlag = delFlag;
        }

        public List<Advertise> getHyAdvertisingReleaseList() {
            return hyAdvertisingReleaseList;
        }

        public void setHyAdvertisingReleaseList(List<Advertise> hyAdvertisingReleaseList) {
            this.hyAdvertisingReleaseList = hyAdvertisingReleaseList;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "id=" + id +
                    ", busCode='" + busCode + '\'' +
                    ", taskNumber='" + taskNumber + '\'' +
                    ", taskName='" + taskName + '\'' +
                    ", state=" + state +
                    ", releaseDate='" + releaseDate + '\'' +
                    ", takeOverDate='" + takeOverDate + '\'' +
                    ", createBy='" + createBy + '\'' +
                    ", createTime='" + createTime + '\'' +
                    ", updateBy='" + updateBy + '\'' +
                    ", updateTime='" + updateTime + '\'' +
                    ", delFlag=" + delFlag +
                    ", hyAdvertisingReleaseList=" + hyAdvertisingReleaseList +
                    '}';
        }
    }

    public static class Advertise{
        private long id;//主键id
        private String advertisingNumber;//广告编号
        private String advertisingName;//广告名称
        private String fileName;//文件名称
        private String filePath;//文件ftp路径
        private int fileType;//文件类型 0JPG  1MP4
        private String releaseDate;//发布日期
        private int whetherIssued;//是否发布0未1发
        private String createBy;//创建人
        private String createTime;//创建时间
        private String updateBy;//更新人
        private String updateTime;//更新时间
        private int delFlag;//是否删除0未1删

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getAdvertisingNumber() {
            return advertisingNumber;
        }

        public void setAdvertisingNumber(String advertisingNumber) {
            this.advertisingNumber = advertisingNumber;
        }

        public String getAdvertisingName() {
            return advertisingName;
        }

        public void setAdvertisingName(String advertisingName) {
            this.advertisingName = advertisingName;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public int getFileType() {
            return fileType;
        }

        public void setFileType(int fileType) {
            this.fileType = fileType;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(String releaseDate) {
            this.releaseDate = releaseDate;
        }

        public int getWhetherIssued() {
            return whetherIssued;
        }

        public void setWhetherIssued(int whetherIssued) {
            this.whetherIssued = whetherIssued;
        }

        public String getCreateBy() {
            return createBy;
        }

        public void setCreateBy(String createBy) {
            this.createBy = createBy;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getUpdateBy() {
            return updateBy;
        }

        public void setUpdateBy(String updateBy) {
            this.updateBy = updateBy;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public int getDelFlag() {
            return delFlag;
        }

        public void setDelFlag(int delFlag) {
            this.delFlag = delFlag;
        }

        @Override
        public String toString() {
            return "Advertise{" +
                    "id=" + id +
                    ", advertisingNumber='" + advertisingNumber + '\'' +
                    ", advertisingName='" + advertisingName + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", filePath='" + filePath + '\'' +
                    ", fileType=" + fileType +
                    ", releaseDate='" + releaseDate + '\'' +
                    ", whetherIssued=" + whetherIssued +
                    ", createBy='" + createBy + '\'' +
                    ", createTime='" + createTime + '\'' +
                    ", updateBy='" + updateBy + '\'' +
                    ", updateTime='" + updateTime + '\'' +
                    ", delFlag=" + delFlag +
                    '}';
        }
    }

}
