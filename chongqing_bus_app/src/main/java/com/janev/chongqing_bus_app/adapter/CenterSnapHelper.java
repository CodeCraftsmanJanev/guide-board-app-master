package com.janev.chongqing_bus_app.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.janev.chongqing_bus_app.R;

public class CenterSnapHelper extends LinearSnapHelper {
    private OrientationHelper mVerticalHelper;
    private OrientationHelper mHorizontalHelper;

    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(layoutManager, targetView,
                    getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }

        if(layoutManager.canScrollVertically()){
            out[1] = distanceToCenter(layoutManager, targetView,
                    getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }

        return out;
    }
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null || mVerticalHelper.getLayoutManager() != layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null || mHorizontalHelper.getLayoutManager() != layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }
    private int distanceToCenter(RecyclerView.LayoutManager layoutManager,View targetView,OrientationHelper helper){
        int childCenter = helper.getDecoratedStart(targetView)
                + helper.getDecoratedMeasurement(targetView);
        int containerCenter = helper.getStartAfterPadding();

        return childCenter - containerCenter;
    }
}
