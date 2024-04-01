package com.janev.chongqing_bus_app.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.janev.chongqing_bus_app.BR;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.databinding.ItemMoreInfoBinding;

import java.util.ArrayList;
import java.util.List;

public class MoreInfoAdapter extends RecyclerView.Adapter<MoreInfoAdapter.Holder> {

    private final List<MoreInfo> list = new ArrayList<>();

    public void setData(List<MoreInfo> list){
        this.list.clear();
        if(list != null && !list.isEmpty()){
            this.list.addAll(list);
        }
        notifyItemRangeChanged(0,this.list.size() - 1);
    }

    public List<MoreInfo> getList() {
        return list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_more_info, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bindData(this.list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class Holder extends RecyclerView.ViewHolder{
        private ItemMoreInfoBinding binding;
        public Holder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bindData(MoreInfo moreInfo){
            binding.setMoreInfo(moreInfo);
        }
    }

    public static class MoreInfo extends BaseObservable{
        private String title;
        private String content;

        public MoreInfo(String title, String content) {
            this.title = title;
            this.content = content;
        }

        @Bindable
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
            notifyPropertyChanged(BR.title);
        }

        @Bindable
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
            notifyPropertyChanged(BR.content);
        }
    }
}
