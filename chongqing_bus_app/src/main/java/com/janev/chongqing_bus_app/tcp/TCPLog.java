package com.janev.chongqing_bus_app.tcp;

import android.util.Log;

import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.nio.ByteBuffer;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public class TCPLog extends DataLog{

    private static final class Holder {
        public static final TCPLog INSTANCE = new TCPLog();
    }

    public static TCPLog getInstance(){
        return Holder.INSTANCE;
    }

    private TCPLog() {
        super(Path.getAppLogPath());
    }

    protected Function<byte[], ObservableSource<?>> resolve(){
        return bytes -> Observable.just(bytes).map(ByteBuffer::wrap).map(byteBuffer -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n----------------------------------------\n");
            stringBuilder.append("指令：").append(BytesUtils.bytesToHex(byteBuffer.array())).append("\n");

            String startH = getBytesHex(byteBuffer,2);
            stringBuilder.append("头部标识：").append(startH).append("\n");

            byte msgIdB = byteBuffer.get();
            String msgIdH = BytesUtils.byteToHex(msgIdB);
            stringBuilder.append("消息ID：").append(msgIdH).append("\n");

            String deviceAddress = getByteHex(byteBuffer);
            stringBuilder.append("设备地址：").append(deviceAddress).append("\n");

            String deiceNumH = getBytesHex(byteBuffer,6);
            stringBuilder.append("设备编号：").append(deiceNumH).append("\n");

            String terminalNumH = getBytesHex(byteBuffer,6);
            stringBuilder.append("终端编号：").append(terminalNumH).append("\n");

            String messageSerialH = getBytesHex(byteBuffer,2);
            stringBuilder.append("消息流水号：").append(messageSerialH).append("\n");

            int length = getBytesInt(byteBuffer,2);
            stringBuilder.append("内容长度：").append(length).append("\n");

            byte[] dataBs = new byte[length];
            byteBuffer.get(dataBs);
            String dataH = BytesUtils.bytesToHex(dataBs);
            stringBuilder.append("内容数据：").append(dataH).append("\n");

            stringBuilder.append("------------------").append("\n");
            switchLog(msgIdB,dataBs,stringBuilder);
            stringBuilder.append("------------------").append("\n");

            String crcH = getByteHex(byteBuffer);
            stringBuilder.append("内容校验：").append(crcH).append("\n");

            String endH = getByteHex(byteBuffer);
            stringBuilder.append("尾部标识：").append(endH).append("\n");

            if(msgIdB >= (byte)0x01 && msgIdB <= (byte)0x0F){
                d(stringBuilder.toString());
            } else if(msgIdB >= (byte)0x81 && msgIdB <= (byte)0x8F){
                e(stringBuilder.toString());
            } else {
                w(stringBuilder.toString());
            }
            return new Object();
        });
    }

    private void switchLog(byte msgId,byte[] bytes,StringBuilder stringBuilder){
        try {
            ByteBuffer dataBuffer = ByteBuffer.wrap(bytes);
            switch (msgId) {
                case (byte)0x01:
                    stringBuilder.append("设备通用应答-0x01").append("\n");
                    r01(dataBuffer,stringBuilder);
                    break;
                case (byte)0x02:
                    stringBuilder.append("设备登录-0x02").append("\n");
                    r02(dataBuffer,stringBuilder);
                    break;
                case (byte)0x03:
                    stringBuilder.append("设备心跳-0x03").append("\n");
                    r03(dataBuffer,stringBuilder);
                    break;
                case (byte)0x04:
                    stringBuilder.append("参数上报-0x04").append("\n");
                    r04(dataBuffer,stringBuilder);
                    break;
                case (byte)0x06:
                    stringBuilder.append("升级状态通知-0x06").append("\n");
                    r06(dataBuffer,stringBuilder);
                    break;
                case (byte)0x07:
                    stringBuilder.append("主动升级查询-0x07").append("\n");
                    r07(dataBuffer,stringBuilder);
                    break;
                case (byte)0x08:
                    stringBuilder.append("广告信息上报-0x08").append("\n");
                    r08(dataBuffer,stringBuilder);
                    break;
                case (byte)0x09:
                    stringBuilder.append("拍照截图应答-0x09").append("\n");
                    r09(dataBuffer,stringBuilder);
                    break;
                case (byte)0x0A:
                    stringBuilder.append("文件上传应答-0x0A").append("\n");
                    r0A(dataBuffer,stringBuilder);
                    break;
                case (byte)0x0B:
                    stringBuilder.append("异常上报-0x0B").append("\n");
                    r0B(dataBuffer,stringBuilder);
                    break;
                case (byte) 0x81://平台回复
                    stringBuilder.append("平台通用应答-0x81").append("\n");
                    r81(dataBuffer,stringBuilder);
                    break;
                case (byte)0x84://参数查询（平台命令）
                    stringBuilder.append("参数查询-0x84").append("\n");
                    r84(dataBuffer,stringBuilder);
                    break;
                case (byte)0x85://参数设置（平台命令）
                    stringBuilder.append("参数设置-0x85").append("\n");
                    r85(dataBuffer,stringBuilder);
                    break;
                case (byte)0x86://平台下发设备升级指令（平台命令）
                    stringBuilder.append("设备升级-0x86").append("\n");
                    r86(dataBuffer,stringBuilder);
                    break;
                case (byte) 0x87://升级查询应答（平台回复）
                    stringBuilder.append("升级查询应答-0x87").append("\n");
                    r87(dataBuffer,stringBuilder);
                    break;
                case (byte)0x88://广告信息查询（平台命令）
                    stringBuilder.append("广告信息查询-0x88").append("\n");
                    r88(dataBuffer,stringBuilder);
                    break;
                case (byte)0x89://拍照截图（平台命令）
                    stringBuilder.append("拍照截图-0x89").append("\n");
                    r89(dataBuffer,stringBuilder);
                    break;
                case (byte)0x8A://文件上传（平台命令）
                    stringBuilder.append("文件上传-0x8A").append("\n");
                    r8A(dataBuffer,stringBuilder);
                    break;
                default:
                    stringBuilder.append("未知").append("\n");
                    break;
            }
        } catch (Exception e) {
            stringBuilder.append("解析错误：").append(e.getMessage()).append("\n");
        }
    }

    private void r01(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseOrder = getByteHex(byteBuffer);
        stringBuilder.append("应答指令：").append(responseOrder).append("\n");

        String responseSerial = getBytesHex(byteBuffer,2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");

        String responseResult = getByteHex(byteBuffer);
        stringBuilder.append("应答结果：").append(responseResult).append("\n");
    }
    private void r02(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        for (int i = 0; i < 5; i++) {
            String type = "";
            switch (i) {
                case 0:
                    type = "硬件序列号";
                    break;
                case 1:
                    type = "硬件版本号";
                    break;
                case 2:
                    type = "固件版本号";
                    break;
                case 3:
                    type = "应用版本号";
                    break;
                case 4:
                    type = "ICCID";
                    break;
            }

            int hwSerialLength = getByteInt(byteBuffer);
            stringBuilder.append(type + "长度：").append(hwSerialLength).append("\n");

            String content = getBytesHex(byteBuffer,hwSerialLength);
            stringBuilder.append(type + "：").append(content).append("\n");
        }

        String productNumber = getBytesHex(byteBuffer,5);
        stringBuilder.append("厂商编码：").append(productNumber).append("\n");

        String productAuthNumber = getBytesHex(byteBuffer,16);
        stringBuilder.append("厂商授权码：").append(productAuthNumber).append("\n");
    }
    private void r03(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        int lineNameLength = getByteInt(byteBuffer);
        stringBuilder.append("线路号长度：").append(lineNameLength).append("\n");

        String lineName = getBytesHex(byteBuffer,lineNameLength);
        stringBuilder.append("线路号：").append(lineName).append("\n");

        String resourceId = getBytesHex(byteBuffer,8);
        stringBuilder.append("广告资源 ID：").append(resourceId).append("\n");

        int resourceVersionLength = getByteInt(byteBuffer);
        stringBuilder.append("广告资源版本号长度：").append(resourceVersionLength).append("\n");

        String resourceVersion = getBytesHex(byteBuffer,resourceVersionLength);
        stringBuilder.append("广告资源版本号长度：").append(resourceVersion).append("\n");

        String programId = getBytesHex(byteBuffer,8);
        stringBuilder.append("节目单 ID：").append(programId).append("\n");

        String materialId = getBytesHex(byteBuffer,8);
        stringBuilder.append("素材 ID：").append(materialId).append("\n");
    }
    private void r04(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseSerial = getBytesHex(byteBuffer,2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");

        int paramNumber = getByteInt(byteBuffer);
        stringBuilder.append("参数个数：").append(paramNumber).append("\n");

        for (int i = 0; i < paramNumber; i++) {
            String paramId = getByteHex(byteBuffer);
            stringBuilder.append("参数ID：").append(paramId).append("\n");

            int paramLength = getByteInt(byteBuffer);
            stringBuilder.append("参数长度：").append(paramLength).append("\n");

            String paramContent = getBytesHex(byteBuffer,paramLength);
            stringBuilder.append("参数内容：").append(paramContent).append("\n");
        }
    }
    private void r06(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseSerial = getBytesHex(byteBuffer,2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");

        String upgradeType = getByteHex(byteBuffer);
        stringBuilder.append("升级类型：").append(upgradeType).append("\n");

        String resourceId = getBytesHex(byteBuffer,8);
        stringBuilder.append("资源 ID：").append(resourceId).append("\n");

        String upgradeProgress = getByteHex(byteBuffer);
        stringBuilder.append("升级进度：").append(upgradeProgress).append("\n");

        int fileNameLength = getByteInt(byteBuffer);
        stringBuilder.append("当前文件名长度：").append(fileNameLength).append("\n");

        String fileName = getBytesHex(byteBuffer,fileNameLength);
        stringBuilder.append("当前文件名：").append(fileName).append("\n");

        int downloadProgress = getByteInt(byteBuffer);
        stringBuilder.append("下载进度：").append(downloadProgress).append("\n");
    }
    private void r07(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String queryType = getByteHex(byteBuffer);
        stringBuilder.append("查询类型：").append(queryType).append("\n");

        String resourceId = getBytesHex(byteBuffer,8);
        stringBuilder.append("资源 ID：").append(resourceId).append("\n");

        int versionLength = getByteInt(byteBuffer);
        stringBuilder.append("当前版本号长度：").append(versionLength).append("\n");

        String version = getBytesHex(byteBuffer,versionLength);
        stringBuilder.append("当前版本号：").append(version).append("\n");
    }
    private void r08(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseSerial = getBytesHex(byteBuffer,2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");

        String resourceId = getBytesHex(byteBuffer,8);
        stringBuilder.append("广告资源文件 ID：").append(resourceId).append("\n");

        int versionLength = getByteInt(byteBuffer);
        stringBuilder.append("广告资源版本号长度：").append(versionLength).append("\n");

        String version = getBytesHex(byteBuffer,versionLength);
        stringBuilder.append("广告资源版本号：").append(version).append("\n");

        int programNumber = getByteInt(byteBuffer);
        stringBuilder.append("节目单个数：").append(programNumber).append("\n");

        for (int i = 0; i < programNumber; i++) {
            int pId = (i + 1);

            String programId = getBytesHex(byteBuffer,8);
            stringBuilder.append("节目单 ").append(pId).append(" ID：").append(programId).append("\n");

            int materialNumber = getByteInt(byteBuffer);
            for (int i1 = 0; i1 < materialNumber; i1++) {

                String materialId = getBytesHex(byteBuffer,8);
                stringBuilder.append("节目单 ").append(pId).append(" 素材 ").append( i1).append(" ID：").append(materialId).append("\n");

                String materialType = getByteHex(byteBuffer);
                stringBuilder.append("节目单 ").append(pId).append(" 素材 ").append(i1).append(" 类型：").append(materialType).append("\n");

                String playTotal = getBytesHex(byteBuffer,2);
                stringBuilder.append("节目单 ").append(pId).append(" 素材 ").append(i1).append(" 播放次数：").append(playTotal).append("\n");
            }
        }
    }
    private void r09(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseSerial = getBytesHex(byteBuffer,2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");

        int fileIdLength = getByteInt(byteBuffer);
        stringBuilder.append("文件 ID 长度：").append(fileIdLength).append("\n");

        String fileId = getBytesHex(byteBuffer,fileIdLength);
        stringBuilder.append("文件 ID：").append(fileId).append("\n");

        String excType = getByteHex(byteBuffer);
        stringBuilder.append("执行方式：").append(excType).append("\n");

        String excResult = getByteHex(byteBuffer);
        stringBuilder.append("执行结果：").append(excResult).append("\n");

        int fileNameLength = getByteInt(byteBuffer);
        stringBuilder.append("文件名长度：").append(fileNameLength).append("\n");

        String fileName = getBytesHex(byteBuffer,fileNameLength);
        stringBuilder.append("文件名：").append(fileName).append("\n");
    }
    private void r0A(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseSerial = getBytesHex(byteBuffer,2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");

        String fileType = getByteHex(byteBuffer);
        stringBuilder.append("文件类型：").append(fileType).append("\n");

        int fileIdLength = getByteInt(byteBuffer);
        stringBuilder.append("文件ID长度：").append(fileIdLength).append("\n");

        String fileId = getBytesHex(byteBuffer,fileIdLength);
        stringBuilder.append("文件ID：").append(fileId).append("\n");

        String excResult = getByteHex(byteBuffer);
        stringBuilder.append("执行结果：").append(excResult).append("\n");

        int fileNameLength = getByteInt(byteBuffer);
        stringBuilder.append("文件名长度：").append(fileNameLength).append("\n");

        String fileName = getBytesHex(byteBuffer, fileNameLength);
        stringBuilder.append("文件名：").append(fileName).append("\n");
    }
    private void r0B(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String errType = getBytesHex(byteBuffer, 2);
        stringBuilder.append("异常类型：").append(errType).append("\n");

        String errState = getByteHex(byteBuffer);
        stringBuilder.append("异常状态：").append(errState).append("\n");

        int errMsgLength = getByteInt(byteBuffer);
        stringBuilder.append("异常信息长度：").append(errMsgLength).append("\n");

        String errMsg = getBytesHex(byteBuffer, errMsgLength);
        stringBuilder.append("异常信息内容：").append(errMsg).append("\n");
    }

    private void r81(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseOrder = getByteHex(byteBuffer);
        stringBuilder.append("应答指令：").append(responseOrder).append("\n");
        String responseSerial = getBytesHex(byteBuffer, 2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");
        String responseResult = getByteHex(byteBuffer);
        stringBuilder.append("应答结果：").append(responseResult).append("\n");
    }
    private void r84(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        int paramsNumber = getByteInt(byteBuffer);
        stringBuilder.append("查询参数个数：").append(paramsNumber).append("\n");
        for (int i = 0; i < paramsNumber; i++) {
            String byteHex = getByteHex(byteBuffer);
            stringBuilder.append("参数 ").append((i + 1)).append(" ID: ").append(byteHex).append("\n");
        }
    }
    private void r85(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        int paramsNumber = getByteInt(byteBuffer);
        stringBuilder.append("参数个数：").append(paramsNumber).append("\n");
        for (int i = 0; i < paramsNumber; i++) {
            String paramId = getByteHex(byteBuffer);
            stringBuilder.append("参数 ").append((i + 1)).append(" ID: ").append(paramId).append("\n");
            int paramLength = getByteInt(byteBuffer);
            stringBuilder.append("参数 ").append((i + 1)).append(" 长度: ").append(paramLength).append("\n");
            String param = getBytesHex(byteBuffer, paramLength);
            stringBuilder.append("参数 ").append((i + 1)).append(" 内容: ").append(param).append("\n");
        }
    }
    private void r86(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String upgradeType = getByteHex(byteBuffer);
        stringBuilder.append("升级类型：").append(upgradeType).append("\n");
        String upgradeFlag = getByteHex(byteBuffer);
        stringBuilder.append("升级标识：").append(upgradeFlag).append("\n");
        String resourceId = getBytesHex(byteBuffer, 8);
        stringBuilder.append("资源ID：").append(resourceId).append("\n");
        int versionLength = getByteInt(byteBuffer);
        stringBuilder.append("版本号长度：").append(versionLength).append("\n");
        String version = getBytesHex(byteBuffer, versionLength);
        stringBuilder.append("版本号：").append(version).append("\n");
        int ftpAddressLength = getByteInt(byteBuffer);
        stringBuilder.append("文件地址长度：").append(ftpAddressLength).append("\n");
        String ftpAddress = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpAddressLength));
        stringBuilder.append("文件地址：").append(ftpAddress).append("\n");
        int ftpUserLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP用户名长度：").append(ftpUserLength).append("\n");
        String ftpUser = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpUserLength));
        stringBuilder.append("FTP用户名：").append(ftpUser).append("\n");
        int ftpPasswordLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP密码长度：").append(ftpPasswordLength).append("\n");
        String ftpPassword = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpPasswordLength));
        stringBuilder.append("FTP密码：").append(ftpPassword).append("\n");
    }
    private void r87(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String responseSerial = getBytesHex(byteBuffer, 2);
        stringBuilder.append("应答流水号：").append(responseSerial).append("\n");
        String queryType = getByteHex(byteBuffer);
        stringBuilder.append("查询类型：").append(queryType).append("\n");
        String resourceId = getBytesHex(byteBuffer, 8);
        stringBuilder.append("资源ID：").append(resourceId).append("\n");
        int versionLength = getByteInt(byteBuffer);
        stringBuilder.append("最新版本号长度：").append(versionLength).append("\n");
        String version = getBytesHex(byteBuffer, versionLength);
        stringBuilder.append("最新版本号：").append(version).append("\n");
        int ftpAddressLength = getByteInt(byteBuffer);
        stringBuilder.append("文件地址长度：").append(ftpAddressLength).append("\n");
        String ftpAddress = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpAddressLength));
        stringBuilder.append("文件地址：").append(ftpAddress).append("\n");
        int ftpUserLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP用户名长度：").append(ftpUserLength).append("\n");
        String ftpUser = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpUserLength));
        stringBuilder.append("FTP用户名：").append(ftpUser).append("\n");
        int ftpPasswordLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP密码长度：").append(ftpPasswordLength).append("\n");
        String ftpPassword = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpPasswordLength));
        stringBuilder.append("FTP密码：").append(ftpPassword).append("\n");
    }
    private void r88(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        stringBuilder.append("消息内容为空").append("\n");
    }
    private void r89(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String queryType = getByteHex(byteBuffer);
        stringBuilder.append("截图方式：").append(queryType).append("\n");
        int fileIDLength = getByteInt(byteBuffer);
        stringBuilder.append("文件ID长度：").append(fileIDLength).append("\n");
        String fileID = StringUtils.hexStringToString(getBytesHex(byteBuffer, fileIDLength));
        stringBuilder.append("文件ID：").append(fileID).append("\n");
        int ftpAddressLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP地址长度：").append(ftpAddressLength).append("\n");
        String ftpAddress = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpAddressLength));
        stringBuilder.append("FTP地址：").append(ftpAddress).append("\n");
        int ftpUserLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP用户名长度：").append(ftpUserLength).append("\n");
        String ftpUser = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpUserLength));
        stringBuilder.append("FTP用户名：").append(ftpUser).append("\n");
        int ftpPasswordLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP密码长度：").append(ftpPasswordLength).append("\n");
        String ftpPassword = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpPasswordLength));
        stringBuilder.append("FTP密码：").append(ftpPassword).append("\n");
    }
    private void r8A(ByteBuffer byteBuffer,StringBuilder stringBuilder){
        String fileType = getByteHex(byteBuffer);
        stringBuilder.append("文件类型：").append(fileType).append("\n");
        String startTime = StringUtils.hexStringToString(getBytesHex(byteBuffer, 7));
        stringBuilder.append("开始时间：").append(startTime).append("\n");
        String endTime = StringUtils.hexStringToString(getBytesHex(byteBuffer, 7));
        stringBuilder.append("结束时间：").append(endTime).append("\n");
        int fileIDLength = getByteInt(byteBuffer);
        stringBuilder.append("文件ID长度：").append(fileIDLength).append("\n");
        String fileID = StringUtils.hexStringToString(getBytesHex(byteBuffer, fileIDLength));
        stringBuilder.append("文件ID：").append(fileID).append("\n");
        int ftpAddressLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP地址长度：").append(ftpAddressLength).append("\n");
        String ftpAddress = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpAddressLength));
        stringBuilder.append("FTP地址：").append(ftpAddress).append("\n");
        int ftpUserLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP用户名长度：").append(ftpUserLength).append("\n");
        String ftpUser = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpUserLength));
        stringBuilder.append("FTP用户名：").append(ftpUser).append("\n");
        int ftpPasswordLength = getByteInt(byteBuffer);
        stringBuilder.append("FTP密码长度：").append(ftpPasswordLength).append("\n");
        String ftpPassword = StringUtils.hexStringToString(getBytesHex(byteBuffer, ftpPasswordLength));
        stringBuilder.append("FTP密码：").append(ftpPassword).append("\n");
    }


}
