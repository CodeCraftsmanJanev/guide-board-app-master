package com.janev.chongqing_bus_app.tcp;

import android.os.Environment;
import android.os.StatFs;

import com.blankj.utilcode.util.FileUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.system.Path;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StorageChecker {

    public static void checkAndDelete(long size){
        List<File> unusedFiles = null;

        long availableStorageSpace = getAvailableStorageSpace();
        if(availableStorageSpace < size){
            List<String> pathList = new ArrayList<>();
            List<Material> query = DaoManager.get().query(Material.class);
            for (Material material : query) {
                String materialPath = Path.getMaterialPath(material);
                pathList.add(materialPath);
            }
            unusedFiles = FileUtils.listFilesInDirWithFilter(Path.getMaterialDir(), pathname -> !pathList.contains(pathname.getPath()));
        }

        if(unusedFiles != null && !unusedFiles.isEmpty()){
            Iterator<File> iterator = unusedFiles.iterator();
            while (iterator.hasNext() && availableStorageSpace <= size){
                FileUtils.delete(iterator.next());
                iterator.remove();
                availableStorageSpace = getAvailableStorageSpace();
            }
        }
    }

    public static long getAvailableStorageSpace() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            long blockSize = statFs.getBlockSizeLong();
            long availableBlocks = statFs.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        }
        return 0;
    }
}
