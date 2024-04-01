package com.yunbiao.publicity_guideboard.ui;

import android.app.ProgressDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.yunbiao.publicity_guideboard.R;
import com.yunbiao.publicity_guideboard.databinding.FragmentSettingBinding;
import com.yunbiao.publicity_guideboard.system.Cache;
import com.yunbiao.publicity_guideboard.system.Path;

public class SettingFragment extends BaseFragment<FragmentSettingBinding>{
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_setting;
    }

    @Override
    protected void initView() {
        String userName = Cache.getString(Cache.Key.USER_NAME,Cache.Default.USER_NAME);
        String password = Cache.getString(Cache.Key.PASSWORD,Cache.Default.PASSWORD);
        String busCode = Cache.getString(Cache.Key.BUS_CODE);
        int imageTimeIndex = Cache.getInt(Cache.Key.IMAGE_TIME_INDEX,Cache.Default.IMAGE_TIME_INDEX);
        boolean debug = Cache.getBoolean(Cache.Key.DEBUG);

        //用户名
        binding.edtUserName.setText(userName);
        binding.ivClearUserName.setOnClickListener(v -> binding.edtUserName.setText(""));
        //密码
        binding.edtPassword.setText(password);
        binding.edtPassword.setOnClickListener(v -> binding.edtPassword.setText(""));
        //车号
        binding.edtBusCode.setText(busCode);
        binding.edtBusCode.setOnClickListener(v -> binding.edtBusCode.setText(""));
        //图片播放时长
        int[] intArray = getResources().getIntArray(R.array.image_time);
        String[] stringArray = new String[intArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = intArray[i] + "秒";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),R.layout.myspiner,stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spImageTime.setAdapter(adapter);
        binding.spImageTime.setSelection(imageTimeIndex);
        //收集日志
        binding.swLog.setChecked(debug);
        //保存按钮
        binding.btnConfirm.setOnClickListener(v -> {
            String inputUserName = binding.edtUserName.getText().toString();
            String inputPassword = binding.edtPassword.getText().toString();
            String inputBusCode = binding.edtBusCode.getText().toString();
            int selectedPosition = binding.spImageTime.getSelectedItemPosition();
            boolean isDebug = binding.swLog.isChecked();

            if(TextUtils.equals(userName,inputUserName)//用户名未改变
                    && TextUtils.equals(password,inputPassword)//密码未改变
                    && TextUtils.equals(busCode,inputBusCode)//车号未改变
                    && selectedPosition == imageTimeIndex //图片播放时长索引未改变
                    && isDebug == debug//log未改变
            ){
                requireActivity().onKeyDown(KeyEvent.KEYCODE_BACK,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK));
                return;
            }

            Cache.setString(Cache.Key.USER_NAME,inputUserName);
            Cache.setString(Cache.Key.PASSWORD,inputPassword);
            Cache.setString(Cache.Key.BUS_CODE,inputBusCode);
            Cache.setInt(Cache.Key.IMAGE_TIME_INDEX,selectedPosition);
            Cache.setBoolean(Cache.Key.DEBUG, isDebug);
            //如果调整了日志设置则清除日志文件
            if(isDebug != debug){
                boolean b = FileUtils.deleteAllInDir(Path.getLogPath());
                Log.d(TAG, "调整了日志设置，清除所有日志");
            }

            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            AppUtils.relaunchApp(true);
        });
    }

    @Override
    protected void initData() {

    }
}
