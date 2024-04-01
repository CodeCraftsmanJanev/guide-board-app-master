package com.janev.chongqing_bus_app.tcp.task.resource;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.UiMessageUtils;
import com.janev.chongqing_bus_app.db.DaoManager;
import com.janev.chongqing_bus_app.db.Material;
import com.janev.chongqing_bus_app.db.Program;
import com.janev.chongqing_bus_app.db.Time;
import com.janev.chongqing_bus_app.system.Path;
import com.janev.chongqing_bus_app.system.UiEvent;
import com.janev.chongqing_bus_app.utils.L;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ResourceLocal {
    private static final String TAG = "ResourceLocal";
    private static Disposable disposable;

    public static void readFileFromStorage(){
        clearDownload();
        disposable = Observable
                .just(1)
                .map(i -> {
                    File dir = new File(Path.getBusResource());
                    List<File> files = FileUtils.listFilesInDir(dir);
                    Log.d(TAG, "11111 readFileFromStorage: " + files);
                    if(files.isEmpty()){
                        throw new Exception("暂无本地资源");
                    }

                    List<File> resourceFiles = FileUtils.listFilesInDirWithFilter(dir, file -> {
                        String fileExtension = FileUtils.getFileExtension(file);
                        Log.d(TAG, "readFileFromStorage: " + fileExtension);
                        return TextUtils.equals("json",fileExtension) || TextUtils.equals("JSON",fileExtension);
                    });
                    Log.d(TAG, "22222 readFileFromStorage: " + resourceFiles);
                    if(resourceFiles.isEmpty()){
                        throw new Exception("资源文件不存在");
                    }

                    File resourceFile = resourceFiles.get(0);
                    Log.d(TAG, "33333 readFileFromStorage: " + resourceFile);
                    if(resourceFile == null){
                        throw new Exception("资源文件不存在");
                    }

                    String s = FileIOUtils.readFile2String(resourceFile);
                    Log.d(TAG, "44444 readFileFromStorage: " + s);
                    if(TextUtils.isEmpty(s)){
                        throw new Exception("资源文件为空");
                    }

                    return resourceFile;
                })
                .map(new ResourceDataResolver())
                .map(data -> {
                    d("解析后: " + data.toString());
                    //加库
                    saveDatabase(data.getPrograms(),data.getMaterials(),data.getTimes());
                    return data;
                })
                .map(data -> {
                    File dir = new File(Path.getBusResource());

                    List<Material> materials = data.getMaterials();
                    if (materials != null && !materials.isEmpty()) {
                        for (Material material : materials) {

                            //检查目标文件
                            File targetFile = new File(Path.getMaterialPath(material));
                            d("apply: 目标文件：" + targetFile.getPath());
                            if(FileUtils.isFileExists(targetFile)){
                                continue;
                            }

                            //检查源文件
                            File sourceFile = new File(Path.getLocalMaterialPath(dir,material));
                            d("apply: 源文件：" + targetFile.getPath());
                            if(!FileUtils.isFileExists(sourceFile)){
                                continue;
                            }

                            boolean copy = FileUtils.copy(sourceFile, targetFile, (srcFile, destFile) -> true);
                            d("apply: 复制结果：" + copy);
                        }
                    }
                    return data;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {

                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                d(throwable.getMessage());
                            }
                        },
                        new Action() {
                            @Override
                            public void run() throws Exception {

                            }
                        }
                );





    }

    private static void clearDownload(){
        if(disposable != null && !disposable.isDisposed()){
            disposable.dispose();
            disposable = null;
        }
    }


    private static void saveDatabase(List<Program> programs, List<Material> materials, List<Time> times){
        clearDownload();

        DaoManager.get().deleteAll(Program.class);
        DaoManager.get().deleteAll(Material.class);
        DaoManager.get().deleteAll(Time.class);

        if(!programs.isEmpty()) DaoManager.get().addOrUpdatePrograms(programs);
        if(!materials.isEmpty()) DaoManager.get().addOrUpdateMaterials(materials);
        if(!times.isEmpty()) DaoManager.get().addOrUpdateTimes(times);

        UiMessageUtils.getInstance().send(UiEvent.UPDATE_RESOURCE);
    }


    private static void d(String l){
        L.tcp(TAG,l);
    }
}
