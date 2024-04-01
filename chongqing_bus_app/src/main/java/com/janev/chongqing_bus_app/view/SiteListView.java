package com.janev.chongqing_bus_app.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteListView extends View {

    private final List<String> stringList = new ArrayList<>();

    public SiteListView(@NonNull Context context) {
        this(context,null);
    }

    public SiteListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public SiteListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    public void setList(List<String> list){
        stringList.clear();
        if(list != null && !list.isEmpty()){
            stringList.addAll(list);
        }
        postInvalidate();
    }

    private void init(){
        int viewWidth = getMeasuredWidth();
        int viewHeight = getMeasuredHeight();
        int screenNumber = 7;
        int dotWidth = 120;

        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.BLACK);


    }


}
