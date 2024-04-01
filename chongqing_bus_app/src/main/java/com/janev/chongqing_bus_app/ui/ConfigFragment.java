package com.janev.chongqing_bus_app.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.SeekBar;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.R;
import com.janev.chongqing_bus_app.alive.KeepAlive;
import com.janev.chongqing_bus_app.databinding.FragmentConfigBinding;
import com.janev.chongqing_bus_app.system.Agreement;
import com.janev.chongqing_bus_app.system.Cache;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.tcp.message.MessageUtils;

import java.util.List;

public class ConfigFragment extends BaseFragment<FragmentConfigBinding>{
    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_config;
    }

    private Runnable onClickMoreRunnable;
    private Runnable onClickConnectionRunnable;

    public void setOnClickMoreRunnable(Runnable onClickMoreRunnable) {
        this.onClickMoreRunnable = onClickMoreRunnable;
    }

    public void setOnClickConnectionRunnable(Runnable onClickConnectionRunnable) {
        this.onClickConnectionRunnable = onClickConnectionRunnable;
    }

    @Override
    protected void initView() {
        int agreementOrdinal = Cache.getInt(Cache.Key.AGREEMENT_ORDINAL, Cache.Default.AGREEMENT_ORDINAL);
        String productNumber = MessageUtils.getProductNumberHex();
        String authNumber = MessageUtils.getAuthNumberHex();
        String deviceNumber = MessageUtils.getDeviceNumber();

        boolean al = false;
        if(FileUtils.isFileExists(KeepAlive.AUTO_LAUNCH_FLAG)){
            String s = FileIOUtils.readFile2String(KeepAlive.AUTO_LAUNCH_FLAG);
            al = !TextUtils.isEmpty(s) && TextUtils.equals("1",s);
        }
        boolean autoLaunch = al;

        //协议类型
        List<String> strings = Agreement.toNameList();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(requireContext(),R.layout.myspiner,strings);
        binding.spSerport.setAdapter(arrayAdapter);
        binding.spSerport.setSelection(agreementOrdinal);

        //音量
        binding.sbVolume.setMax(100);
        binding.sbVolume.setProgress(MessageUtils.getVolumePercent());
        binding.sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    MessageUtils.setVolumePercent(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //设置厂商编码和授权码
        binding.edtProductNumber.setText(productNumber);
        binding.edtAuthNumber.setText(authNumber);
        binding.edtDeviceNumber.setText(deviceNumber);
        binding.edtProductNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputAuthNumber = binding.edtAuthNumber.getText().toString();
                String inputDeviceNumber = binding.edtDeviceNumber.getText().toString();
                String inputProductNumber = s.toString();
                if(inputProductNumber.length() > 0 && inputProductNumber.length() < 10){
                    binding.edtProductNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else if(inputAuthNumber.length() > 0 && inputAuthNumber.length() < 32){
                    binding.edtAuthNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else if(inputDeviceNumber.length() > 0 && inputDeviceNumber.length() < 12){
                    binding.edtDeviceNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else {
                    binding.btnConfirm.setEnabled(true);
                }
            }
        });
        binding.edtAuthNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputProductNumber = binding.edtProductNumber.getText().toString();
                String inputDeviceNumber = binding.edtDeviceNumber.getText().toString();
                String inputAuthNumber = s.toString();
                if(inputAuthNumber.length() > 0 && inputAuthNumber.length() < 32){
                    binding.edtAuthNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else if(inputProductNumber.length() > 0 && inputProductNumber.length() < 10){
                    binding.edtProductNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                }  else if(inputDeviceNumber.length() > 0 && inputDeviceNumber.length() < 12){
                    binding.edtDeviceNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else {
                    binding.btnConfirm.setEnabled(true);
                }
            }
        });
        binding.edtDeviceNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputAuthNumber = binding.edtAuthNumber.getText().toString();
                String inputProductNumber = binding.edtProductNumber.getText().toString();
                String inputDeviceNumber = s.toString();
                if(inputDeviceNumber.length() > 0 && inputDeviceNumber.length() < 12){
                    binding.edtDeviceNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else if(inputAuthNumber.length() > 0 && inputAuthNumber.length() < 32){
                    binding.edtAuthNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else if(inputProductNumber.length() > 0 && inputProductNumber.length() < 10){
                    binding.edtProductNumber.setError("格式错误");
                    binding.btnConfirm.setEnabled(false);
                } else {
                    binding.btnConfirm.setEnabled(true);
                }
            }
        });

        String productDate = MessageUtils.getProductDate();
        binding.tvProductDate.setText(productDate);
        binding.tvProductDate.setOnClickListener(v -> {
            int year = Integer.parseInt(productDate.substring(0, 4));
            int month = Integer.parseInt(productDate.substring(4, 6));
            int day = Integer.parseInt(productDate.substring(6));
            new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
                String yearStr = String.valueOf(year1);
                while (yearStr.length() < 4) {
                    yearStr = "0" + yearStr;
                }
                String monthStr = String.valueOf(month1 + 1);
                while (monthStr.length() < 2) {
                    monthStr = "0" + monthStr;
                }
                String dayStr = String.valueOf(dayOfMonth);
                while (dayStr.length() < 2) {
                    dayStr = "0" + dayStr;
                }
                String d = yearStr + monthStr + dayStr;
                MessageUtils.setProductDate(d);
                binding.tvProductDate.setText(d);
                Log.e(TAG, "onDateSet: " + d);
            },year,month - 1,day).show();


        });

        //图片播放时长
        int[] intArray = getResources().getIntArray(R.array.image_time);
        String[] stringArray = new String[intArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            stringArray[i] = intArray[i] + "秒";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),R.layout.myspiner,stringArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //收集日志
