package com.janev.chongqing_bus_app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ImageUtils;
import com.janev.chongqing_bus_app.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ArrowAnimationView extends TextureView {

    private final int ARROW_NUMBER = 3;
    private final long UPDATE_TIME = 300;

    public ArrowAnimationView(@NonNull Context context) {
        this(context,null);
    }

    public ArrowAnimationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public ArrowAnimationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private Paint paint = new Paint();
    private void init(){
        setOpaque(false);

        paint.setAntiAlias(true);

        setSurfaceTextureListener(surfaceTextureListener);
    }

    private static final String TAG = "ArrowAnimationView";
    private float viewWidth,viewHeight;
    private float arrowWidth,arrowHeight;
    private Bitmap icArrow;
    private final SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            viewWidth = width;
            viewHeight = height;

            arrowWidth = (viewWidth / ARROW_NUMBER) + 15;
            arrowHeight = viewHeight;

            icArrow = ImageUtils.getBitmap(R.mipmap.icon_arrow_3,(int) arrowWidth, (int) arrowHeight);
            Log.d(TAG, "onSurfaceTextureAvailable: " + icArrow.getWidth() + " --- " + icArrow.getHeight());
            startAnim();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: " + width + " --- " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            stopAnim();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        }
    };

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if(visibility == View.VISIBLE && !isAniming()){
            startAnim();
        } else if((visibility == View.INVISIBLE || visibility == View.GONE) && isAniming()){
            stopAnim();
        }
    }

    private float leftOffset,y_position = 0.0f;
    private Disposable animDisposable;
    private void startAnim(){
        if(isAniming()){
            return;
        }
        Scheduler single = Schedulers.single();
        animDisposable = Observable.intervalRange(0,Long.MAX_VALUE,1,UPDATE_TIME, TimeUnit.MILLISECONDS)
                .subscribeOn(single)
                .observeOn(single)
                .doOnNext(aLong -> {
                    if(getVisibility() == View.GONE || getVisibility() == View.INVISIBLE){
                        return;
                    }
                    if(icArrow == null || viewHeight == 0 || arrowWidth == 0.0f){
                        return;
                    }
                    if(y_position == 0.0f){
                        //开始动画时先计算出y轴位置
                        y_position = BigDecimal.valueOf(getPivotY())
                                .subtract(
                                        BigDecimal.valueOf(icArrow.getHeight())
                                                .divide(BigDecimal.valueOf(2), RoundingMode.HALF_DOWN))
                                .floatValue();
//                        y_position = getPivotY() - ((float) icArrow.getHeight() / 2);
                    }
                    if(leftOffset == 0.0f){
                        leftOffset = BigDecimal.valueOf(viewWidth)
                                .divide(
                                        BigDecimal.valueOf(arrowWidth).multiply(BigDecimal.valueOf(ARROW_NUMBER))
                                        ,RoundingMode.HALF_DOWN)
                                .floatValue();
//                        leftOffset = viewWidth / (arrowWidth * ARROW_NUMBER);
                    }

                    Canvas canvas = lockCanvas();
                    if(canvas != null){
                        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                        long l = aLong % (ARROW_NUMBER + 1);
                        if(l != 0){
                            for (long i = 1; i <= l; i++) {
                                float left = arrowWidth * (i - 1);
                                left *= leftOffset;
                                canvas.drawBitmap(icArrow,left,y_position,paint);
                            }
                        }
                        unlockCanvasAndPost(canvas);
                    }
                })
                .subscribe();
    }

    private boolean isAniming(){
        return animDisposable != null && !animDisposable.isDisposed();
    }

    private void stopAnim(){
        if(animDisposable != null && !animDisposable.isDisposed()){
            animDisposable.dispose();
            animDisposable = null;
        }
    }
}
