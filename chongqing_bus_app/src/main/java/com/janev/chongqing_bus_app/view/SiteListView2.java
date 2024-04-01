package com.janev.chongqing_bus_app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SiteListView2 extends SurfaceView {

    private int viewWidth,viewHeight;
    private SurfaceHolder surfaceHolder;

    private Paint textPaint,linePaint,dotPaint;

    public SiteListView2(@NonNull Context context) {
        this(context,null);
    }

    public SiteListView2(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public SiteListView2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init(){
        d("初始化");
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(mSurfaceHolderCallback);
    }

    private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            viewWidth = getWidth();
            viewHeight = getHeight();
            surfaceHolder = holder;

            textPaint = new Paint();
            textPaint.setTextSize(30);
            textPaint.setColor(Color.BLACK);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);

            linePaint = new Paint();
            linePaint.setColor(Color.BLUE);

            dotPaint = new Paint();
            dotPaint.setColor(Color.YELLOW);

            startRenderThread();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            stop();
        }
    };

    private Disposable disposable;
    private void startRenderThread(){
        stop();
        disposable = Observable.interval(100, TimeUnit.MILLISECONDS)
                .doOnNext(aLong -> {
                    Canvas canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas();
                        canvas.drawColor(Color.WHITE);

                        drawContent(canvas);
                    } catch (Exception e){
                        e.printStackTrace();
                    } finally {
                        if(canvas != null){
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                })
                .subscribeOn(Schedulers.single())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> {
                    initLinePoint();
                })
                .subscribe();
    }

    private void stop(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }

    private void drawContent(Canvas canvas){
        drawLine(canvas);

    }

    private int lineHeight,lineWidth,lineLeft,lineTop,lineRight,lineBottom;
    private void initLinePoint(){
        lineHeight = 20;
        lineWidth = (int) (viewWidth * 0.8);
        lineLeft = (viewWidth - lineWidth) / 2;
        lineTop = viewHeight / 10;
        lineRight = lineWidth + lineLeft;
        lineBottom = lineTop + lineHeight;
    }


    private void drawLine(Canvas canvas){
        canvas.drawRect(lineLeft,lineTop,lineRight,lineBottom,linePaint);
    }

    private int dotNumber = 3;
    private void drawDot(Canvas canvas){



    }

    private static final String TAG = "SiteListView";
    private void d(String log){
        Log.d(TAG, log);
    }
}
