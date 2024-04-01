package com.janev.chongqing_bus_app.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.Utils;
import com.janev.chongqing_bus_app.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


public class NetworkReceiver{
    private static final String TAG = "NetworkReceiver";

    private final Utils.Consumer<Integer> resIdConsumer;
    private final Context context;
    private TelephonyManager telephonyManager;
    private WifiManager wifiManager;

    public NetworkReceiver(Context context,@NonNull Utils.Consumer<Integer> resIdConsumer) {
        this.resIdConsumer = resIdConsumer;
        this.context = context;

        callbackIcon();
    }

    public void unregister(){
        if(NetworkUtils.isRegisteredNetworkStatusChangedListener(networkStatusChangedListener)){
            NetworkUtils.unregisterNetworkStatusChangedListener(networkStatusChangedListener);
        }
        cancelListenWifi();
        cancelListenSim();
    }

    private void callbackIcon(){
        networkStatusChangedListener.onConnected(NetworkUtils.getNetworkType());
        if (!NetworkUtils.isRegisteredNetworkStatusChangedListener(networkStatusChangedListener)) {
            NetworkUtils.registerNetworkStatusChangedListener(networkStatusChangedListener);
        }
    }

    private final NetworkUtils.OnNetworkStatusChangedListener networkStatusChangedListener = new NetworkUtils.OnNetworkStatusChangedListener() {
        private NetworkUtils.NetworkType thisNetwork;
        @Override
        public void onDisconnected() {
            thisNetwork = null;
            resIdConsumer.accept(R.mipmap.icon_ether_no);
        }

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
            Log.e(TAG, "onConnected: " + networkType.name());
            //如果缓存网络与当前不同，则取消监听后重新监听
            if(thisNetwork != null && thisNetwork.compareTo(networkType) != 0){
                cancelListenSim();
                cancelListenWifi();
            }

            thisNetwork = networkType;

            switch (networkType) {
                case NETWORK_2G:
                case NETWORK_3G:
                case NETWORK_4G:
                case NETWORK_5G:
                    setSimSign();
                    break;
                case NETWORK_WIFI:
                    setWifiSign();
                    break;
                case NETWORK_ETHERNET:
                    resIdConsumer.accept(R.mipmap.icon_ether_yes);
                    break;
                case NETWORK_UNKNOWN:
                    resIdConsumer.accept(R.mipmap.icon_net_unknown);
                    break;
                case NETWORK_NO:
                default:
                    resIdConsumer.accept(R.mipmap.icon_ether_no);
                    break;
            }
        }
    };

    private void setSimSign(){
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager == null){
            return;
        }
        int simState = telephonyManager.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
            case TelephonyManager.SIM_STATE_UNKNOWN:
                resIdConsumer.accept(R.mipmap.icon_signal_no);
                break;
            default:
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                break;
        }
    }

    private final PhoneStateListener phoneStateListener = new PhoneStateListener(){
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            int asu = signalStrength.getGsmSignalStrength();
            int lastSignal = -113 + 2 * asu;
            if(lastSignal > 0){
                resIdConsumer.accept(R.mipmap.icon_signal_5);
            } else if(lastSignal >= -55){
                resIdConsumer.accept(R.mipmap.icon_signal_4);
            } else if(lastSignal >= -70){
                resIdConsumer.accept(R.mipmap.icon_signal_3);
            } else if(lastSignal >= -85){
                resIdConsumer.accept(R.mipmap.icon_signal_2);
            } else if(lastSignal >= -100){
                resIdConsumer.accept(R.mipmap.icon_signal_1);
            } else {
                resIdConsumer.accept(R.mipmap.icon_signal_no);
            }
        }
    };

    private void cancelListenSim(){
        if(telephonyManager != null){
            telephonyManager.listen(phoneStateListener,PhoneStateListener.LISTEN_NONE);
            telephonyManager = null;
        }
    }

    private Disposable wifiDisposable;
    private void setWifiSign(){
        if(wifiDisposable != null && !wifiDisposable.isDisposed()){
            wifiDisposable.dispose();
            wifiDisposable = null;
        }
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager == null){
            return;
        }
        wifiDisposable = Observable.interval(1, 5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        aLong -> {
                            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                            int level = connectionInfo.getRssi();
                            if (level <= 0 && level >= -50) {
                                resIdConsumer.accept(R.mipmap.icon_wifi_signal_4);
                            } else if (level < -50 && level >= -70) {
                                resIdConsumer.accept(R.mipmap.icon_wifi_signal_3);
                            } else if (level < -70 && level >= -80) {
                                resIdConsumer.accept(R.mipmap.icon_wifi_signal_2);
                            } else if (level < -80 && level >= -100) {
                                resIdConsumer.accept(R.mipmap.icon_wifi_signal_1);
                            } else {
                                resIdConsumer.accept(R.mipmap.icon_wifi_signal_no);
                            }
                        },
                        throwable -> {

                        },
                        () -> {

                        }
                );
    }

    private void cancelListenWifi(){
        if(wifiDisposable != null && !wifiDisposable.isDisposed()){
            wifiDisposable.dispose();
            wifiDisposable = null;
            wifiManager = null;
        }
    }
}
