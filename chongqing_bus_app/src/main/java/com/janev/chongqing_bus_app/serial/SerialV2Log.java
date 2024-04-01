//package com.janev.chongqing_bus_app.serial;
//
//import com.janev.chongqing_bus_app.system.Path;
//import com.janev.chongqing_bus_app.tcp.DataLog;
//import com.janev.chongqing_bus_app.utils.BytesUtils;
//import com.janev.chongqing_bus_app.utils.StringUtils;
//
//import java.nio.ByteBuffer;
//
//import io.reactivex.Observable;
//import io.reactivex.ObservableSource;
//import io.reactivex.functions.Function;
//
//public class SerialV2Log extends DataLog {
//
//    private static final class Holder {
//        public static final SerialV2Log INSTANCE = new SerialV2Log();
//    }
//
//    public static SerialV2Log getInstance(){
//        return SerialV2Log.Holder.INSTANCE;
//    }
//
//    private SerialV2Log() {
//        super(Path.getSerialLogPath());
//    }
//
//    @Override
//    protected Function<byte[], ObservableSource<?>> resolve() {
//        return bytes -> Observable.just(bytes).map(ByteBuffer::wrap).map(byteBuffer -> {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("\n----------------------------------------\n");
//
//            String start = getBytesHex(byteBuffer, 2);
//            stringBuilder.append("头部标识：").append(start).append("\n");
//
//            byte msgIdB = byteBuffer.get();
//            String msgId = BytesUtils.byteToHex(msgIdB);
//            stringBuilder.append("消息ID：").append(msgId).append("\n");
//
//            String deviceAddress = getByteHex(byteBuffer);
//            stringBuilder.append("设备地址：").append(deviceAddress).append("\n");
//
//            int msgLength = getBytesInt(byteBuffer,2);
//            stringBuilder.append("消息长度：").append(msgLength).append("\n");
//
//            byte[] dataBs = new byte[msgLength];
//            byteBuffer.get(dataBs);
//            String dataH = BytesUtils.bytesToHex(dataBs);
//            stringBuilder.append("内容数据：").append(dataH).append("\n");
//
//            stringBuilder.append("------------------").append("\n");
//            switchLog(msgIdB,dataBs,stringBuilder);
//            stringBuilder.append("------------------").append("\n");
//
//            String crc = getByteHex(byteBuffer);
//            stringBuilder.append("校验码：").append(crc).append("\n");
//
//            String end = getByteHex(byteBuffer);
//            stringBuilder.append("尾部标识：").append(end).append("\n");
//
//            if(msgIdB >= (byte)0x01 && msgIdB <= (byte)0x0F){
//                e(stringBuilder.toString());
//            } else if(msgIdB >= (byte)0x81 && msgIdB <= (byte)0x8F){
//                d(stringBuilder.toString());
//            } else {
//                w(stringBuilder.toString());
//            }
//
//            return new Object();
//        });
//    }
//
//    private void switchLog(byte msgIdB, byte[] dataBs, StringBuilder stringBuilder) {
//        try {
//            ByteBuffer dataBuffer = ByteBuffer.wrap(dataBs);
//            switch (msgIdB) {
//                case (byte)0x01:
//                    stringBuilder.append("车辆信息-0x01").append("\n");
//                    r01(dataBuffer,stringBuilder);
//                    break;
//                case (byte)0x02:
//                    stringBuilder.append("线路数据-0x02").append("\n");
//                    r02(dataBuffer,stringBuilder);
//                    break;
//                case (byte)0x03:
//                    stringBuilder.append("路牌数据-0x03").append("\n");
//                    r03(dataBuffer,stringBuilder);
//                    break;
//                case (byte)0x04:
//                    stringBuilder.append("进出站消息-0x04").append("\n");
//                    r04(dataBuffer,stringBuilder);
//                    break;
//                case (byte)0x06:
//                    stringBuilder.append("时间同步-0x06").append("\n");
//                    r06(dataBuffer,stringBuilder);
//                    break;
//                case (byte)0x07:
//                    stringBuilder.append("参数查询-0x07").append("\n");
//                    r07(dataBuffer,stringBuilder);
//                    break;
//                case (byte)0x08:
//                    stringBuilder.append("星级及驾驶员-0x08").append("\n");
//                    r08(dataBuffer,stringBuilder);
//                    break;
//                case (byte)0x09:
//                    stringBuilder.append("心跳检查-0x09").append("\n");
//                    r09(dataBuffer,stringBuilder);
//                    break;
//                case (byte) 0x80://升级查询应答（平台回复）
//                    stringBuilder.append("设备通用应答-0x80").append("\n");
//                    r80(dataBuffer,stringBuilder);
//                    break;
//                case (byte) 0x81://升级查询应答（平台回复）
//                    stringBuilder.append("查询路牌-0x81").append("\n");
//                    r81(dataBuffer,stringBuilder);
//                    break;
//                case (byte) 0x82://升级查询应答（平台回复）
//                    stringBuilder.append("查询线路-0x82").append("\n");
//                    r82(dataBuffer,stringBuilder);
//                    break;
//                case (byte) 0x87://升级查询应答（平台回复）
//                    stringBuilder.append("参数查询应答-0x87").append("\n");
//                    r87(dataBuffer,stringBuilder);
//                    break;
//            }
//        } catch (Exception e){
//            stringBuilder.append("解析错误：").append(e.getMessage()).append("\n");
//        }
//    }
//
//    private void r01(ByteBuffer dataBuffer, StringBuilder stringBuilder) {
//        int carLicenseLength = getByteInt(dataBuffer);
//        stringBuilder.append("车牌号长度：").append(carLicenseLength).append("\n");
//        String carLicense = getBytesHex(dataBuffer, carLicenseLength);
//        if(carLicenseLength > 0){
//            carLicense = StringUtils.hexStringToString(carLicense);
//        }
//        stringBuilder.append("车牌号：").append(carLicense).append("\n");
//        int carNumberLength = getByteInt(dataBuffer);
//        stringBuilder.append("车辆编号长度：").append(carNumberLength).append("\n");
//        String carNumber = getBytesHex(dataBuffer, carNumberLength);
//        if(carNumberLength > 0){
//            carNumber = StringUtils.hexStringToString(carNumber);
//        }
//        stringBuilder.append("车辆编号：").append(carNumber).append("\n");
//        String terminalId = getBytesHex(dataBuffer, 6);
//        stringBuilder.append("终端号：").append(terminalId).append("\n");
//    }
//    private void r02(ByteBuffer dataBuffer, StringBuilder stringBuilder) {
//        int lineNameLength = getByteInt(dataBuffer);
//        stringBuilder.append("线路号长度：").append(lineNameLength).append("\n");
//        String lineName = getBytesHex(dataBuffer, lineNameLength);
//        if(lineNameLength > 0){
//            lineName = StringUtils.hexStringToString(lineName);
//        }
//        stringBuilder.append("线路号：").append(lineName).append("\n");
//        String direction = getByteHex(dataBuffer);
//        stringBuilder.append("运行方向：").append(direction).append("\n");
//        int siteNumber = getByteInt(dataBuffer);
//        stringBuilder.append("站点个数：").append(siteNumber).append("\n");
//        int packNumber = getByteInt(dataBuffer);
//        stringBuilder.append("分包总数：").append(packNumber).append("\n");
//        int currPack = getByteInt(dataBuffer);
//        stringBuilder.append("本包序号：").append(currPack).append("\n");
//
//        while (dataBuffer.hasRemaining()) {
//            int index = getByteInt(dataBuffer);
//            stringBuilder.append("站点序号：").append(index).append("\n");
//            int chLength = getByteInt(dataBuffer);
//            stringBuilder.append("站点中文名长度：").append(chLength).append("\n");
//            String chName = getBytesHex(dataBuffer, chLength);
//            if(chLength > 0){
//                chName = StringUtils.hexStringToString(chName);
//            }
//            stringBuilder.append("站点中文名：").append(chName).append("\n");
//            int enLength = getByteInt(dataBuffer);
//            stringBuilder.append("站点英文名长度：").append(enLength).append("\n");
//            String enName = getBytesHex(dataBuffer, enLength);
//            if(enLength > 0){
//                enName = StringUtils.hexStringToString(enName);
//            }
//            stringBuilder.append("站点英文名：").append(enName).append("\n");
//        }
//    }
//    private void r03(ByteBuffer dataBuffer, StringBuilder stringBuilder) {
//        int lineNameLength = getByteInt(dataBuffer);
//        stringBuilder.append("线路号长度：").append(lineNameLength).append("\n");
//        String lineName = getBytesHex(dataBuffer, lineNameLength);
//        if(lineNameLength > 0){
//            lineName = StringUtils.hexStringToString(lineName);
//        }
//        stringBuilder.append("线路号：").append(lineName).append("\n");
//        int startChLength = getByteInt(dataBuffer);
//        stringBuilder.append("起点站中文名长度：").append(startChLength).append("\n");
//        String startChName = getBytesHex(dataBuffer, startChLength);
//        if(startChLength > 0){
//            startChName = StringUtils.hexStringToString(startChName);
//        }
//        stringBuilder.append("起点站中文名：").append(startChName).append("\n");
//        int startEnLength = getByteInt(dataBuffer);
//        stringBuilder.append("起点站英文名长度：").append(startEnLength).append("\n");
//        String startEnName = getBytesHex(dataBuffer, startEnLength);
//        if(startEnLength > 0){
//            startEnName = StringUtils.hexStringToString(startEnName);
//        }
//        stringBuilder.append("起点站英文名：").append(startEnName).append("\n");
//        int endChLength = getByteInt(dataBuffer);
//        stringBuilder.append("终点站中文名长度：").append(endChLength).append("\n");
//        String endChName = getBytesHex(dataBuffer, endChLength);
//        if(endChLength > 0){
//            endChName = StringUtils.hexStringToString(endChName);
//        }
//        stringBuilder.append("终点站中文名：").append(endChName).append("\n");
//        int endEnLength = getByteInt(dataBuffer);
//        stringBuilder.append("终点站英文名长度：").append(endEnLength).append("\n");
//        String endEnName = getBytesHex(dataBuffer, endEnLength);
//        if(endEnLength > 0){
//            endEnName = StringUtils.hexStringToString(endEnName);
//        }
//        stringBuilder.append("终点站英文名：").append(endEnName).append("\n");
//    }
//    private void r04(ByteBuffer dataBuffer, StringBuilder stringBuilder) {
//        String direction = getByteHex(dataBuffer);
//        stringBuilder.append("方向：").append(direction).append("\n");
//        int siteIndex = getByteInt(dataBuffer);
//        stringBuilder.append("站序：").append(siteIndex).append("\n");
//        String inOut = getByteHex(dataBuffer);
//        stringBuilder.append("进出标识：").append(inOut).append("\n");
//        String showTimes = getByteHex(dataBuffer);
//        stringBuilder.append("显示次数：").append(showTimes).append("\n");
//        String scrollSpeed = getByteHex(dataBuffer);
//        stringBuilder.append("滚动速度：").append(scrollSpeed).append("\n");
//        int broadContentLength = getByteInt(dataBuffer);
//        stringBuilder.append("报站内容长度：").append(broadContentLength).append("\n");
//        String broadContent = getBytesHex(dataBuffer, broadContentLength);
//        if(broadContentLength > 0){
//            broadContent = StringUtils.hexStringToString(broadContent);
//        }
//        stringBuilder.append("报站内容：").append(broadContent).append("\n");
//    }
//    private void r06(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        String date = getBytesHex(dataBuffer, 4);
//        stringBuilder.append("日期：").append(date).append("\n");
//        String time = getBytesHex(dataBuffer, 3);
//        stringBuilder.append("时间：").append(time).append("\n");
//        int week = getByteInt(dataBuffer);
//        stringBuilder.append("星期：").append(week).append("\n");
//    }
//    private void r07(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        int paramNumber = getByteInt(dataBuffer);
//        stringBuilder.append("查询参数个数：").append(paramNumber).append("\n");
//        for (int i = 0; i < paramNumber; i++) {
//            String paramId = getByteHex(dataBuffer);
//            stringBuilder.append("参数 ").append((i + 1)).append(" ID：").append(paramId).append("\n");
//        }
//    }
//    private void r08(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        int lineNameLength = getByteInt(dataBuffer);
//        stringBuilder.append("线路号长度：").append(lineNameLength).append("\n");
//        String lineName = getBytesHex(dataBuffer, lineNameLength);
//        if(lineNameLength > 0){
//            lineName = StringUtils.hexStringToString(lineName);
//        }
//        stringBuilder.append("线路号：").append(lineName).append("\n");
//        int lineStar = getByteInt(dataBuffer);
//        stringBuilder.append("线路星级：").append(lineStar).append("\n");
//        int driverNameLength = getByteInt(dataBuffer);
//        stringBuilder.append("驾驶员姓名长度：").append(driverNameLength).append("\n");
//        String driverName = getBytesHex(dataBuffer, driverNameLength);
//        if(driverNameLength > 0){
//            driverName = StringUtils.hexStringToString(driverName);
//        }
//        stringBuilder.append("驾驶员姓名：").append(driverName).append("\n");
//        int driverWorkerLength = getByteInt(dataBuffer);
//        stringBuilder.append("驾驶员工号长度：").append(driverWorkerLength).append("\n");
//        String driverWorker = getBytesHex(dataBuffer, driverWorkerLength);
//        if(driverWorkerLength > 0){
//            driverWorker = StringUtils.hexStringToString(driverWorker);
//        }
//        stringBuilder.append("驾驶员工号：").append(driverWorker).append("\n");
//
//        String driverType = getByteHex(dataBuffer);
//        stringBuilder.append("驾驶员政治面貌：").append(driverType).append("\n");
//        int unitLength = getByteInt(dataBuffer);
//        stringBuilder.append("单位名称长度：").append(unitLength).append("\n");
//        String unit = getBytesHex(dataBuffer, unitLength);
//        if(unitLength > 0){
//            unit = StringUtils.hexStringToString(unit);
//        }
//        stringBuilder.append("单位名称：").append(unit).append("\n");
//    }
//    private void r09(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        stringBuilder.append("消息内容为空").append("\n");
//    }
//    private void r80(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        String responseAddress = getByteHex(dataBuffer);
//        stringBuilder.append("应答地址").append(responseAddress).append("\n");
//        String responseOrder = getByteHex(dataBuffer);
//        stringBuilder.append("应答指令").append(responseOrder).append("\n");
//        String responseResult = getByteHex(dataBuffer);
//        stringBuilder.append("应答结果").append(responseResult).append("\n");
//    }
//    private void r81(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        String deviceAddress = getByteHex(dataBuffer);
//        stringBuilder.append("设备地址").append(deviceAddress).append("\n");
//    }
//    private void r82(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        String deviceAddress = getByteHex(dataBuffer);
//        stringBuilder.append("设备地址").append(deviceAddress).append("\n");
//    }
//    private void r87(ByteBuffer dataBuffer, StringBuilder stringBuilder){
//        String responseAddress = getByteHex(dataBuffer);
//        stringBuilder.append("应答地址").append(responseAddress).append("\n");
//        int paramNumber = getByteInt(dataBuffer);
//        stringBuilder.append("参数个数：").append(paramNumber).append("\n");
//        for (int i = 0; i < paramNumber; i++) {
//            String paramId = getByteHex(dataBuffer);
//            stringBuilder.append("参数 ").append((i + 1)).append(" ID：").append(paramId).append("\n");
//            int length = getByteInt(dataBuffer);
//            stringBuilder.append("参数 ").append((i + 1)).append(" 长度：").append(length).append("\n");
//            String content = getBytesHex(dataBuffer,length);
//            stringBuilder.append("参数 ").append((i + 1)).append(" 内容：").append(content).append("\n");
//        }
//    }
//
//}
