package cn.edu.bit.cs.explorer.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserHandle;
import android.os.storage.StorageManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by entalent on 2015/11/10.
 */
public class StorageUtil {

    static String[] UNIT_STR = {
            "B", "KB", "MB", "GB", "TB"
    };

    /**
     *
     * @param context
     * @return all available storage volumes on the device
     */
    public static final ArrayList<StorageVolumeInfo> getVolumePaths(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Method _getVolumePaths;
        ArrayList<StorageVolumeInfo> paths = new ArrayList<>();
        try {
            _getVolumePaths = storageManager.getClass().getMethod("getVolumePaths");
            String[] ret = (String[]) _getVolumePaths.invoke(storageManager);
            for (String i : ret) {
                StorageVolumeInfo info = new StorageVolumeInfo(i);
                if(info.isAvailableVolume())
                    paths.add(info);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (paths.size() == 0) {
            StorageVolumeInfo info = new StorageVolumeInfo(Environment.getExternalStorageDirectory().getAbsolutePath());
            if(info.isAvailableVolume())
                paths.add(info);
        }
        return paths;
    }

    public static String formatSizeStr(long sizeLong){
        double size = sizeLong;
        int unit = 0;
        while(unit < UNIT_STR.length && size >= 1024){
            size /= 1024.0;
            unit++;
        }
        return String.format("%.2f %s", size, UNIT_STR[unit]);
    }

}
