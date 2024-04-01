//package com.janev.chongqing_bus_app.serial;
//
//import com.janev.chongqing_bus_app.system.Path;
//import com.janev.chongqing_bus_app.tcp.DataLog;
//import com.janev.chongqing_bus_app.utils.BytesUtils;
//
//import java.nio.ByteBuffer;
//
//import io.reactivex.Observable;
//import io.reactivex.ObservableSource;
//import io.reactivex.functions.Function;
//
//public class SerialV1Log extends DataLog {
//
//    private static final class Holder {
//        public static final SerialV1Log INSTANCE = new SerialV1Log();
//    }
//
//    public static SerialV1Log getInstance(){
//        return SerialV1Log.Holder.INSTANCE;
//    }
//
//    private SerialV1Log() {
//        super(Path.getSerialLogPath());
//    }
//
//    @Override
//    protected Function<byte[], ObservableSource<?>> resolve() {
//        return bytes -> Observable.just(bytes).map(ByteBuffer::wrap).map(byteBuffer -> {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("\n----------------------------------------\n");
//
//            String start = getByteHex(byteBuffer);
//            stringBuilder.append("帧头：").append(start).append("\n");
//
//            String direction = getByteHex(byteBuffer);
//            stringBuilder.append("信息方向：").append(direction).append("\n");
//
//            String deviceNumber = getByteHex(byteBuffer);
//            stringBuilder.append("设备编号：").append(deviceNumber).append("\n");
//
//            byte orderB = byteBuffer.get();
//            String order = BytesUtils.byteToHex(orderB);
//            stringBuilder.append("命令：").append(order).append("\n");
//
//            byte[] dataBs = new byte[byteBuffer.capacity() - 6];
//            byteBuffer.get(dataBs);
//            String dataH = BytesUtils.bytesToHex(dataBs);
//            stringBuilder.append("信息域：").append(dataH).append("\n");
//
//            stringBuilder.append("------------------").append("\n");
//            switchLog(orderB,dataBs,stringBuilder);
//            stringBuilder.append("------------------").append("\n");
//
//            String crc = getByteHex(byteBuffer);
//            stringBuilder.append("校验和：").append(crc).append("\n");
//
//            String end = getByteHex(byteBuffer);
//            stringBuilder.append("帧尾：").append(end).append("\n");
//
//            return new Object();
//        });
//    }
//
//
//    private void switchLog(byte msgIdB, byte[] dataBs, StringBuilder stringBuilder) {
//        try {
//            switch (msgIdB) {
//                case (byte)0x10:
//                    stringBuilder.append("数据指令 下发数据命令 0x10").append("\n");
//                    break;
//                case (byte)0x30:
//                    stringBuilder.append("站名下发指令 上行 0x30").append("\n");
//                    break;
//                case (byte)0x31:
//                    stringBuilder.append("站名下发指令 下行 0x31").append("\n");
//                    break;
//                case (byte)0x40:
//                    stringBuilder.append("宣传语指令 下发数据命令 0x40").append("\n");
//                    break;
//                case (byte)0x11:
//                    stringBuilder.append("查询指令 下发数据命令 0x11").append("\n");
//                    break;
//                case (byte)0x50:
//                    stringBuilder.append("时间同步 0x50").append("\n");
//                    break;
//                case (byte)0x12:
//                    stringBuilder.append("接收命令应答 0x12").append("\n");
//                    break;
//                case (byte)0x13:
//                    stringBuilder.append("LED 屏状态返回 0x13").append("\n");
//                    break;
//                case (byte) 0x60:
//                    stringBuilder.append("车辆信息 下发数据命令 0x60").append("\n");
//                    break;
//                case (byte) 0x70:
//                    stringBuilder.append("线路星级+驾驶员信息 下发数据命令 0x70").append("\n");
//                    break;
//            }
//        } catch (Exception e){
//            stringBuilder.append("解析错误：").append(e.getMessage()).append("\n");
//        }
//    }
//
//}
