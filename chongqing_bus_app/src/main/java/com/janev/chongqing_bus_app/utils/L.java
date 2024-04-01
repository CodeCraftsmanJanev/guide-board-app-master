package com.janev.chongqing_bus_app.utils;

import android.util.Log;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.janev.chongqing_bus_app.system.Path;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class L {
    private static final String TAG = "L";

    private static final String LINE_SEP       = System.getProperty("line.separator");

    private static final ExecutorService OUT_LOG_EXECUTOR = Executors.newSingleThreadExecutor();

    private static boolean logSwitch = true;

    public static void setLogSwitch(boolean s){
        logSwitch = s;
    }

    private static void removeObsoleteFiles(String dir){
        Log.d(TAG, "检查路径:" + dir);
        List<File> fileList = FileUtils.listFilesInDir(dir);

        Log.d(TAG, "共有文件:" + fileList.size());
        Date currentDate = new Date();
        long sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000; // 7天的毫秒数

        for (File file : fileList) {
            long lastModified = file.lastModified();
            long timeDifference = currentDate.getTime() - lastModified;
            if (timeDifference > sevenDaysInMillis) {
                // 文件超过7天
                boolean delete = FileUtils.delete(file);
                Log.e(TAG, "删除文件：" + file.getName() + " --- " + delete);
            }
        }
    }

    public static void clean(){
        removeObsoleteFiles(Path.getSerialLogPath());
        removeObsoleteFiles(Path.getAppLogPath());
    }

    public static void serialD(String TAG, String log){
        Log.d(TAG, log);

        if(logSwitch){
            input2File(Path.getSerialLogPath(),TAG,log);
        }
    }

    public static void serialE(String TAG,String error){
        Log.e(TAG, error);

        if(logSwitch){
            input2File(Path.getSerialLogPath(),TAG,error);
        }
    }

    public static void d(String tag,String log,String path){
        Log.d(tag, log);
        if(logSwitch){
            input2File(path,tag,log);
        }
    }
    public static void e(String tag,String log,String path){
        Log.e(tag, log);
        if(logSwitch){
            input2File(path,tag,log);
        }
    }
    public static void w(String tag,String log,String path){
        Log.w(tag, log);
        if(logSwitch){
            input2File(path,tag,log);
        }
    }

    public static void serialE(String TAG,Throwable throwable){
        if(throwable == null){
            throwable = new Exception("null exception");
        }
        serialE(TAG,"串口错误：" + throwable.getMessage());
    }

    public static void appD(String TAG, String log){
        Log.d(TAG, log);

        if(logSwitch){
            input2File(Path.getAppLogPath(),TAG,log);
        }
    }

    public static void ads(String TAG,String log){
        Log.d(TAG, log);

        if(logSwitch){
            input2File(Path.getAppLogPath(),TAG,log);
        }
    }

    public static void tcp(String TAG,String log){
        Log.d(TAG, log);

        if(logSwitch){
            input2File(Path.getAppLogPath(),TAG,log);
        }
    }

    public static void tcpE(String TAG,String log){
        Log.e(TAG, log);

        if(logSwitch){
            input2File(Path.getAppLogPath(),TAG,log);
        }
    }

    private static void input2File(String dir,String tag,Object obj){
        OUT_LOG_EXECUTOR.execute(() -> {
            String content;
            if(obj instanceof Throwable){
                Throwable t = (Throwable) obj;
                content = getFullStackTrace(t);
            } else {
                content = (String) obj;
            }
            String time = TimeUtils.date2String(new Date(), "yyyyMMddHH:mm:ss");
            String fileName = time.substring(0, 8);
            String timeTag = time.substring(8);
            File file = new File(dir, fileName);
            if (FileUtils.createOrExistsFile(file)) {
                String fullLog = timeTag + "/  " + tag + ":  " + content + LINE_SEP;
                FileIOUtils.writeFileFromString(file,fullLog,true);
            }
        });
    }

    public static String getFullStackTrace(Throwable throwable) {
        final List<Throwable> throwableList = new ArrayList<>();
        while (throwable != null && !throwableList.contains(throwable)) {
            throwableList.add(throwable);
            throwable = throwable.getCause();
        }
        final int size = throwableList.size();
        final List<String> frames = new ArrayList<>();
        List<String> nextTrace = getStackFrameList(throwableList.get(size - 1));
        for (int i = size; --i >= 0; ) {
            final List<String> trace = nextTrace;
            if (i != 0) {
                nextTrace = getStackFrameList(throwableList.get(i - 1));
                removeCommonFrames(trace, nextTrace);
            }
            if (i == size - 1) {
                frames.add(throwableList.get(i).toString());
            } else {
                frames.add(" Caused by: " + throwableList.get(i).toString());
            }
            frames.addAll(trace);
        }
        StringBuilder sb = new StringBuilder();
        for (final String element : frames) {
            sb.append(element).append(LINE_SEP);
        }
        return sb.toString();
    }

    private static List<String> getStackFrameList(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        final String stackTrace = sw.toString();
        final StringTokenizer frames = new StringTokenizer(stackTrace, LINE_SEP);
        final List<String> list = new ArrayList<>();
        boolean traceStarted = false;
        while (frames.hasMoreTokens()) {
            final String token = frames.nextToken();
            // Determine if the line starts with <whitespace>at
            final int at = token.indexOf("at");
            if (at != -1 && token.substring(0, at).trim().isEmpty()) {
                traceStarted = true;
                list.add(token);
            } else if (traceStarted) {
                break;
            }
        }
        return list;
    }

    private static void removeCommonFrames(final List<String> causeFrames, final List<String> wrapperFrames) {
        int causeFrameIndex = causeFrames.size() - 1;
        int wrapperFrameIndex = wrapperFrames.size() - 1;
        while (causeFrameIndex >= 0 && wrapperFrameIndex >= 0) {
            // Remove the frame from the cause trace if it is the same
            // as in the wrapper trace
            final String causeFrame = causeFrames.get(causeFrameIndex);
            final String wrapperFrame = wrapperFrames.get(wrapperFrameIndex);
            if (causeFrame.equals(wrapperFrame)) {
                causeFrames.remove(causeFrameIndex);
            }
            causeFrameIndex--;
            wrapperFrameIndex--;
        }
    }
}
