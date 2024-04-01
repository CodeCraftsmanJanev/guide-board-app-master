package com.janev.chongqing_bus_app.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class AppInstallerUtils {
    private static final String TAG = "UpgradeAppManager";

//    public static void installSilent(String path){
//        File apkFile = new File(path);
//        PackageManager packageManager = App.getContext().getPackageManager();
//        PackageInstaller packageInstaller = packageManager.getPackageInstaller();
//        PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
//        sessionParams.setSize(apkFile.length());
//
//        int sessionId = createSession(packageInstaller,sessionParams);
//        if(sessionId != -1){
//            boolean copySuccess = copyInstallFile(packageInstaller, sessionId,apkFile.getPath());
//            if(copySuccess){
//                Log.e(TAG, "installSilent: 拷贝成功");
//                execInstallCommand(App.getContext(),packageInstaller,sessionId);
//            } else {
//                Log.e(TAG, "installSilent: 拷贝失败");
//            }
//        }
//    }
//    private static int createSession(PackageInstaller packageInstaller,
//                              PackageInstaller.SessionParams sessionParams) {
//        int sessionId = -1;
//        try {
//            sessionId = packageInstaller.createSession(sessionParams);
//            d("创建会话：" + sessionId);
//        } catch (IOException e) {
//            e.printStackTrace();
//            d("创建会话失败");
//        }
//        return sessionId;
//    }
//    private static boolean copyInstallFile(PackageInstaller packageInstaller,
//                                    int sessionId, String apkFilePath) {
//        InputStream in = null;
//        OutputStream out = null;
//        PackageInstaller.Session session = null;
//        boolean success = false;
//        try {
//            File apkFile = new File(apkFilePath);
//            session = packageInstaller.openSession(sessionId);
//            out = session.openWrite("base.apk", 0, apkFile.length());
//            in = new FileInputStream(apkFile);
//            int total = 0, c;
//            byte[] buffer = new byte[65536];
//            while ((c = in.read(buffer)) != -1) {
//                total += c;
//                out.write(buffer, 0, c);
//            }
//            session.fsync(out);
//            Log.i(TAG, "streamed " + total + " bytes");
//            success = true;
//            Log.e(TAG, "copyInstallFile: 拷贝成功");
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "copyInstallFile: 拷贝失败：" + e.getMessage());
//        } finally {
//            closeQuietly(out);
//            closeQuietly(in);
//            closeQuietly(session);
//        }
//        return success;
//    }
//
//    private static void execInstallCommand(Context context, PackageInstaller packageInstaller, int sessionId) {
//        PackageInstaller.Session session = null;
//        try {
//            session = packageInstaller.openSession(sessionId);
//            Intent intent = new Intent(context, InstallResultReceiver.class);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            session.commit(pendingIntent.getIntentSender());
//            Log.e(TAG, "execInstallCommand: 已安装");
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e(TAG, "execInstallCommand: 安装失败：" + e.getMessage());
//        } finally {
//            closeQuietly(session);
//        }
//    }
//
//    public static class InstallResultReceiver extends BroadcastReceiver{
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent != null) {
//                final int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
//                        PackageInstaller.STATUS_FAILURE);
//                if (status == PackageInstaller.STATUS_SUCCESS) {
//                    // success
//                    d("安装成功");
//                } else {
//                    d("安装失败：" + intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
//                    //Log.e(TAG, intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE));
//                }
//            }
//        }
//    }
//
//    private static void closeQuietly(Closeable closeable){
//        if(closeable != null){
//            try {
//                closeable.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private static void d(String log){
        L.appD(TAG,log);
    }

    private static void execLinuxCommand(){
        String cmd= "sleep 10; am start -n com.janev.chongqing_bus_app/ui.SplashActivity";
        //Runtime对象
        Runtime runtime = Runtime.getRuntime();
        try {
            Process localProcess = runtime.exec("su");
            OutputStream localOutputStream = localProcess.getOutputStream();
            DataOutputStream localDataOutputStream = new DataOutputStream(localOutputStream);
            localDataOutputStream.writeBytes(cmd);
            localDataOutputStream.flush();
            Log.d(TAG,"设备准备重启");
        } catch (IOException e) {
            Log.d(TAG,TAG+"strLine:"+e.getMessage());
            e.printStackTrace(); }
    }

    public static InstallResult installSilentWithCommand(String path) {
        execLinuxCommand();

        String cmd = "pm install -r " + path;
        Log.d(TAG, "installSilentWithCommand: 命令：" + cmd);
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
            //静默安装需要root权限
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();
            //执行命令
            process.waitFor();
            //获取返回结果
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
            //显示结果
            Log.d(TAG, "成功消息：" + successMsg.toString() + "\n" + "错误消息: " + errorMsg.toString());
            if(!TextUtils.isEmpty(errorMsg.toString())){
                return new InstallResult(false,errorMsg.toString());
            }
            return new InstallResult(true,successMsg.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "installSilentWithCommand: 执行异常：" + e.getMessage() );
            return new InstallResult(false,e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class InstallResult{
        private boolean success;
        private String msg;

        public InstallResult(boolean success, String msg) {
            this.success = success;
            this.msg = msg;
        }
    }
}
