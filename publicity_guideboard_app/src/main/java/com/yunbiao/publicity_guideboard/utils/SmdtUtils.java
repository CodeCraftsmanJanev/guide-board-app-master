package com.yunbiao.publicity_guideboard.utils;

import android.app.smdt.SmdtManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.yunbiao.publicity_guideboard.App;

import java.io.File;

public class SmdtUtils {

    private SmdtManager smdtManager;

    private static final class Holder{
        public static final SmdtUtils INSTANCE = new SmdtUtils();
    }

    public static SmdtUtils getInstance(){
        return Holder.INSTANCE;
    }

    private SmdtUtils(){}

    private static final String TAG = "SmdtUtils";
    public Bitmap screenShot(Context context){
        if(smdtManager != null){
            Log.e(TAG, "screenShot: 1111111111111111111");
            return smdtManager.smdtScreenShot(context);
        }
        return null;
    }

    public void installApp(File file){
        if(smdtManager == null){
            smdtManager = SmdtManager.create(App.getContext());
        }
        if(smdtManager != null) {
            smdtManager.smdtSilentInstall(file.getPath(),App.getContext());
        }
    }

    public void screenShotToFile(String path,String name,Context context){
        if(smdtManager == null){
            smdtManager = SmdtManager.create(App.getContext());
        }
        if(smdtManager != null) {
            smdtManager.smdtTakeScreenshot(path, name, context);
        }
    }

    public int setTime(int year, int month, int day, int hourOfDay, int minute){
        if(smdtManager == null){
            smdtManager = SmdtManager.create(App.getContext());
        }
        if(smdtManager != null){
            return smdtManager.setTime(App.getContext(),year,month,day,hourOfDay,minute);
        }
        return -1;
    }
}
