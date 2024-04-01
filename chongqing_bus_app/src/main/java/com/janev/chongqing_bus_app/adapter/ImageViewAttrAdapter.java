package com.janev.chongqing_bus_app.adapter;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;

public class ImageViewAttrAdapter {

    @BindingAdapter("android:src")
    public static void setSrc(ImageView imageView,int resId){
        imageView.setImageResource(resId);
    }

    @BindingAdapter("android:background")
    public static void setBackground(View view, int resId){
        view.setBackgroundResource(resId);
    }
}
