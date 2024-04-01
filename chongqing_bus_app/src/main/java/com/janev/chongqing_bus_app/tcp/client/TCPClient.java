package com.janev.chongqing_bus_app.tcp.client;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.App;
import com.janev.chongqing_bus_app.easysocket.EasySocket;
import com.janev.chongqing_bus_app.easysocket.config.EasySocketOptions;
import com.janev.chongqing_bus_app.easysocket.connection.action.SocketStatus;
import com.janev.chongqing_bus_app.easysocket.connection.heartbeat.HeartManager;
import com.janev.chongqing_bus_app.easysocket.connection.reconnect.DefaultReConnection;
import com.janev.chongqing_bus_app.easysocket.entity.OriginReadData;
import com.janev.chongqing_bus_app.easysocket.entity.SocketAddress;
import com.janev.chongqing_bus_app.easysocket.interfaces.conn.IConnectionManager;
import com.janev.chongqing_bus_app.easysocket.interfaces.conn.ISocketActionListener;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.TCPLog;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;

public abstract class TCPClient implements ISocketActionListener, HeartManager.HeartbeatListener, DataResolver.OnDataCallback {
    private final String TAG = getClass().getSimpleName();
    private final EasySocket easySocket;
    private final DataResolver dataResolver;

    public TCPClient(@NonNull String ip, int port,@NonNull String spareIp, int sparePort,long pulseFrequency, int pulseFeedLoseTimes, DataResolver dataResolver) {
        SocketAddress socketAddress = new SocketAddress(ip,port);

        EasySocketOptions.Builder optionsBuilder = new EasySocketOptions.Builder()
                .setConnectTimeout(60000)
                .setRequestTimeout(60000)
                .setReconnectionManager(new DefaultReConnection())
                .setSocketAddress(socketAddress)
                .setHeartbeatFreq(pulseFrequency)
                .setMaxHeartbeatLoseTimes(pulseFeedLoseTimes);

        if(!TextUtils.isEmpty(spareIp) && sparePort != 0){
            SocketAddress backupAddress = new SocketAddress(spareIp,sparePort);
            optionsBuilder.setBackupAddress(backupAddress);
        }

        EasySocketOptions options = optionsBuilder.build();
        EasySocket.getInstance().setDebug(false);

        easySocket = EasySocket.getInstance()
                .createConnection(options, App.getContext())
                .subscribeSocketAction(this);

        this.dataResolver = dataResolver;

        //设置心跳间隔
        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_SET_PULSE_FREQUENCY, localMessage -> {
            int pulseFrequency1 = (int) localMessage.getObject();
            if(easySocket != null){
                easySocket.getDefOptions().setHeartbeatFreq(pulseFrequency1);
            }
        });
        //查询TCP连接状态
        UiMessageUtils.getInstance().addListener(UiEvent.EVENT_QUERY_TCP_CONNECTION, localMessage -> {
            Object object = localMessage.getObject();
            if(object != null){
                return;
            }

            String mainIp = "", mainPort = "",
                    spareIp1 = "", sparePort1 = "",
                    isConnect = "未连接", isLogon = "未登录",
                    loginTime = "",pulseTime = "";
            if(easySocket != null && easySocket.getDefOptions() != null){
                EasySocketOptions defOptions = easySocket.getDefOptions();
                SocketAddress mainSocketAddress = defOptions.getSocketAddress();
                SocketAddress backupAddress = defOptions.getBackupAddress();

                if(mainSocketAddress != null){
                    mainIp = mainSocketAddress.getIp();
                    mainPort = mainSocketAddress.getPort() + "";
                }

                if(backupAddress != null){
                    spareIp1 = backupAddress.getIp();
                    sparePort1 = backupAddress.getPort() + "";
                }

                try {
                    IConnectionManager defconnection = easySocket.getDefconnection();
                    int connectionStatus = defconnection.getConnectionStatus();
                    switch (connectionStatus) {
                        case SocketStatus.SOCKET_DISCONNECTED:
                            isConnect = "已断开";
                            break;
                        case SocketStatus.SOCKET_CONNECTING:
                            isConnect = "正在连接";
                            break;
                        case SocketStatus.SOCKET_CONNECTED:
                            isConnect = "已连接";
                            break;
                        case SocketStatus.SOCKET_DISCONNECTING:
                            isConnect = "正在断开";
                            break;
                    }
                    isLogon = getLogonState();
                    loginTime = getLogonTime();
                } catch (Exception e){
                    e.printStackTrace();
                }

                pulseTime = getPulseTime();
            }

            ConnectionInfo connectionInfo = new ConnectionInfo(mainIp,mainPort, spareIp1, sparePort1,isConnect,isLogon,loginTime,pulseTime);
            UiMessageUtils.getInstance().send(UiEvent.EVENT_QUERY_TCP_CONNECTION,connectionInfo);
        });
    }

    protected String getLogonState(){
        return "";
    }

    protected String getLogonTime(){
        return "";
    }

    protected String getPulseTime(){return "";}

    public void connect(){
        if(easySocket == null){
            d("连接失败，manager为null");
        } else if(easySocket.getDefconnection().getConnectionStatus() == SocketStatus.SOCKET_CONNECTED){
            d("已连接...");
        } else {
            d("开始连接...");
            easySocket.connect();
        }
    }

    public void disconnect(){
        if(heartBeatSocket != null){
            heartBeatSocket.disconnect(false);
        }
        if(easySocket != null && easySocket.getDefconnection().getConnectionStatus() == SocketStatus.SOCKET_CONNECTED){
            d("断开连接");
            easySocket.disconnect(false);
        } else {
            d("连接已断开");
        }
    }

    public void destroy(){
        disconnect();
    }

    public boolean isConnect() {
        return easySocket != null && easySocket.getDefconnection() != null && easySocket.getDefconnection().getConnectionStatus() == SocketStatus.SOCKET_CONNECTED;
    }

    private EasySocket heartBeatSocket;
    public void startHeatBeat(){
        heartBeatSocket = EasySocket.getInstance().startHeartBeat(TCPClient.this);
    }

    @Override
    public void onSocketConnSuccess(SocketAddress socketAddress) {
        d("连接成功：" + socketAddress.getIp() + "，" + socketAddress.getPort());
    }

    @Override
    public void onSocketConnFail(SocketAddress socketAddress, boolean isNeedReconnect) {
        d( "连接失败: 是否需要重连" + isNeedReconnect);
    }

    @Override
    public void onSocketDisconnect(SocketAddress socketAddress, boolean isNeedReconnect) {
        d( "连接断开: 是否需要重连" + isNeedReconnect);
    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {

    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, String readData) {

    }

    @Override
    public void onSocketResponse(SocketAddress socketAddress, byte[] readData) {
        d("收到数据 <--- " + BytesUtils.bytesToHex(readData));

        TCPLog.getInstance().inputData(readData);

        this.dataResolver.resolve(readData,this);
    }

    @Override
    public byte[] heartBeatBytes() {
        return new byte[0];
    }

    @Override
    public boolean isServerHeartbeat(OriginReadData orginReadData) {

        return false;
    }

    protected void d(String log){
        L.tcp(TAG,log);
    }

    public static class ConnectionInfo{
        private final String mainIp;
        private final String mainPort;
        private final String spareIp;
        private final String sparePort;

        private final String isConnect;//是否链接
        private final String isLogon;//是否登录

        private final String loginTime;//登录时间
        private final String pulseTime;//心跳时间

        public ConnectionInfo(String mainIp, String mainPort, String spareIp, String sparePort, String isConnect, String isLogon,String loginTime,String pulseTime) {
            this.mainIp = mainIp;
            this.mainPort = mainPort;
            this.spareIp = spareIp;
            this.sparePort = sparePort;
            this.isConnect = isConnect;
            this.isLogon = isLogon;
            this.loginTime = loginTime;
            this.pulseTime = pulseTime;
        }

        public String getPulseTime() {
            return pulseTime;
        }

        public String getLoginTime() {
            return loginTime;
        }

        public String getMainIp() {
            return mainIp;
        }

        public String getMainPort() {
            return mainPort;
        }

        public String getSpareIp() {
            return spareIp;
        }

        public String getSparePort() {
            return sparePort;
        }

        public String getIsConnect() {
            return isConnect;
        }

        public String getIsLogon() {
            return isLogon;
        }
    }
}
