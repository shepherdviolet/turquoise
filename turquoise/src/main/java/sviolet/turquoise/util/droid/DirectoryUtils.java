/*
 * Copyright (C) 2015-2017 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.util.droid;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 路径工具
 * Created by S.Violet on 2015/7/28.
 */
public class DirectoryUtils {

    /**
     * 获得默认的外部存储根路径
     */
    public static File getExternalStorageDirectory(){
        return Environment.getExternalStorageDirectory();
    }

    /************************
     * 应用文件目录
     */

    /**
     * 应用对应外部文件路径
     * @param context context
     * @return nullable, 可能为空!
     */
    @Nullable
    public static File getExternalFilesDir(Context context){
        return context.getExternalFilesDir(null);
    }

    /**
     * 应用对应外部文件路径
     * @param context context
     * @param type Environment.DIRECTORY_...
     * @return nullable, 可能为空!
     */
    @Nullable
    public static File getExternalFilesDir(Context context, String type){
        return context.getExternalFilesDir(type);
    }

    /**
     * 应用对应数据路径/data/data/<application package>/
     * @param context context
     */
    public static File getDataDir(Context context){
        return new File(context.getApplicationInfo().dataDir);
    }

    /**
     * 应用源码路径 /data/app/<application package>/base.apk
     * @param context context
     */
    public static File getSourceDir(Context context){
        return new File(context.getApplicationInfo().sourceDir);
    }

    /**
     * 应用native库路径 /data/app/<application package>/lib/arm
     * @param context context
     */
    public static File getNativeLibraryDir(Context context){
        return new File(context.getApplicationInfo().nativeLibraryDir);
    }

    /**
     * 打开assets下的文件.
     * URL方式获取(不能用于new File()) file:///android_asset/...
     * @param context context
     * @param fileName 文件名(路径), 例如"htmls/index.html"
     * @return InputStream
     * @throws IOException IO异常
     */
    public static InputStream openAssets(Context context, String fileName) throws IOException {
        return context.getAssets().open(fileName);
    }

    /************************
     * 缓存目录
     */

    /**
     * 应用对应的缓存路径, 动态选择优先外部储存<br/>
     * 外部储存存在时, 返回/sdcard/Android/data/<application package>/cache/subDir
     * 外部储存不存在, 返回/data/data/<application package>/cache/subDir
     * @param context context
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
     * @param context context
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
     * @param context context
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
     * @param context context
     * @return /data/data/<application package>/cache
     */
    public static File getInnerCacheDir(Context context){
        return context.getCacheDir();
    }

}
