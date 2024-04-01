package com.janev.chongqing_bus_app.tcp.message.message_utils;

import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Knowledge;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.ReplyRequest;
import com.janev.chongqing_bus_app.utils.BytesUtils;
import com.janev.chongqing_bus_app.utils.L;
import com.janev.chongqing_bus_app.utils.StringUtils;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class KnowledgeMessageUtils {


    public static void resolve(byte order, byte[] msgSerial, byte[] data) {
        new ReplyRequest(BytesUtils.byteToHex(order),BytesUtils.bytesToHex(msgSerial),ReplyRequest.SUCCESS).send();

        final Queue<Byte> byteQueue = new LinkedList<>();
        for (byte datum : data) {
            byteQueue.add(datum);
        }

        Byte showFlag = byteQueue.poll();
        d("显示标识：" + showFlag);

        int publicityWordNumber = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
        d("宣传语个数：" + publicityWordNumber);

        List<Knowledge> knowledgeList = new ArrayList<>();
        List<Knowledge> tempKnowledgeList = new ArrayList<>();

        int tag = 0;
        byte type = (byte)0xFF;
        int length = 0;
        while (!byteQueue.isEmpty()) {
            switch (tag) {
                case 0:
                    type = byteQueue.poll();
                    tag = 1;
                    break;
                case 1:
                    length = BytesUtils.hex16to10(BytesUtils.byteToHex(byteQueue.poll()));
                    tag = 2;
                    break;
                case 2:
                    String content = StringUtils.hexStringToString(BytesUtils.bytesToHex(getBytes(byteQueue,length)));
                    Knowledge knowledge = new Knowledge();
                    knowledge.setContent(content);
                    //循环宣传语
                    if(type == 0x01){
                        knowledgeList.add(knowledge);
                    }
                    //临时宣传语
                    else if(type == 0x02){
                        tempKnowledgeList.add(knowledge);
                    }
                    tag = 0;
                    break;
            }
        }

        //如果为空或者为重置宣传语，则设置后发空，由UI重新查询设置
        if(showFlag == (byte)0x01){
            //重置
            DaoManager.get().setKnowledge(knowledgeList);
            d("循环宣传语为空或标识为重置，发送空消息");
            UiMessageUtils.getInstance().send(UiEvent.EVENT_ADD_KNOWLEDGE);
        }
        //如果宣传语不为空且为新增，则添加后发送新增的宣传语
        else if(showFlag == (byte)0x00){
            //新增
            DaoManager.get().addKnowledge(knowledgeList);
            d("循环宣传语不为空且为添加，发送新增");
            if(!knowledgeList.isEmpty()){
                UiMessageUtils.getInstance().send(UiEvent.EVENT_ADD_KNOWLEDGE);
            }
        }

        d("发送临时宣传语：" + tempKnowledgeList.size());
        UiMessageUtils.getInstance().send(UiEvent.EVENT_SHOW_TEMP_KNOWLEDGE,tempKnowledgeList);
    }


    private static byte[] getBytes(Queue<Byte> byteQeque, int length){
        byte[] data = new byte[length];
        for (int i = 0; i < data.length; i++) {
            data[i] = byteQeque.poll();
        }
        return data;
    }

    private static final String TAG = "KnowledgeMessageUtils";
    private static void d(String log){
        L.tcp(TAG,log);
    }

}
