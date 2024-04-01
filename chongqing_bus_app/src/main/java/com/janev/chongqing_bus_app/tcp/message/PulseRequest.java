package com.janev.chongqing_bus_app.tcp.message;


import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.easysocket.EasySocket;
import com.janev.chongqing_bus_app.tcp.task.resource.DatePlayView;

public class PulseRequest extends IRequest {

    public PulseRequest() {
        super("03");
    }

    @Override
    protected String getTag() {
        return "发送心跳";
    }

    // 2828 【头部标识】
    // 03 【消息 ID】
    // 06 【设备地址】
    // 220120000001 【设备编号】
    // 013920170007 【终端编号】
    // 0322 【消息流水号】
    // 001d 【长度】
    // 03 线路号长度
    // 323732 线路号
    // 0000000000000000 广告资源ID
    // 00 广告资源版本号长度
    // 0000000000000000 节目单ID
    // 0000000000000000 素材ID
    // 07
    // 0c

    @Override
    protected String getHexData() {
        // 2828
        // 03
        // 06
        // 220120000001
        // 000000000000
        // 077a
        // 001e

        // 03
        // 323732
        // 0000000000000064
        // 01
        // 32
        // 0000000000000050
        // 0000000000000019

        // 4E0C


        // 2828
        // 03
        // 06
        // 220120000001
        // 000000000000
        // 07bb
        // 001e
        // 03
        // 323732
        // 0000000000000033
        // 013200000000000000000000000000000000910C

        StringBuilder stringBuilder = new StringBuilder();

        //线路号
        String lineNameHex = MessageUtils.getLineNameHex();

        String lineNameLength = MessageUtils.getLength(lineNameHex,2);
        d("线路号长度：" + lineNameLength);
        stringBuilder.append(lineNameLength);

        d("线路号：" + lineNameHex);
        stringBuilder.append(lineNameHex);

        //广告资源ID
        String publicityId = MessageUtils.getResourceID();
        d("广告资源ID：" + publicityId);
        stringBuilder.append(publicityId);

        String resourceVersion = MessageUtils.getResourceVersion();

        String length = MessageUtils.getLength(resourceVersion);
        d("广告资源版本号长度：" + length);
        stringBuilder.append(length);

        d("广告资源版本号：" + resourceVersion);
        stringBuilder.append(resourceVersion);

        long currMaterialId = DatePlayView.getCurrMaterialId();
        Material material = DaoManager.get().queryMaterialBy_Id(currMaterialId);

        String programId;
        if(material != null){
            programId = MessageUtils.addZero(material.getProgramId(),16);
        } else {
            programId = MessageUtils.addZero("",16);
        }
        d("节目单ID：" + programId);
        stringBuilder.append(programId);

        String materialId;
        if(material != null){
            materialId = MessageUtils.addZero(material.getId(),16);
        } else {
            materialId = MessageUtils.addZero("",16);
        }
        d("素材ID：" + materialId);
        stringBuilder.append(materialId);

        return stringBuilder.toString();
    }

    @Override
    public void send() {
        EasySocket.getInstance().upMessage(byteArray());
        setLastSendTime();
    }
}