package com.janev.chongqing_bus_app.tcp.client;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;
import com.janev.chongqing_bus_app.easysocket.entity.OriginReadData;
import com.janev.chongqing_bus_app.easysocket.entity.SocketAddress;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.message.ResourceInfoReportRequest;
import com.janev.chongqing_bus_app.tcp.message.message_utils.FileUploadMessageUtils;
import com.janev.chongqing_bus_app.tcp.message.LoginRequest;
import com.janev.chongqing_bus_app.tcp.message.message_utils.ParamsQueryMessageUtils;
import com.janev.chongqing_bus_app.tcp.message.message_utils.ParamsSetMessageUtils;
import com.janev.chongqing_bus_app.tcp.message.message_utils.ScreenShotMessageUtils;
import com.janev.chongqing_bus_app.tcp.message.ActiveUpgradeRequest;
import com.janev.chongqing_bus_app.tcp.message.PulseRequest;
import com.janev.chongqing_bus_app.tcp.message.message_utils.UpgradeMessageUtils;
import com.janev.chongqing_bus_app.utils.BytesUtils;

import java.nio.ByteBuffer;
import java.util.Date;

public class ChongqingV2Client extends TCPClient {

    public ChongqingV2Client() {
        super(MessageUtils.getMainServerAddress(),MessageUtils.getMainServerPort(),
                MessageUtils.getSpareServerAddress(),MessageUtils.getSpareServerPort(),
                MessageUtils.getPulseInterval() * 1000L,
                30,
                new ChongqingV2Resolver());
    }

    @Override
    public byte[] heartBeatBytes() {
        return new PulseRequest().byteArray();
    }

