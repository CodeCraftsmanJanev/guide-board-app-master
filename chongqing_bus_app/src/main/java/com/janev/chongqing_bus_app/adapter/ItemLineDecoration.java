package com.janev.chongqing_bus_app.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.janev.chongqing_bus_app.App;
import com.janev.chongqing_bus_app.R;

public class ItemLineDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "ItemLineDecoration";

    private Paint mLinePaint;

    public ItemLineDecoration() {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(App.getContext().getResources().getColor(android.R.color.holo_blue_bright));
        //设置连接线宽度
        mLinePaint.setStrokeWidth(20);
    }

    float x = 0;
    float y = 0;
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

//        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) parent.getLayoutManager();
//        if(linearLayoutManager == null){
//            return;
//        }
//        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
//        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
//        Log.d(TAG, "onDraw: " + firstVisibleItemPosition + " --- " + lastVisibleItemPosition);
//
//        View viewByPosition = linearLayoutManager.findViewByPosition(firstVisibleItemPosition);
//        if(viewByPosition == null){
//            return;
//        }
//        SiteListAdapter.Holder childViewHolder = (SiteListAdapter.Holder) parent.getChildViewHolder(viewByPosition);
//
//        ImageView ivDot = childViewHolder.binding.ivDot;
//        float v = ivDot.getPivotY();//以圆点的Y轴旋转点为线的位置
//        Log.d(TAG, "onDraw: 线的Y轴：" + v);
//
//        c.drawLine(0,v,parent.getWidth(),v,mLinePaint);
    }
}