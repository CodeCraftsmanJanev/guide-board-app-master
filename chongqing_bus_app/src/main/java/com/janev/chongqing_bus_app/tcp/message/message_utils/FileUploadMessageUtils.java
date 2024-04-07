package com.janev.chongqing_bus_app.tcp.message.message_utils;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.Program;
import com.janev.chongqing_bus_app.db.Site;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.tcp.message.FileUploadRequest;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;
import com.janev.chongqing_bus_app.tcp.message.ReplyRequest;
import com.janev.chongqing_bus_app.tcp.task.resource.ResourceManager2;
import com.janev.chongqing_bus_app.tcp.task.uploader.FtpUploader;
import com.janev.chongqing_bus_app.tcp.task.uploader.OnUploadListener;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class FileUploadMessageUtils {

    public static void upload(byte order, String msgSerial, ByteBuffer byteBuffer){
        new ReplyRequest(BytesUtils.byteToHex(order),msgSerial,ReplyRequest.SUCCESS).send();

        byte fileType = byteBuffer.get();
        Log.d(TAG, "文件类型：" + fileType);

        String startTimeH = MessageUtils.getBytesHex(byteBuffer, 7);
        Log.d(TAG, "开始时间：" + startTimeH);

        String endTimeH = MessageUtils.getBytesHex(byteBuffer, 7);
        Log.d(TAG, "结束时间：" + endTimeH);

        int fileIdLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "文件ID长度：" + fileIdLength);

        String fileIdHex = MessageUtils.getBytesHex(byteBuffer, fileIdLength);
        String fileId = fileIdLength == 0 ? "" : StringUtils.hexStringToString(fileIdHex);
        Log.d(TAG, "文件ID：" + fileId);

        int ftpAddressLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP地址长度：" + ftpAddressLength);

        String ftpAddress = MessageUtils.getBytesHex(byteBuffer, ftpAddressLength);
        if(ftpAddressLength > 0){
            ftpAddress = StringUtils.hexStringToString(ftpAddress);
        }
        Log.d(TAG, "FTP地址：" + ftpAddress);

        int ftpUserLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP用户名长度：" + ftpUserLength);

        String ftpUser = MessageUtils.getBytesHex(byteBuffer, ftpUserLength);
        if(ftpUserLength > 0){
            ftpUser = StringUtils.hexStringToString(ftpUser);
        }
        Log.d(TAG, "FTP用户名：" + ftpUser);

        int ftpPasswordLength = MessageUtils.getByteInt(byteBuffer);
        Log.d(TAG, "FTP密码长度：" + ftpPasswordLength);

        String ftpPassword = MessageUtils.getBytesHex(byteBuffer, ftpPasswordLength);
        if(ftpPasswordLength > 0){
            ftpPassword = StringUtils.hexStringToString(ftpPassword);
        }
        Log.d(TAG, "FTP密码：" + ftpPassword);

        File file;
        switch (fileType) {
            case (byte) 0x00://参数配置文件
                Log.d(TAG, "参数配置文件");
                file = getParamsFile(fileId);
                break;
            case (byte) 0x01://应用软件日志文件
                Log.d(TAG, "应用软件日志文件");
                file = getLogoFile(fileId,startTimeH,endTimeH,Path.getLogPath());
                break;
            case (byte) 0x02://串口通讯日志文件
                Log.d(TAG, "串口通讯日志文件");
                file = getLogoFile(fileId,startTimeH,endTimeH,Path.getSerialLogPath());
                break;
            case (byte) 0x03://广告资源配置文件
                Log.d(TAG, "广告资源配置文件");
                file = getResourceFile(fileId);
                break;
            case (byte) 0x04://播放统计文件
                Log.d(TAG, "播放统计文件");
                file = getPlayTotalFile(fileId);
                break;
            case (byte) 0x05://线路文件
                Log.d(TAG, "线路文件");
                file = getLineDataFile(fileId);
                break;
            default:
                file = null;
                break;
        }

        if(file != null){
            Log.d(TAG, "上传文件：" + file.getPath());
            Disposable subscribe = new FtpUploader(file, ftpAddress, ftpUser, ftpPassword)
                    .setListener(new OnUploadListener() {
                        @Override
                        public void onStart() {
                            Log.d(TAG, "onStart: ");
                        }

                        @Override
                        public void onProgress(int percent) {
                            Log.d(TAG, "onProgress: " + percent);
                        }

                        @Override
                        public void onComplete(Boolean b, boolean isDownload) {
                            Log.d(TAG, "onComplete: " + b);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e(TAG, "onError: ", throwable);
                        }
                    })
                    .upload()
                    .subscribeOn(Schedulers.io())
                    .doOnTerminate(new Action() {
                        @Override
                        public void run() throws Exception {
                            FileUtils.delete(file);
                        }
                    })
                    .subscribe(aBoolean -> {
                        Log.d(TAG, "accept: 上传结果：" + aBoolean);
                        new FileUploadRequest(msgSerial, fileType, fileId, file.getName(), aBoolean ? "00" : "01").send();
                    }, throwable -> {
                        Log.e(TAG, "报错，上传失败", throwable);
                        new FileUploadRequest(msgSerial,fileType,fileId,"","01").send();
                    });
        } else {
            Log.e(TAG, "没有文件，上传失败");
            new FileUploadRequest(msgSerial,fileType,fileId,"","01").send();
        }
    }

    private static File getParamsFile(String fileID){
        JSONArray jsonArray = new JSONArray();
        Field[] fields = Cache.Key.class.getFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = fields[i];
                //变量名
                String keyFieldName = field.getName();
                //变量值
                String key = (String) field.get(keyFieldName);

                if(!canContinue(key)){
                    continue;
                }
                //对应名称
                int paramId = getParamId(key);
                String name = getParamName(key);
                Object value = getParamValue(key, keyFieldName);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id",paramId);
                jsonObject.put("name",name);
                jsonObject.put("value",value.toString());

                jsonArray.add(jsonObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        File uploadFile = new File(Path.getTempDir(), fileID + ".txt");
        boolean b = FileIOUtils.writeFileFromString(uploadFile,jsonArray.toJSONString(),false);
        if(b){
            return uploadFile;
        }
        return null;
    }

    private static File getLogoFile(String fileID,String startTime,String endTime,String dir){
        List<File> files = FileUtils.listFilesInDirWithFilter(dir, file -> {
            Log.d(TAG, "startTime: " + startTime);
            Date startDate = TimeUtils.string2Date(startTime, "yyyyMMddHHmmss");
            Log.d(TAG, "endTime: " + endTime);
            Date endDate = TimeUtils.string2Date(endTime,"yyyyMMddHHmmss");
            Date fileDate = new Date(FileUtils.getFileLastModified(file));
            return fileDate.after(startDate) && fileDate.before(endDate);
        });

        if(!files.isEmpty()){
            File file = new File(Path.getTempDir(),fileID + ".zip");
            if (zipFiles(files,file)) {
                return file;
            }
        }

        return null;
    }

    private static File getResourceFile(String fileID){
        String ftpContent = ResourceManager2.getFtpContent();
        if(!TextUtils.isEmpty(ftpContent)){
            File file = new File(Path.getTempDir(),fileID + ".txt");
            if (FileIOUtils.writeFileFromString(file,ftpContent,false)) {
                return file;
            }
        }
        return null;
    }

    private static File getPlayTotalFile(String fileID){
        JSONObject resourceObject = new JSONObject();
        resourceObject.put("resource_id",ResourceManager2.getResFileId());
        resourceObject.put("resource_version",ResourceManager2.getResFileVersion());

        JSONArray programArray = new JSONArray();
        List<Program> programList = DaoManager.get().query(Program.class);
        for (Program program : programList) {
            JSONObject programObject = new JSONObject();
            programObject.put("program_id",program.getId());
            programObject.put("program_name",program.getName());

            JSONArray materialArray = new JSONArray();
            List<Material> materialList = DaoManager.get().queryMaterialByProgramId(program.getId());
            for (Material material : materialList) {
                JSONObject materialObject = new JSONObject();
                materialObject.put("material_id",material.getId());
                materialObject.put("material_name",material.getName());
                materialObject.put("material_play_total",material.getPlayTotalTimes());
                materialObject.put("material_play_duration",material.getPlayTotalDuration());
                materialArray.add(materialObject);
            }
            programObject.put("material_list",materialArray);
            programArray.add(programObject);
        }
        resourceObject.put("program_list",programArray);

        File file = new File(Path.getTempDir(),fileID + ".txt");
        if (FileIOUtils.writeFileFromString(file,resourceObject.toJSONString(),false)) {
            return file;
        }
        return null;
    }

    private static File getLineDataFile(String fileID){
        String string = Cache.getString(Cache.Key.LINE_NAME);
        if(!TextUtils.isEmpty(string)){
            List<Site> upList = DaoManager.get().querySiteByLineName(string,0);
            List<Site> downList = DaoManager.get().querySiteByLineName(string,1);

            JSONArray upArray = new JSONArray();
            for (Site site : upList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("index",site.getIndex());
                jsonObject.put("name",site.getName());
                jsonObject.put("enName",site.getEnName());
                upArray.add(jsonObject);
            }

            JSONArray downArray = new JSONArray();
            for (Site site : downList) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("index",site.getIndex());
                jsonObject.put("name",site.getName());
                jsonObject.put("enName",site.getEnName());
                downArray.add(jsonObject);
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lineName",string);
            jsonObject.put("upSiteCount",upList.size());
            jsonObject.put("downSiteCount",downList.size());
            jsonObject.put("upList",upArray);
            jsonObject.put("downList",downArray);
            String jsonString = jsonObject.toJSONString();
            d("线路文件内容：" + jsonString);

            File file = new File(Path.getTempDir(),fileID + ".txt");
            if (FileIOUtils.writeFileFromString(file, jsonString)) {
                return file;
            }
        }
        return null;
    }

    //根据变量名获得参数默认值
    private static String getParamDefaultValue(String keyFieldName){
        try {
            //默认变量
            Field field1 = Cache.Default.class.getField(keyFieldName);
            String defaultFieldName = field1.getName();
            Object o = field1.get(defaultFieldName);
            if(o != null){
                return o.toString();
            }
        } catch (Exception e){
//            e.printStackTrace();
        }
        return "";
    }

    //根据变量值获得参数名称
    private static String getParamName(String key){
        if(TextUtils.isEmpty(key)){
            return "";
        }
        switch (key) {
            case Cache.Key.AGREEMENT_ORDINAL:
                return "协议类型";
                // zhang
            case Cache.Key.zhufuping:
                return "主副屏";
            case Cache.Key.LINE_INFO:
                return "线路详情";
            case Cache.Key.LINE_NAME:
                return "线路名";
            case Cache.Key.SITE_LIST_UP:
                return "上行列表";
            case Cache.Key.SITE_LIST_DOWN:
                return "下行列表";
            case Cache.Key.DEBUG:
                return "调试开关";
            case Cache.Key.VOLUME_PERCENT:
                return "音量百分比";
            case Cache.Key.LINE_STAR:
                return "线路星级";
            case Cache.Key.WORKER_ID:
                return "司机工号";
            case Cache.Key.DEVICE_NUMBER:
                return "设备编号";
            case Cache.Key.TERMINAL_NUMBER:
                return "终端编号";
            case Cache.Key.MESSAGE_SERIAL:
                return "消息流水";
            case Cache.Key.RESOURCE_ID:
                return "资源ID";
            case Cache.Key.APP_RES_ID:
                return "应用资源ID";
            case Cache.Key.RESOURCE_VERSION:
                return "资源版本";
            case Cache.Key.POLITIC:
                return "政治面貌";
            case Cache.Key.CAR_NUMBER:
                return "车辆编号";
            case Cache.Key.CAR_LICENSE_NUMBER:
                return "车牌号";
            case Cache.Key.PULSE_INTERVAL:
                return "心跳间隔";
            case Cache.Key.MAIN_SERVER_ADDRESS:
                return "主服务器地址";
            case Cache.Key.MAIN_SERVER_PORT:
                return "主服务器端口";
            case Cache.Key.SPARE_SERVER_ADDRESS:
                return "备用服务器地址";
            case Cache.Key.SPARE_SERVER_PORT:
                return "备用服务器端口";
            case Cache.Key.DEVICE_ADDRESS:
                return "设备地址";
            case Cache.Key.PRODUCT_NUMBER:
                return "厂商编码";
            case Cache.Key.AUTH_NUMBER:
                return "厂商授权码";
            case Cache.Key.APP_RESOURCE_ID:
                return "资源文件ID";
            case Cache.Key.APP_RESOURCE_VERSION:
                return "资源版本号";
            case Cache.Key.MESSAGE_RESEND_TIMES:
                return "消息重发时间";
            case Cache.Key.PRODUCT_DATE:
                return "生产日期";
            case Cache.Key.PARAMS_ID:
                return "参数配置文件ID";
            case Cache.Key.PARAMS_VERSION:
                return "参数配置文版本号";
            default:
                return "";
        }
    }

    //根据变量值获得参数ID
    private static int getParamId(String key){
        if(TextUtils.isEmpty(key)){
            return -1;
        }
        switch (key) {
            case Cache.Key.VOLUME_PERCENT:
                return 1;
            case Cache.Key.DEVICE_NUMBER:
                return 2;
            case Cache.Key.TERMINAL_NUMBER:
                return 3;
            case Cache.Key.CAR_NUMBER:
                return 4;
//            case Cache.Key.CAR_LICENSE_NUMBER:
//                return 5;
            case Cache.Key.PULSE_INTERVAL:
                return 6;
            case Cache.Key.MAIN_SERVER_ADDRESS:
                return 7;
            case Cache.Key.MAIN_SERVER_PORT:
                return 8;
            case Cache.Key.SPARE_SERVER_ADDRESS:
                return 9;
            case Cache.Key.SPARE_SERVER_PORT:
                return 10;
            case Cache.Key.DEVICE_ADDRESS:
                return 11;
            case Cache.Key.PRODUCT_NUMBER:
                return 12;
            case Cache.Key.AUTH_NUMBER:
                return 13;
            case Cache.Key.MESSAGE_RESEND_TIMES:
                return 14;
            case Cache.Key.PRODUCT_DATE:
                return 15;
            case Cache.Key.DEBUG:
                return 16;
            default:
                return -1;
        }
    }

    private static Object getParamValue(String key,String keyFieldName){
        if(TextUtils.isEmpty(key)){
            return "";
        }
        switch (key) {
            case Cache.Key.VOLUME_PERCENT:
            case Cache.Key.PULSE_INTERVAL:
            case Cache.Key.SPARE_SERVER_PORT:
            case Cache.Key.MAIN_SERVER_PORT:
            case Cache.Key.MESSAGE_RESEND_TIMES:
                if(Cache.contains(key)){
                    int anInt = Cache.getInt(key);
                    Log.e(TAG, "getParamValue: anInt  " + key + " --- " + anInt);
                    return anInt;
                } else {
                    String paramDefaultValue = getParamDefaultValue(keyFieldName);
                    Log.d(TAG, "getParamValue: anInt  " + key + " --- " + paramDefaultValue);
                    return paramDefaultValue;
                }
            case Cache.Key.DEBUG:
                if(Cache.contains(key)){
                    boolean aBoolean = Cache.getBoolean(key);
                    Log.e(TAG, "getParamValue: aBoolean  " + key + " --- " + aBoolean);
                    return aBoolean;
                } else {
                    String paramDefaultValue = getParamDefaultValue(keyFieldName);
                    Log.d(TAG, "getParamValue: aBoolean  " + key + " --- " + paramDefaultValue);
                    return paramDefaultValue;
                }
            default:
                if(Cache.contains(key)){
                    String string = Cache.getString(key);
                    Log.e(TAG, "getParamValue: string  " + key + " --- " + string);
                    return string;
                } else {
                    String paramDefaultValue = getParamDefaultValue(keyFieldName);
                    Log.d(TAG, "getParamValue: string  " + key + " --- " + paramDefaultValue);
                    return paramDefaultValue;
                }
        }
    }

    private static boolean canContinue(String key){
        if(TextUtils.isEmpty(key)){
            return false;
        }
        switch (key) {
            case Cache.Key.AGREEMENT_ORDINAL:
                //  zhang
            case Cache.Key.zhufuping:
            case Cache.Key.LINE_INFO:
            case Cache.Key.LINE_NAME:
            case Cache.Key.SITE_LIST_UP:
            case Cache.Key.SITE_LIST_DOWN:

            case Cache.Key.LINE_STAR:
            case Cache.Key.WORKER_ID:

            case Cache.Key.MESSAGE_SERIAL:
            case Cache.Key.RESOURCE_ID:
            case Cache.Key.APP_RES_ID:

            case Cache.Key.RESOURCE_VERSION:
            case Cache.Key.POLITIC:
            case Cache.Key.APP_RESOURCE_ID:
            case Cache.Key.APP_RESOURCE_VERSION:
            case Cache.Key.PARAMS_ID:
            case Cache.Key.PARAMS_VERSION:
                return false;
            default:
                return true;
        }
    }

    private static boolean zipFiles(List<File> files,File destFile){
        try {
            return ZipUtils.zipFiles(files,destFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final String TAG = "FileUploadMessageUtils";
    private static void d(String log){
        L.tcp(TAG,log);
    }

    // 2828
    // 06
    // 06
    // 220120000001
    // 013920170007
    // 0a3c
    // 0016
    // 0a3d
    // 05
    // 0000000000000042
    // 0208b6afbbadcad3c6b564510C
}
