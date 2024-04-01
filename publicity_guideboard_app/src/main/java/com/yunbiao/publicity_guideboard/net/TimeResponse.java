package com.yunbiao.publicity_guideboard.net;

import java.util.List;

public class TimeResponse {

    private boolean success;
    private int totalCounts;
    private List<Result> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getTotalCounts() {
        return totalCounts;
    }

    public void setTotalCounts(int totalCounts) {
        this.totalCounts = totalCounts;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public static class Result {
        private long id;
        private String app_version;
        private String download_address;
        private String app_package;
        private String enabled;
        private String cid;
        private String cname;
        private long time;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getApp_version() {
            return app_version;
        }

        public void setApp_version(String app_version) {
            this.app_version = app_version;
        }

        public String getDownload_address() {
            return download_address;
        }

        public void setDownload_address(String download_address) {
            this.download_address = download_address;
        }

        public String getApp_package() {
            return app_package;
        }

        public void setApp_package(String app_package) {
            this.app_package = app_package;
        }

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }

        public String getCid() {
            return cid;
        }

        public void setCid(String cid) {
            this.cid = cid;
        }

        public String getCname() {
            return cname;
        }

        public void setCname(String cname) {
            this.cname = cname;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
    }


}
