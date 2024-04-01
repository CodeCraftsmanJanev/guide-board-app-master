package com.janev.chongqing_bus_app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.janev.chongqing_bus_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScrollTextView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "ScrollTextView";

    private SurfaceHolder surfaceHolder;

    private Paint paint;
    private boolean stopScroll = false;
    private boolean pauseScroll = false;

    private boolean clickEnable = false;
    private boolean isHorizontal = true;
    private int speed = 2;
    private String text = "";
    private float textSize = 20f;
    private int textColor = Color.BLACK;
    private int textBackColor = Color.WHITE;

    private int needScrollTimes = Integer.MAX_VALUE;

    private int viewWidth = 0;
    private int viewHeight = 0;
    private float textWidth = 0f;
    private float textX = 0f;
    private float textY = 0f;

    private ScheduledExecutorService scheduledExecutorService;

    boolean isSetNewText = false;
    boolean isScrollForever = true;

    public ScrollTextView(Context context) {
        this(context,null);
    }

    public ScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
        paint = new Paint();
        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.ScrollTextView);
        clickEnable = arr.getBoolean(R.styleable.ScrollTextView_clickEnable, clickEnable);
        isHorizontal = arr.getBoolean(R.styleable.ScrollTextView_isHorizontal, isHorizontal);
        speed = arr.getInteger(R.styleable.ScrollTextView_speed, speed);
        text = arr.getString(R.styleable.ScrollTextView_text);
        textColor = arr.getColor(R.styleable.ScrollTextView_text_color, Color.BLACK);
        textSize = arr.getDimension(R.styleable.ScrollTextView_text_size, textSize);
        needScrollTimes = arr.getInteger(R.styleable.ScrollTextView_times, Integer.MAX_VALUE);
        isScrollForever = arr.getBoolean(R.styleable.ScrollTextView_isScrollForever, true);
        startAndEndPauseTime = getId() == R.id.tv_line_name ? 1000 : 3000;

        paint.setColor(textColor);
        paint.setTextSize(textSize);

        setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        setFocusable(true);
        arr.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int mHeight = getFontHeight(textSize);
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        // when layout width or height is wrap_content ,should init ScrollTextView Width/Height
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(viewWidth, mHeight);
            viewHeight = mHeight;
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(viewWidth, viewHeight);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(viewWidth, mHeight);
            viewHeight = mHeight;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        stopScroll = false;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new ScrollTextThread(), 100, 100, TimeUnit.MILLISECONDS);
        Log.d(TAG, "ScrollTextTextView is created");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "arg0:" + holder.toString() + "  arg1:" + format + "  arg2:" + width + "  arg3:" + height);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopScroll = true;
        scheduledExecutorService.shutdownNow();
        Log.d(TAG, "ScrollTextTextView is destroyed");
    }

    /**
     * text height
     *
     * @param fontSize fontSize
     * @return fontSize`s height
     */
    private int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.ascent);
    }

    /**
     * get Background color
     *
     * @return textBackColor
     */
    public int getBackgroundColor(){
        return textBackColor;
    }

    /**
     * set background color
     *
     * @param color textBackColor
     */
    public void setScrollTextBackgroundColor(int color){
        this.setBackgroundColor(color);
        this.textBackColor=color;
    }


    /**
     * get speed
     *
     * @return speed
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * get Text
     *
     * @return
     */
    public String getText() {
        return text;
    }

    /**
     * get text size
     *
     * @return  px
     */
    public float getTextSize() {
        return px2sp(this.getContext(),textSize);
    }


    /**
     * get text color
     *
     * @return textColor
     */
    public int getTextColor() {
        return textColor;
    }


    /**
     * set scroll times
     *
     * @param times scroll times
     */
    public void setTimes(int times) {
        if (times <= 0) {
            throw new IllegalArgumentException("times was invalid integer, it must between > 0");
        } else {
            needScrollTimes = times;
            isScrollForever = false;
        }
    }


    /**
     * set scroll text size SP
     *
     * @param textSizeTem scroll times
     */
    public void setTextSize(float textSizeTem) {
        if (textSize < 20) {
            throw new IllegalArgumentException("textSize must  > 20");
        } else if (textSize > 900) {
            throw new IllegalArgumentException("textSize must  < 900");
        } else {

            this.textSize=sp2px(getContext(), textSizeTem);
            //重新设置Size
            paint.setTextSize(textSize);
            //试图区域也要改变
            measureVarious();

            //实际的视图高,thanks to WG
            int mHeight = getFontHeight(textSizeTem);
            ViewGroup.LayoutParams lp = this.getLayoutParams();
            lp.width = viewWidth;
            lp.height = dip2px(this.getContext(), mHeight);
            this.setLayoutParams(lp);

            isSetNewText = true;
        }
    }

    /**
     * dp to px
     *
     * @param context c
     * @param dpValue dp
     * @return
     */
    private int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * sp to px
     *
     * @param context c
     * @param spValue sp
     * @return
     */
    private int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    public  int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * isHorizontal or vertical
     *
     * @param horizontal isHorizontal or vertical
     */
    public void setHorizontal(boolean horizontal) {
        isHorizontal = horizontal;
    }

    /**
     * set scroll text
     *
     * @param newText scroll text
     */

    public void setText(String newText) {
        isSetNewText = true;
        stopScroll = false;
        this.text = newText;
        measureVarious();
    }

    /**
     * Set the text color
     *
     * @param color A color value in the form 0xAARRGGBB.
     */
    public void setTextColor(@ColorInt int color) {
        textColor = color;
        paint.setColor(textColor);
    }

    /**
     * set scroll speed
     *
     * @param speed SCROLL SPEED [4,14] ///// 0?
     */
    public void setSpeed(int speed) {
        if (speed > 14 || speed < 4) {
            throw new IllegalArgumentException("Speed was invalid integer, it must between 4 and 14");
        } else {
            this.speed = speed;
        }
    }

    /**
     * scroll text forever
     *
     * @param scrollForever scroll forever or not
     */
    public void setScrollForever(boolean scrollForever) {
        isScrollForever = scrollForever;
    }


    public boolean isPauseScroll() {
        return pauseScroll;
    }

    public void setPauseScroll(boolean pauseScroll) {
        this.pauseScroll = pauseScroll;
    }

    /**
     * touch to stop / start
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!clickEnable) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pauseScroll = !pauseScroll;
                break;
        }
        return true;
    }

    /**
     * scroll text vertical
     */
    private void drawVerticalScroll() {
        List<String> strings = new ArrayList<>();
        int start = 0, end = 0;
        while (end < text.length()) {
            while (paint.measureText(text.substring(start, end)) < viewWidth && end < text.length()) {
                end++;
            }
            if (end == text.length()) {
                strings.add(text.substring(start, end));
                break;
            } else {
                end--;
                strings.add(text.substring(start, end));
                start = end;
            }
        }

        float fontHeight = paint.getFontMetrics().bottom - paint.getFontMetrics().top;

        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        float baseLine = viewHeight / 2 + distance;

        for (int n = 0; n < strings.size(); n++) {
            for (float i = viewHeight + fontHeight; i > -fontHeight; i = i - 3) {
                if (stopScroll || isSetNewText) {
                    return;
                }

                if (pauseScroll) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.toString());
                    }
                    continue;
                }

                Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawText(strings.get(n), 0, i, paint);
                surfaceHolder.unlockCanvasAndPost(canvas);

                if (i - baseLine < 4 && i - baseLine > 0) {
                    if (stopScroll) {
                        return;
                    }
                    try {
                        Thread.sleep(speed * 1000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
        }
    }

    /**
     * Draw text
     *
     * @param X X
     * @param Y Y
     */
    private synchronized void draw(float X, float Y) {
        Canvas canvas = surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawText(text, X, Y, paint);
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        this.setVisibility(visibility);
    }

    private boolean isStartAndEndPause = true; //是否在开头和结尾时停顿
    private int xOffset = 80;//
    long startAndEndPauseTime = 3000;
    float startX,endX;
    /**
     * measure text
     */
    private void measureVarious() {
        //计算文字宽度
        textWidth = paint.measureText(text);
        if(textWidth <= getWidth()){
            startX = viewWidth;
            endX = textWidth;
        } else if(isStartAndEndPause){
            //如果需要停顿，则终点设置为文字宽度
            endX = textWidth + xOffset;
            //起点设置为控件宽度
            startX = viewWidth - xOffset;
        } else {
            //控件宽度+文字宽度=滚动终点
            endX = viewWidth + textWidth;
            //设置滚动起点
            startX = viewWidth - viewWidth / 5;
        }
        textX = startX;

        //baseline measure !
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
        textY = viewHeight / 2 + distance;
    }

    private Runnable finishRunnable;
    public void setOnFinishRunnable(Runnable runnable) {
        this.finishRunnable = runnable;
    }

    /**
     * Scroll thread
     *
     */
    class ScrollTextThread implements Runnable {
        @Override
        public void run() {

            measureVarious();

            while (!stopScroll) {
                // NoNeed Scroll，短文不滚动，居中 ？暂时不支持吧
//                if (textWidth < getWidth()) {
//                    draw(1, textY);
//                    sleep(500);
//                    continue;
//                }

                //横向滚动
                if (isHorizontal) {
                    //暂停
                    if (pauseScroll) {
                        sleep(500);
                        continue;
                    }

                    //宽度-文字的X为向左滚动
                    draw(viewWidth - textX, textY);
                    //计算滚动值
                    if(textWidth <= viewWidth){
                        sleep(500);
                        continue;
                    }

                    //起点停顿
                    if(textX == startX && isStartAndEndPause){
                        sleep(startAndEndPauseTime);
                    }

                    textX += speed;

                    //如果X大于边界则归零
                    if (textX > endX) {
                        --needScrollTimes;
                        if(isStartAndEndPause){
                            //以控件宽度作为起点
                            textX = startX;
                            sleep(startAndEndPauseTime);
                        } else {
                            textX = 0;
                        }
                        if(finishRunnable != null){
                            finishRunnable.run();
                        }
                    }
                } else {
                    drawVerticalScroll();
                    isSetNewText = false;
                    --needScrollTimes;
                }

                //如果滚动次数为0则不滚动
                if (needScrollTimes <= 0 && isScrollForever) {
                    stopScroll = true;
                }

            }
        }
    }

    private void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString());
        }
    }
}