    @Override
    public boolean isServerHeartbeat(OriginReadData orginReadData) {
        if(orginReadData.getOriginDataBytes() != null){
            byte[] bodyBytes = orginReadData.getOriginDataBytes();
            String start = BytesUtils.bytesToHex(BytesUtils.SubByte(bodyBytes, 0, 2));
            String end = BytesUtils.byteToHex(bodyBytes[bodyBytes.length - 1]);
            if(TextUtils.equals("2828",start.toUpperCase()) && TextUtils.equals("0C",end.toUpperCase())){
                byte order = bodyBytes[2];
                if(order == (byte) 0x81){
                    byte responseByte = bodyBytes[20];
                    if(responseByte == (byte)0x03){
                        pulseTime = System.currentTimeMillis();
                        Log.e("PulseTime", "服务器响应心跳：" + pulseTime);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {
        super.onSocketConnSuccess(socketAddress);
        login();
    }

    @Override
    protected String getLogonState() {
        if(loginData == null){
            return "未登录";
        }

        int result = loginData.getResult();
        if(result == 0){
            return "已登录";
        }

        if(result == -1){
            return "未登录";
        }

        return "登录失败";
    }

    @Override
    protected String getLogonTime() {
        if(loginData != null && loginData.isSuccess()){
            return loginData.getLastResultTime();
        }
        return "";
    }

    @Override
    protected String getPulseTime() {
        if(pulseTime == 0){
            return "";
        }
        String s = TimeUtils.date2String(new Date(pulseTime), "HH:mm:ss");
        Log.e("PulseTime", "getPulseTime: 获取心跳响应时间：" + s);
        return s;
    }

    @Override
    public void resolveCallback(byte order, String msgSerial, ByteBuffer byteBuffer) {
        switch (order) {
            case (byte) 0x81://平台回复
                d("平台回复");
                response(byteBuffer);
                break;
            case (byte)0x84://参数查询（平台命令）
                d("参数查询（平台命令）");
                ParamsQueryMessageUtils.query(order,msgSerial,byteBuffer);
                break;
            case (byte)0x85://参数设置（平台命令）
                d("参数设置（平台命令）");
                ParamsSetMessageUtils.set(order,msgSerial,byteBuffer);
                break;
            case (byte)0x86://平台下发设备升级指令（平台命令）
                d("平台下发设备升级指令（平台命令）");
                UpgradeMessageUtils.deviceUpgrade(order,msgSerial,byteBuffer);
                break;
            case (byte) 0x87://升级查询应答（平台回复）
                d("升级查询应答（平台回复）");
                handleUpgradeResponse(order,msgSerial,byteBuffer);
                break;
            case (byte)0x88://广告信息查询（平台命令）
                d("广告信息查询（平台命令）");
                responseReportResourceInfo(msgSerial);
                break;
            case (byte)0x89://拍照截图（平台命令）
                d("拍照截图（平台命令）");
                ScreenShotMessageUtils.screenShot(order,msgSerial,byteBuffer);
                break;
            case (byte)0x8A://文件上传（平台命令）
                d("文件上传（平台命令）");
                FileUploadMessageUtils.upload(order, msgSerial, byteBuffer);
                break;
//            case (byte)0x8E://宣传语信息
//                d("宣传语信息");
//                KnowledgeMessageUtils.resolve(order,msgSerial,data);
//                break;
        }
    }

    private long pulseTime;
    /**
     * 平台应答
     */
    private void response(ByteBuffer byteBuffer){
        byte responseOrder = byteBuffer.get();

        String responseSerial = getBytesHex(byteBuffer, 2);

        byte responseResult = byteBuffer.get();

        switch (responseOrder) {
            case 0x02://登录
                setLoginResult(responseResult);
                break;
            case 0x08://主动上报广告信息应答
                setActiveReportResult(responseResult);
                break;
        }
    }

    /**
     * 主动请求登录
     */
    private LoginRequest loginData;
    private void login(){
        if(loginData == null){
            loginData = new LoginRequest(loginSuccessRunnable);
        }

        if(!loginData.isSuccess()){
            d("请求登录");
            loginData.send();
        }
    }

    /**
     * 登录结果
     * @param result
     */
    private void setLoginResult(int result){
        d("登录结果：" + result);
        if(loginData != null){
            loginData.setResult(result);
        }
    }

    /**
     * 登录成功回调
     */
    private final Runnable loginSuccessRunnable = () -> {
        startHeatBeat();
        activeReportResourceInfo();
    };

    /**
     * 主动上报广告信息
     */
    private ResourceInfoReportRequest resourceInfoReportRequest;
    private void activeReportResourceInfo(){
        d("主动上报广告信息");
        if(resourceInfoReportRequest == null){
            resourceInfoReportRequest = new ResourceInfoReportRequest("",firstReportFinishRunnable);
        }

        if(!resourceInfoReportRequest.isSuccess()){
            resourceInfoReportRequest.autoSend();
        }
    }

    private void setActiveReportResult(int result){
        d("主动上报广告信息结果：" + result);
        if(resourceInfoReportRequest != null){
            resourceInfoReportRequest.setResult(result);
        }
    }

    private final Runnable firstReportFinishRunnable = this::activeQueryUpgradeAds;

    /**
     * 广告信息查询回复
     * @param msgSerial
     */
    private void responseReportResourceInfo(String msgSerial){
        new ResourceInfoReportRequest(msgSerial,null).send();
    }

    private void handleUpgradeResponse(byte order, String msgSerial, ByteBuffer byteBuffer){
        String responseSerial = MessageUtils.getBytesHex(byteBuffer, 2);

        byte queryType = byteBuffer.get();

        switch (queryType) {
            case 0x02://应用程序
                activeQueryUpgradeAppResponse();
                break;
            case 0x03://应用程序资源包
//                activeQueryUpgradeAppResourceResponse();
                break;
            case 0x05://广告资源文件
                activeQueryUpgradeAdsResponse();
                break;
        }

        byteBuffer.position(0);
        UpgradeMessageUtils.activeUpgrade(order,msgSerial,byteBuffer);
    }

    /**
     * 主动查询升级
     */
    private ActiveUpgradeRequest upgradeAdsRequest;
    private void activeQueryUpgradeAds(){
        if(upgradeAdsRequest == null){
            upgradeAdsRequest = new ActiveUpgradeRequest(ActiveUpgradeRequest.ADS_RESOURCE);
        }

        if(!upgradeAdsRequest.isSuccess()){
            d("主动查询广告升级");
            upgradeAdsRequest.sendWaitResult();
        }
    }

    private void activeQueryUpgradeAdsResponse(){
        d("主动查询响应：广告资源文件");
        if(upgradeAdsRequest != null){
            upgradeAdsRequest.setResult(0);
        }

        activeQueryUpgradeApp();
    }

    private ActiveUpgradeRequest upgradeAppRequest;
    private void activeQueryUpgradeApp(){
        if(upgradeAppRequest == null){
            upgradeAppRequest = new ActiveUpgradeRequest(ActiveUpgradeRequest.APP);
        }

        if(!upgradeAppRequest.isSuccess()){
            d("主动查询APP升级");
            upgradeAppRequest.sendWaitResult();
        }
    }

    private void activeQueryUpgradeAppResponse(){
        d("主动查询响应：应用程序");
        if(upgradeAppRequest != null){
            upgradeAppRequest.setResult(0);
        }
    }


//    private UpgradeRequest upgradeAppResourceRequest;
//    private void activeQueryUpgradeAppResource(){
//        if(upgradeAppResourceRequest == null){
//            upgradeAppResourceRequest = new UpgradeRequest(UpgradeRequest.APP_RESOURCE);
//        }
//
//        if(!upgradeAppResourceRequest.isSuccess()){
//            d("主动查询APP资源升级");
//            upgradeAppResourceRequest.sendWaitResult();
//        }
//    }
//
//    private void activeQueryUpgradeAppResourceResponse(){
//        d("主动查询响应：应用程序资源包");
//        if(!upgradeAppResourceRequest.isSuccess()){
//            upgradeAppResourceRequest.setResult(0);
//        }
//
//        activeQueryUpgradeApp();
//    }

    @Override
    public void destroy() {
        super.destroy();
        UpgradeMessageUtils.destroy();
    }

    //参数查询应答
    //2828870022012000000100000000000074ea000f000005000000000000000000000000110c2828810022012000000100000000000027370004030ddc00450c
    //28288600220120000001000000000000350f00740400000000000000003103312e31536674703a2f2f3132312e352e3131312e3135302f4164766572745265736f757263655061636b6167652f39643132653166382d666164662d343232352d386533642d3535346530663361393361312e6a736f6e0766747075736572096d6d5f313233343536f30c

    //2828860022012000000100000000000051a800740500000000000000003103312e30536674703a2f2f3132312e352e3131312e3135302f4164766572745265736f757263655061636b6167652f39643132653166382d666164662d343232352d386533642d3535346530663361393361312e6a736f6e0766747075736572096d6d5f313233343536300c

    //28 28 86 00 22 01 20 00 00 01 00 00 00 00 00 00 51 A8 00 74 05 00 00 00 00 00 00 00 00 31 03 31 2E 30 53 66 74 70 3A 2F 2F 31 32 31 2E 35 2E 31 31 31 2E 31 35 30 2F 41 64 76 65 72 74 52 65 73 6F 75 72 63 65 50 61 63 6B 61 67 65 2F 39 64 31 32 65 31 66 38 2D 66 61 64 66 2D 34 32 32 35 2D 38 65 33 64 2D 35 35 34 65 30 66 33 61 39 33 61 31 2E 6A 73 6F 6E 07 66 74 70 75 73 65 72 09 6D 6D 5F 31 32 33 34 35 36 30 0C


    protected int getByteInt(ByteBuffer byteBuffer){
        return BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
    }
    protected String getByteHex(ByteBuffer byteBuffer){
        return BytesUtils.byteToHex(byteBuffer.get());
    }
    protected int getBytesInt(ByteBuffer byteBuffer,int length){
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        return BytesUtils.hex16to10(BytesUtils.bytesToHex(bytes));
    }
    protected String getBytesHex(ByteBuffer byteBuffer,int length){
        if(length > 0){
            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);
            return BytesUtils.bytesToHex(bytes);
        } else {
            return "";
        }
    }
}