//        binding.swLog.setChecked(debug);

        //自动启动
        binding.swAutoLaunch.setChecked(autoLaunch);

        //保存按钮
        binding.btnConfirm.setOnClickListener(v -> {
            int selectAgreement = binding.spSerport.getSelectedItemPosition();
            String inputProductNumber = binding.edtProductNumber.getText().toString();
            String inputAuthNumber = binding.edtAuthNumber.getText().toString();
            String inputDeviceNumber = binding.edtDeviceNumber.getText().toString();
            boolean isDebug = binding.swLog.isChecked();
            boolean isAutoLaunch = binding.swAutoLaunch.isChecked();

            if(agreementOrdinal == selectAgreement //协议类型
//                    && isDebug == debug//log未改变
                    && isAutoLaunch == autoLaunch//自启未改变
                    && TextUtils.equals(productNumber,inputProductNumber)
                    && TextUtils.equals(authNumber,inputAuthNumber)
                    && TextUtils.equals(deviceNumber,inputDeviceNumber)
            ){
                requireActivity().onKeyDown(KeyEvent.KEYCODE_BACK,new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK));
                return;
            }
            //设置协议类型
            Cache.setInt(Cache.Key.AGREEMENT_ORDINAL,selectAgreement);

            //设置厂商编码
            if(TextUtils.isEmpty(inputProductNumber)){
                MessageUtils.removeProductNumber();
            } else {
                MessageUtils.setProductNumber(inputProductNumber);
            }

            //设置厂商授权码
            if(TextUtils.isEmpty(inputAuthNumber)){
                MessageUtils.removeAuthNumber();
            } else {
                MessageUtils.setAuthNumber(inputAuthNumber);
            }

            //设置设备编号
            if(TextUtils.isEmpty(inputDeviceNumber)){
                MessageUtils.removeDeviceNumber();
            } else {
                MessageUtils.setDeviceNumber(inputDeviceNumber);
            }

            //自启开关
            if(FileUtils.isFileExists(KeepAlive.AUTO_LAUNCH_FLAG)){
                boolean b = FileIOUtils.writeFileFromString(KeepAlive.AUTO_LAUNCH_FLAG, isAutoLaunch ? "1" : "0", false);
                Log.d(TAG, "initView: 写入结果：" + b);
            }
            if(isAutoLaunch != autoLaunch){
                Log.d(TAG, "调整了自启设置");
            }

            //重启APP
            ProgressDialog progressDialog = new ProgressDialog(requireContext());
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            AppUtils.relaunchApp(true);
        });

        binding.btnMore.setOnClickListener(v -> {
            if(onClickMoreRunnable != null){
                onClickMoreRunnable.run();
            }
        });

        binding.tvConnection.setOnClickListener(v -> {
            if(onClickConnectionRunnable != null){
                onClickConnectionRunnable.run();
            }
        });
    }

    @Override
    protected void initData() {

    }
}
