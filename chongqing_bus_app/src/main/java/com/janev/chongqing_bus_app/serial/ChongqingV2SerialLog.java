package com.janev.chongqing_bus_app.serial;

import android.text.TextUtils;

import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChongqingV2SerialLog {
    private static final String TAG = "SerialLog";

    private final ExecutorService executorService;

    private static final class Holder {
        public static final ChongqingV2SerialLog INSTANCE = new ChongqingV2SerialLog();
    }

    public static ChongqingV2SerialLog getInstance(){
        return Holder.INSTANCE;
    }

    private ChongqingV2SerialLog() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void resolve(byte[] bytes){
        executorService.submit(new LogTask(bytes));
    }

    private static class LogTask implements Runnable {
        private final byte[] bytes;

        public LogTask(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void run() {
            resolveBytes(bytes);
        }

        private void resolveBytes(byte[] bytes){
            boolean isError = false;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n----------------------------------------");
            if(bytes != null && bytes.length > 0){
                try {
                    stringBuilder.append("\n");
                    stringBuilder.append("全指令：").append(BytesUtils.bytesToHex(bytes)).append("\n");
                    Deque<Byte> byteQueue = new LinkedList<>();
                    for (byte aByte : bytes) {
                        byteQueue.add(aByte);
                    }

                    String start = BytesUtils.bytesToHex(getBytes(byteQueue,2));
                    if(!TextUtils.equals("2828",start)){
                        return;
                    }
                    stringBuilder.append("头部标识：").append(start).append("\n");

                    Byte MESSAGE_ID = byteQueue.poll();
                    stringBuilder.append("消息ID：").append(BytesUtils.byteToHex(MESSAGE_ID)).append("\n");

                    String deviceAddress = BytesUtils.byteToHex(byteQueue.poll());
                    stringBuilder.append("设备地址：").append(deviceAddress).append("\n");

                    int length = BytesUtils.hex16to10(BytesUtils.bytesToHex(getBytes(byteQueue,2)));
                    stringBuilder.append("内容长度：").append(length).append("\n");

                    String end = BytesUtils.byteToHex(byteQueue.pollLast());
                    String crc = BytesUtils.byteToHex(byteQueue.pollLast());

                    stringBuilder.append("----------").append("\n");
                    switch (MESSAGE_ID) {
                        case (byte) 0x01:
                            carInfo(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x02:
                            lineData(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x03:
                            guideBoardData(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x04:
                            inOutMessage(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x05:
                            publicityWord(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x06:
                            timeSync(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x07:
                            paramsQuery(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x87:
                            paramsQueryResponse(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x08:
                            starAndDriverInfo(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x81:
                            queryGuideBoard(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x82:
                            queryLine(byteQueue,stringBuilder);
                            break;
                        case (byte) 0x80:
                            deviceCommonResponse(byteQueue,stringBuilder);
                            break;
                    }
                    stringBuilder.append("----------").append("\n");

                    stringBuilder.append("校验码：").append(crc).append("\n");
                    stringBuilder.append("尾部标识：").append(end).append("\n");
                } catch (Exception e){
                    e.printStackTrace();
                    stringBuilder.append("解析异常：").append(e.getMessage()).append("\n");
                }
            } else {
                stringBuilder.append("空指令：null\n");
            }
            stringBuilder.append("----------------------------------------");
            d(stringBuilder.toString(),isError);
        }

        private void carInfo(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x01 车辆信息]").append("\n");

            int length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("车牌号长度：").append(length).append("\n");

            String carScience = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("车牌号：").append(carScience).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("车辆编号长度：").append(length).append("\n");

            String carNumber = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("车辆编号：").append(carNumber).append("\n");

            String terminalNumber = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque,6)));
            stringBuilder.append("终端号：").append(terminalNumber).append("\n");
        }

        private void lineData(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x02 线路数据]").append("\n");

            int length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("线路号长度：").append(length).append("\n");

            String lineNumber = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("线路号：").append(lineNumber).append("\n");

            String runDirection = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("运行方向：").append(runDirection).append("\n");

            int siteNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("站点个数：").append(siteNumber).append("\n");

            int packNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("分包总数：").append(packNumber).append("\n");

            int packIndex = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("本包序号：").append(packIndex).append("\n");

            int tag = 0;
            while (!byteDeque.isEmpty()) {
                switch (tag) {
                    case 0:
                        int index = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
                        stringBuilder.append("站点序号：").append(index).append("，");
                        tag = 1;
                        break;
                    case 1:
                        length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
                        stringBuilder.append("中文名长度：").append(length).append("，");
                        tag = 2;
                        break;
                    case 2:
                        String name = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
                        stringBuilder.append("中文名：").append(name).append("，");
                        tag = 3;
                        break;
                    case 3:
                        length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
                        stringBuilder.append("英文名长度：").append(length).append("，");
                        tag = 4;
                        break;
                    case 4:
                        name = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
                        stringBuilder.append("英文名：").append(name).append("\n");
                        tag = 0;
                        break;
                }
            }
        }

        private void guideBoardData(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x03 路牌数据]").append("\n");

            int length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("线路号长度：").append(length).append("\n");

            String lineNumber = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("线路号：").append(lineNumber).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("起点站中文名称长度：").append(length).append("\n");

            String name = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("起点站中文名称：").append(name).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("起点站英文名称长度：").append(length).append("\n");

            name = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("起点站英文名称：").append(name).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("终点站中文名称长度：").append(length).append("\n");

            name = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("终点站中文名称：").append(name).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("终点站英文名称长度：").append(length).append("\n");

            name = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("终点站英文名称：").append(name).append("\n");
        }

        private void inOutMessage(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x04 进出站消息]").append("\n");

            String direction = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("方向：").append(direction).append("[").append(TextUtils.equals("01",direction) ? "下行" : "上行").append("]").append("\n");

            String siteIndex = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("站序：").append(siteIndex).append("\n");

            String inOutFlag = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("进出标识：").append(inOutFlag).append("[").append(TextUtils.equals("01",inOutFlag) ? "出站" : "进站").append("]").append("\n");

            String showTimes = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("显示次数：").append(showTimes).append("\n");

            String rollSpeed = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("滚动速度：").append(rollSpeed).append("\n");

            int length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("报站内容长度：").append(length).append("\n");

            String name = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("报站内容：").append(name).append("\n");
        }

        private void publicityWord(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x05 宣传语]").append("\n");

            String showFlag = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("显示标识：").append(showFlag).append("\n");

            int length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("宣传语个数：").append(length).append("\n");

            int tag = 0;
            while (!byteDeque.isEmpty()) {
                switch (tag) {
                    case 0:
                        Byte poll = byteDeque.poll();
                        String string;
                        if (poll == 0x01) {
                            string = "循环宣传用语，车内信息发布屏本地保存，一直循环显示该信息。";
                        } else {
                            string = "临时宣传用语，车内信息发布屏立即显示该信息，之后不再显示。不受显示顺序控制。";
                        }
                        stringBuilder.append("宣传语类型：").append(BytesUtils.byteToHex(poll)).append("[").append(string).append("]").append("\n");
                        tag = 1;
                        break;
                    case 1:
                        length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
                        stringBuilder.append("宣传语长度：").append(length).append("\n");
                        tag = 2;
                        break;
                    case 2:
                        String content = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
                        stringBuilder.append("宣传语内容：").append(content).append("\n");
                        tag = 0;
                        break;
                }
            }
        }

        private void timeSync(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x06 时间同步]").append("\n");

            String date = BytesUtils.bytesToHex(getBytes(byteDeque, 4));
            stringBuilder.append("日期：").append(date).append("\n");

            String time = BytesUtils.bytesToHex(getBytes(byteDeque, 3));
            stringBuilder.append("时间：").append(time).append("\n");

            String week = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("星期：").append(week).append("\n");
        }

        private void paramsQuery(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x07 参数查询]").append("\n");

            int queryParamsNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("查询参数个数：").append(queryParamsNumber).append("(0 表示查询所有参数，不带后面的参数ID)").append("\n");

            if(queryParamsNumber == 0){
                stringBuilder.append("查询所有参数").append("\n");
            } else {
                while (!byteDeque.isEmpty()) {
                    Byte poll = byteDeque.poll();
                    String paramName = getParamName(poll);
                    stringBuilder.append(paramName).append("\n");
                }
            }
        }

        private void paramsQueryResponse(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x87 参数查询]").append("\n");

            int paramsNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("参数个数：").append(paramsNumber).append("(0 表示查询所有参数，不带后面的参数ID)").append("\n");

            if(paramsNumber == 0){
                stringBuilder.append("查询所有参数").append("\n");
            } else {
                int tag = 0,length = 0;
                while (!byteDeque.isEmpty()) {
                    switch (tag) {
                        case 0:
                            String paramName = getParamName(byteDeque.poll());
                            stringBuilder.append(paramName).append("：");
                            tag = 1;
                            break;
                        case 1:
                            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
                            tag = 2;
                            break;
                        case 2:
                            String paramContent = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
                            stringBuilder.append(paramContent).append("\n");
                            tag = 0;
                            break;
                    }
                }
            }
        }

        private void starAndDriverInfo(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x08 参数查询]").append("\n");

            int length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("线路号长度：").append(length).append("\n");

            String lineNumber = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("线路号：").append(lineNumber).append("\n");

            String lineStar = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("线路星级：").append(lineStar).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("驾驶员姓名长度：").append(length).append("\n");

            String driverName = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("驾驶员姓名：").append(driverName).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("驾驶员工号长度：").append(length).append("\n");

            String driverWorkerId = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("驾驶员工号：").append(driverWorkerId).append("\n");

            String govType = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("驾驶员政治面貌：").append(govType).append("\n");

            length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteDeque.poll()));
            stringBuilder.append("单位名称长度：").append(length).append("\n");

            String unitName = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteDeque, length)));
            stringBuilder.append("单位名称：").append(unitName).append("\n");
        }

        private void queryGuideBoard(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x81 查询路牌]").append("\n");

            String deviceAddress = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("设备地址：").append(deviceAddress).append("\n");
        }

        private void queryLine(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x82 查询线路]").append("\n");

            String deviceAddress = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("设备地址：").append(deviceAddress).append("\n");
        }

        private void deviceCommonResponse(Deque<Byte> byteDeque,StringBuilder stringBuilder){
            stringBuilder.append("[0x80 设备通用应答]").append("\n");

            String deviceAddress = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("应答地址：").append(deviceAddress).append("\n");

            String responseOrder = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("应答指令：").append(responseOrder).append("\n");

            String responseResult = BytesUtils.byteToHex(byteDeque.poll());
            stringBuilder.append("应答结果：").append(responseResult).append("\n");
        }

        private String getParamName(Byte b){
            switch (b.intValue()) {
                case 0x01:
                    return "硬件序列号";
                case 0x02:
                    return "硬件版本号";
                case 0x03:
                    return "固件版本号";
                case 0x04:
                    return "应用版本号";
                case 0x05:
                    return "设备出厂日期";
                case 0x06:
                    return "设备自编号";
                case 0x07:
                    return "厂商编码";
                case 0x08:
                    return "设备地址";
                case 0x09:
                    return "LED屏文字滚动速度";
                case 0x10:
                    return "LCD屏幕参数";
                case 0x0B:
                    return "屏幕显示亮度";
                case 0x0C:
                    return "播放声音";
                case 0x41:
                    return "当前车辆自编号";
                case 0x42:
                    return "当前车辆车牌号";
                case 0x43:
                    return "心跳间隔";
                case 0x44:
                    return "消息重发次数";
                case 0x45:
                    return "主服务器地址";
                case 0x46:
                    return "主服务器端口";
                case 0x47:
                    return "备用服务器地址";
                case 0x48:
                    return "备用服务器端口";
                default:
                    if(b.intValue() >= 0x0D && b.intValue() <= 0x1F){
                        return "预留参数：" + BytesUtils.byteToHex(b);
                    } else if(b.intValue() >= 0x20 && b.intValue() <= 0x40){
                        return "自定义参数：" + BytesUtils.byteToHex(b);
                    } else if(b.intValue() >= 0x49 && b.intValue() <= 0x60){
                        return "预留参数：" + BytesUtils.byteToHex(b);
                    } else {
                        return "未知参数：" + BytesUtils.byteToHex(b);
                    }
            }
        }

        private byte[] getBytes(Deque<Byte> byteDeque,int length){
            byte[] data = new byte[length];
            for (int i = 0; i < data.length; i++) {
                data[i] = byteDeque.poll();
            }
            return data;
        }

        private void d(String log,boolean isError){
            if(isError){
                L.serialE("SerialLog",log);
            } else {
                L.serialD("SerialLog",log);
            }
        }
    }
}
