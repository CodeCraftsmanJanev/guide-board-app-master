package com.yunbiao.publicity_guideboard.ui;

import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.databinding.FragmentPublicityBinding;
import com.yunbiao.publicity_guideboard.db.Advert;
import com.yunbiao.publicity_guideboard.db.DaoManager;
import com.yunbiao.publicity_guideboard.utils.PublicityManager;

import java.util.Iterator;
import java.util.List;

public class PublicityFragment extends BaseFragment<FragmentPublicityBinding>{
    private static final String TAG = "PublicityFragment";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_publicity;
    }

    @Override
    protected void initView() {
        UiMessageUtils.getInstance().addListener(PublicityManager.SET_CACHE,setCacheListener);
        UiMessageUtils.getInstance().addListener(PublicityManager.RESET,resetListener);
        UiMessageUtils.getInstance().addListener(PublicityManager.ADD_ADVERT,addAdvertListener);
        UiMessageUtils.getInstance().addListener(PublicityManager.REMOVE_ADVERT,removeAdvertListener);

        getLifecycle().addObserver(PublicityManager.getInstance());
    }

    @Override
    protected void initData() {

    }

    private final UiMessageUtils.UiMessageCallback resetListener = localMessage -> {
        Log.d(TAG, "重置播放列表");
        binding.publicityView.reset();
    };

    private final UiMessageUtils.UiMessageCallback addAdvertListener = localMessage -> {
        Advert advert = DaoManager.get().queryAdvertByNumber(localMessage.getObject().toString());
        Log.d(TAG, "插入数据：" + advert.getLocalPath());
        binding.publicityView.addAdvert(advert);
    };

    private final UiMessageUtils.UiMessageCallback removeAdvertListener = localMessage -> {
        String advertNumber = localMessage.getObject().toString();
        Log.d(TAG, "删除数据：" + advertNumber);
        binding.publicityView.removeAdvert(advertNumber);
    };

    private final UiMessageUtils.UiMessageCallback setCacheListener = localMessage -> {
        Log.d(TAG, "加载缓存");
        List<Advert> query = DaoManager.get().query(Advert.class);
        for (Advert advert : query) {
            if(FileUtils.isFileExists(advert.getLocalPath())){
                Log.d(TAG, "插入数据：" + advert.getLocalPath());
                binding.publicityView.addAdvert(advert);
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLifecycle().removeObserver(PublicityManager.getInstance());
    }
}
