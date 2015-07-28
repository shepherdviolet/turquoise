package sviolet.turquoise.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 路径工具
 * Created by S.Violet on 2015/7/28.
 */
public class DirectoryUtils {

    /**
     * 应用对应的缓存路径, 动态选择优先外部储存<br/>
     * 外部储存存在时, 返回/sdcard/Android/data/<application package>/cache/subDir
     * 外部储存不存在, 返回/data/data/<application package>/cache/subDir
     * @param context
     * @param subDir 子目录
     */
    public static File getCacheDir(Context context, String subDir) {
        File pathFile = getCacheDir(context);
        return new File(pathFile.getAbsolutePath() + File.separator + subDir);
    }

    /**
     * 应用对应的缓存路径, 动态选择优先外部储存<br/>
     * 外部储存存在时, 返回/sdcard/Android/data/<application package>/cache
     * 外部储存不存在, 返回/data/data/<application package>/cache
     * @param context
     */
    public static File getCacheDir(Context context) {
        File pathFile = getExternalCacheDir(context);
        if (pathFile == null){
            pathFile = getInnerCacheDir(context);
        }
        return pathFile;
    }

    /**
     * 应用对应的外部缓存路径, 若不存在返回null
     * @param context
     * @return /sdcard/Android/data/<application package>/cache
     */
    public static File getExternalCacheDir(Context context){
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir();
        }
        return null;
    }

    /**
     * 应用对应的内部缓存路径
     * @param context
     * @return /data/data/<application package>/cache
     */
    public static File getInnerCacheDir(Context context){
        return context.getCacheDir();
    }

}
