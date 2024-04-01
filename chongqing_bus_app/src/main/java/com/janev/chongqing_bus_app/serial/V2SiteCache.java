package com.janev.chongqing_bus_app.serial;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.CloseUtils;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class V2SiteCache {
    private static final String TAG = "V2SiteCache";
    private String lineName;
    private byte upDown = (byte)0xff;
    private byte[][] packArray;
    private final LinkedHashMap<Integer,String> siteMap = new LinkedHashMap<>();
    private final OnSiteDataListener onDataListener;
    public V2SiteCache(OnSiteDataListener onDataListener) {
        this.onDataListener = onDataListener;
    }

    public void put(ByteBuffer byteBuffer){
        try {
            int lineNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
            d("线路号长度：" + lineNameLength);
            byte[] lineNameBytes = new byte[lineNameLength];
            byteBuffer.get(lineNameBytes);
            String lineName = StringUtils.hexStringToString(BytesUtils.bytesToHex(lineNameBytes));
            d("线路号：" + lineName);
            byte upDown = byteBuffer.get();
            d("运行方向：" + upDown);
            int siteCount = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
            d("站点个数：" + siteCount);
            int packCount = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
            d("分包总数：" + packCount);
            int packNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(byteBuffer.get()));
            d("本包序号：" + packNumber);

            //取出除结尾2位外的所有数据
            byte[] packBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(packBytes);
            d("数据长度：" + packBytes.length);

            //判断是否需要重置分包数组
            if(!TextUtils.equals(this.lineName,lineName)
                    || this.upDown != upDown
                    || packNumber <= 1){
                this.lineName = lineName;
                this.upDown = upDown;
                this.packArray = null;
                this.siteMap.clear();
            }

            //初始化分包数组
            if(packArray == null){
                if(packCount == 0){
                    this.packArray = new byte[1][];
                } else {
                    this.packArray = new byte[packCount][];
                }
            }

            //确定当前分包的索引
            int arrayIndex = 0;
            if(packCount != 0){
                arrayIndex = packNumber - 1;
            }
            packArray[arrayIndex] = packBytes;

            if(packNumber == packCount){
                e(" ---------------------------------------------------------- ");
                try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
                    for (byte[] bytes : packArray) {
                        outputStream.write(bytes);
                    }
                    ByteBuffer buffer = ByteBuffer.wrap(outputStream.toByteArray());
                    CloseUtils.closeIO(outputStream);

                    while (buffer.hasRemaining()) {
                        int siteIndex = BytesUtils.hex16to10(BytesUtils.byteToHex(buffer.get()));
                        d("站点序号：" + siteIndex);

                        //如果本包非第一包并且站点数为0，说明是上一包的英文名留到下一包了
                        if(packNumber > 1 && siteIndex == 0){
                            continue;
                        }

                        BitSet bitSet = BitSet.valueOf(new byte[]{buffer.get()});
                        boolean isResponsive = bitSet.get(0);
                        d("响应式：" + isResponsive);

                        int siteChNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(buffer.get()));
                        d("站点中文名长度：" + siteChNameLength);
                        String siteChName = "";
                        if(siteChNameLength > 0){
                            byte[] chNameBytes = new byte[siteChNameLength];
                            buffer.get(chNameBytes);
                            siteChName = StringUtils.hexStringToString(BytesUtils.bytesToHex(chNameBytes));
                        }
                        d("站点中文名：" + siteChName);

                        String siteEnName = "";
                        if(buffer.hasRemaining()){
                            int siteEnNameLength = BytesUtils.hex16to10(BytesUtils.byteToHex(buffer.get()));
                            d("站点英文名长度：" + siteEnNameLength);
                            if(siteEnNameLength > 0){
                                byte[] enNameBytes = new byte[siteEnNameLength];
                                buffer.get(enNameBytes);
                                siteEnName = StringUtils.hexStringToString(BytesUtils.bytesToHex(enNameBytes));
                            }
                        }
                        d("站点英文名：" + siteEnName);

                        siteMap.put(siteIndex,siteChName);
                    }

                    e( "addToList: 解析完毕：" + siteMap.entrySet().toString());
                    if(this.onDataListener != null){
                        onDataListener.onChanged(lineName,upDown,new LinkedHashMap<>(siteMap));
                    }
                } catch (IOException e) {
                    e(e);
                }
            }
        } catch (Exception e){
            e(e);
        }
    }

    private void d(String log){
        L.serialD(TAG,log);
    }

    private void e(String log){
        L.serialE(TAG,log);
    }
    private void e(Exception e){
        L.serialE(TAG,e);
    }

    public interface OnSiteDataListener {
        void onChanged(String lineName,byte upDown,LinkedHashMap<Integer,String> siteMap);
    }
}
