package com.janev.chongqing_bus_app.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.BR;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.databinding.ItemSiteBinding;
import com.janev.chongqing_bus_app.system.UiEvent;

import java.util.ArrayList;
import java.util.List;

public class SiteListAdapter extends RecyclerView.Adapter<SiteListAdapter.Holder> {

    private final int MAX_NUMBER = 7;
    private final List<Site> data = new ArrayList<>();

    public List<Site> getData() {
        return data;
    }

    public void setData(List<Site> list){
        data.clear();
        if(list != null && !list.isEmpty()){
            data.addAll(list);
        }
        notifyDataSetChanged();

        UiMessageUtils.getInstance().send(UiEvent.EVENT_NEXT_SITE,"");
//        notifyItemRangeChanged(0,data.size() - 1);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_site, parent, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = parent.getWidth() / MAX_NUMBER;

        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bindData(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private static final String TAG = "SiteListAdapter";
    /**
     * 驶入
     * @param position
     */
    public void pullIn(RecyclerView recyclerView,int position){
        for (Site datum : data) {
            //当前站
            if(datum.getIndex() == position && datum.getStatus() != Site.ARRIVAL){
                Log.d(TAG, "pullIn: 到站：" + position);
                datum.arrival();
                recyclerView.smoothScrollToPosition(position);

                //得到下一站
                String nextSite = "";
                for (Site site : data) {
                    if (site.getIndex() == (position + 1)) {
                        nextSite = site.getName();
                    }
                }
                UiMessageUtils.getInstance().send(UiEvent.EVENT_SCREEN_BROAD_SITE,new String[]{datum.getName(),nextSite,String.valueOf(datum.isResponsive())});

                //右上角报站
                String siteName = datum.getName();
                if(!TextUtils.isEmpty(siteName)){
                    siteName = "本站：" + siteName;
                }
                UiMessageUtils.getInstance().send(UiEvent.EVENT_NEXT_SITE,siteName);
//                UiMessageUtils.getInstance().send(UiEvent.EVENT_NEXT_SITE,nextSite);
            }
            //到站之前
            else if(datum.getIndex() == (position - 1) && datum.getStatus() != Site.BEFORE_ARRIVAL){
                datum.beforeArrival();
            }
            //在此之前
            else if (datum.getIndex() < position && datum.getStatus() != Site.BEFORE) {
                datum.before();
            }
            //在此之后
            else if(datum.getIndex() > position && datum.getStatus() != Site.AFTER){
                datum.after();
            }
        }
    }

    /**
     * 驶出
     * @param position
     */
    public void pullOut(RecyclerView recyclerView,int position){
        //如果小于有效索引的最后一个数，则加一，如果大于等于有效索引最后一个数则等于最后一个
        position += 1;

        for (int i = 0; i < data.size(); i++) {
            Site site = data.get(i);
            //当前站
            if(site.getIndex() == position && site.getStatus() != Site.SOON){
                Log.d(TAG, "pullIn: 下一站：" + position);
                site.soon();
                recyclerView.smoothScrollToPosition(position);

                UiMessageUtils.getInstance().send(UiEvent.EVENT_SCREEN_BROAD_SITE,new String[]{"",site.getName(),String.valueOf(site.isResponsive())});

                String siteName = site.getName();
                if(!TextUtils.isEmpty(siteName)){
                    siteName = "下一站：" + siteName;
                }
                UiMessageUtils.getInstance().send(UiEvent.EVENT_NEXT_SITE,siteName);
            }
            //即将到站之前
            else if(site.getIndex() == (position - 1) && site.getStatus() != Site.BEFORE_SOON){
                site.beforeSoon();
            }
            //在此之前
            else if (site.getIndex() < position && site.getStatus() != Site.BEFORE) {
                site.before();
            }
            //在此之后
            else if(site.getIndex() > position && site.getStatus() != Site.AFTER){
                site.after();
            }
        }
    }

    public static class Holder extends RecyclerView.ViewHolder{

        public final ItemSiteBinding binding;

        public Holder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void bindData(Site site){
            binding.setSite(site);
            binding.tvSiteName.setText(site.getName());

            //初始化状态
            updatePosition(0,6);
        }

        public void updatePosition(int firstVisibleItemPosition, int lastVisibleItemPosition) {
            Site site = binding.getSite();
            if (getAdapterPosition() == firstVisibleItemPosition) {
                if(site.getStatus() != Site.AFTER){
                    binding.ivLeftArrow.setImageResource(R.mipmap.icon_left_arrow_gray);
                }
                binding.ivLeftArrow.setVisibility(View.VISIBLE);
                binding.ivRightArrow.setVisibility(View.GONE);
            } else if(getAdapterPosition() == lastVisibleItemPosition){
                binding.ivLeftArrow.setVisibility(View.GONE);
                binding.ivRightArrow.setVisibility(View.VISIBLE);
            } else {
                binding.ivLeftArrow.setVisibility(View.GONE);
                binding.ivRightArrow.setVisibility(View.GONE);
            }
        }
    }

    public static class Site extends BaseObservable {
        public static final int BEFORE = 0;
        public static final int AFTER = 1;
        public static final int ARRIVAL = 2;
        public static final int SOON = 3;
        public static final int BEFORE_ARRIVAL = 4;
        public static final int BEFORE_SOON = 5;
        private int status = SOON;

        private int index;
        private String name;
        private boolean isResponsive;

        private int leftLineResId;
        private int rightLineResId;

        private int dotResId;
        private boolean showAnim;

        public Site(int index, String name,boolean isResponsive) {
            this.index = index;
            this.name = name;
            this.isResponsive = isResponsive;
            after();
        }

        public boolean isResponsive() {
            return isResponsive;
        }

        @Bindable
        public int getIndex() {
            return index;
        }

        @Bindable
        public String getName() {
            return name;
        }

        @Bindable
        public int getLeftLineResId() {
            return leftLineResId;
        }

        public void setLeftLineResId(int leftLineResId) {
            this.leftLineResId = leftLineResId;
            notifyPropertyChanged(BR.leftLineResId);
        }

        @Bindable
        public int getRightLineResId() {
            return rightLineResId;
        }

        public void setRightLineResId(int rightLineResId) {
            this.rightLineResId = rightLineResId;
            notifyPropertyChanged(BR.rightLineResId);
        }

        @Bindable
        public int getDotResId() {
            return dotResId;
        }

        public void setDotResId(int dotResId) {
            this.dotResId = dotResId;
            notifyPropertyChanged(BR.dotResId);
        }

        @Bindable
        public boolean isShowAnim() {
            return showAnim;
        }

        public void setShowAnim(boolean showAnim) {
            this.showAnim = showAnim;
            notifyPropertyChanged(BR.showAnim);
        }

        @Bindable
        public int getStatus() {
            return status;
        }

        public void setStatus(int status){
            this.status = status;
            notifyPropertyChanged(BR.status);
        }

        /**
         * 之前的站
         * 灰色实心圆
         * 双灰色线
         */
        public void before(){
            setStatus(BEFORE);
            setDotResId(R.drawable.shape_circle_before);
            setLeftLineResId(R.drawable.shape_line_before);
            setRightLineResId(R.drawable.shape_line_before);
            setShowAnim(false);
        }

        /**
         * 到站上一站
         * 灰色实心圆
         * 双灰色线
         */
        public void beforeArrival(){
            Log.d(TAG, "beforeArrival: 上一站：" + getName());
            setStatus(BEFORE_ARRIVAL);
            setDotResId(R.drawable.shape_circle_before);
            setLeftLineResId(R.drawable.shape_line_before);
            setRightLineResId(R.drawable.shape_line_before);
            setShowAnim(false);
        }

        /**
         * 即将到站上一站
         * 灰色实心圆
         * 左灰右蓝
         */
        public void beforeSoon(){
            Log.d(TAG, "beforeSoon: 上一站：" + getName());
            setStatus(BEFORE_SOON);
            setDotResId(R.drawable.shape_circle_before);
            setLeftLineResId(R.drawable.shape_line_before);
            setRightLineResId(R.drawable.shape_line_after);
            setShowAnim(false);
        }


        /**
         * 之后的站
         * 蓝框圆
         * 双蓝色线
         */
        public void after(){
            setStatus(AFTER);
            if(this.isResponsive){
                setDotResId(R.mipmap.icon_responsive_dot);
            } else {
                setDotResId(R.drawable.shape_circle_after);
            }
            setLeftLineResId(R.drawable.shape_line_after);
            setRightLineResId(R.drawable.shape_line_after);
            setShowAnim(false);
        }

        /**
         * 下一站（即将进站）
         * 蓝色闪粉圆
         * 双蓝色线
         */
        public void soon(){
            setStatus(SOON);
            if(this.isResponsive){
                setDotResId(R.mipmap.icon_responsive_dot);
            } else {
                setDotResId(R.drawable.shape_circle_soon);
            }
            setLeftLineResId(R.drawable.shape_line_after);//左蓝
            setRightLineResId(R.drawable.shape_line_after);//右蓝
            setShowAnim(true);
        }

        /**
         * 到站
         * 绿色实心圆
         * 左灰右蓝
         */
        public void arrival(){
            setStatus(ARRIVAL);
            if(this.isResponsive){
                setDotResId(R.mipmap.icon_responsive_dot);
            } else {
                setDotResId(R.drawable.shape_circle_arrival);
            }
            setLeftLineResId(R.drawable.shape_line_before);//左灰
            setRightLineResId(R.drawable.shape_line_after);//右蓝
            setShowAnim(false);
        }
    }
}
