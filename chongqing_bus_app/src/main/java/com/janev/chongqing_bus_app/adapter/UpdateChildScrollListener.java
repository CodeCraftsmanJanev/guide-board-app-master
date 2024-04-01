package com.janev.chongqing_bus_app.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class UpdateChildScrollListener extends RecyclerView.OnScrollListener {
    @Override
    public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if(manager == null){
            return;
        }
        SiteListAdapter adapter = (SiteListAdapter) recyclerView.getAdapter();
        if(adapter == null){
            return;
        }
        int itemCount = adapter.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            View childAt = recyclerView.getChildAt(i);
            if(childAt == null){
                continue;
            }
            SiteListAdapter.Holder holder = (SiteListAdapter.Holder) recyclerView.getChildViewHolder(childAt);
            if(holder == null){
                continue;
            }
            holder.updatePosition(manager.findFirstVisibleItemPosition(),manager.findLastVisibleItemPosition());
        }
    }
}
