package com.yunbiao.publicity_guideboard.net;

import java.util.List;

public class DriverInfoResponse {

    /*{
    "Ret":0,
    "Msg":"成功",
    "SuccStat":"0",
    "StartChargeSeq":"",
    "Data":[
        {
            "consumeType":"on",
            "drivername":"帅小洪",
            "drivercode":"100402",
            "starcode":"2",
            "party":"",
            "photo":"http://221.10.114.137:5012/joffice/attachFiles/yuangongzhaopian/2021yuangongzhaopian/100402帅小洪.jpg",
            "complainMobile":"0833-2413814"
        }
    ]
}
*/

    private String Ret;
    private String Msg;
    private String SuccStat;
    private String StartChargeSeq;
    private List<Data> Data;

    public String getRet() {
        return Ret;
    }

    public void setRet(String ret) {
        Ret = ret;
    }

    public String getMsg() {
        return Msg;
    }

    public void setMsg(String msg) {
        Msg = msg;
    }

    public String getSuccStat() {
        return SuccStat;
    }

    public void setSuccStat(String succStat) {
        SuccStat = succStat;
    }

    public String getStartChargeSeq() {
        return StartChargeSeq;
    }

    public void setStartChargeSeq(String startChargeSeq) {
        StartChargeSeq = startChargeSeq;
    }

    public List<DriverInfoResponse.Data> getData() {
        return Data;
    }

    public void setData(List<DriverInfoResponse.Data> data) {
        Data = data;
    }

    public static class Data{
        private String consumeType;
        private String drivername;
        private String drivercode;
        private String starcode;
        private String party;
        private String photo;
        private String complainMobile;

        public String getConsumeType() {
            return consumeType;
        }

        public void setConsumeType(String consumeType) {
            this.consumeType = consumeType;
        }

        public String getDrivername() {
            return drivername;
        }

        public void setDrivername(String drivername) {
            this.drivername = drivername;
        }

        public String getDrivercode() {
            return drivercode;
        }

        public void setDrivercode(String drivercode) {
            this.drivercode = drivercode;
        }

        public String getStarcode() {
            return starcode;
        }

        public void setStarcode(String starcode) {
            this.starcode = starcode;
        }

        public String getParty() {
            return party;
        }

        public void setParty(String party) {
            this.party = party;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getComplainMobile() {
            return complainMobile;
        }

        public void setComplainMobile(String complainMobile) {
            this.complainMobile = complainMobile;
        }
    }
}
