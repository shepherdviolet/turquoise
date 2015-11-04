/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.utils.bitmap.loader.handler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import sviolet.turquoise.utils.bitmap.loader.BitmapLoader;
import sviolet.turquoise.utils.sys.DeviceUtils;

/**
 * 磁盘缓存异常处理器默认实现<p/>
 *
 * //TODO 实现完善 注释完善
 *
 * Created by S.Violet on 2015/11/4.
 */
public class DefaultDiskCacheExceptionHandler implements DiskCacheExceptionHandler {

    private Runnable onViewRefreshListener;

    public DefaultDiskCacheExceptionHandler() {

    }

    public DefaultDiskCacheExceptionHandler(Runnable onViewRefreshListener) {
        this.onViewRefreshListener = onViewRefreshListener;
    }

    @Override
    public void onCacheOpenException(final Context context, final BitmapLoader bitmapLoader, Throwable throwable) {
        if (bitmapLoader == null)
            return;

        //打印日志
        if (bitmapLoader.getLogger() != null)
            bitmapLoader.getLogger().e("[DefaultOnDiskCacheOpenFailedListener]onFailed, use BitmapLoader.setOnDiskCacheOpenFailedListener to custom processing", throwable);

        if (context == null){
            if (bitmapLoader.getLogger() != null)
                bitmapLoader.getLogger().e("[DefaultOnDiskCacheOpenFailedListener]context is null, can't show dialog");
            return;
        }

        final String dialogTitle;
        final String dialogMessage;
        final String toastMessage;
        final String buttonMessageYes;
        final String buttonMessageNo;

        if (DeviceUtils.isLocaleZhCn(context)){
            dialogTitle = "图片缓存访问失败";
            dialogMessage = "1.检查内存是否已满.\n" +
                    "2.尝试重启手机.\n" +
                    "\n" +
                    "无法访问缓存的情况下,相同图片会重复下载,流量消耗大幅增加.\n" +
                    "是否继续加载图片?";
            toastMessage = "不加载图片";
            buttonMessageYes = "是";
            buttonMessageNo = "否";
        }else{
            dialogTitle = "ImageCache open failed";
            dialogMessage = "1.Check if the memory is full.\n" +
                    "2.Try to reboot this device.\n" +
                    "\n" +
                    "Image will be downloaded repeatedly without ImageCache, and will waste your data usage.\n" +
                    "To load image anyway?";
            toastMessage = "skip image load";
            buttonMessageYes = "Yes";
            buttonMessageNo = "No";
        }

        //提醒客户缓存访问失败, 由用户选择是否禁用磁盘缓存继续加载图片
        Dialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton(buttonMessageYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        bitmapLoader.setDiskCacheDisabled().open();//禁用磁盘缓存后启动

                        if (onViewRefreshListener != null)
                            onViewRefreshListener.run();
                    }
                })
                .setNegativeButton(buttonMessageNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                    }
                })
                .create();

        //显示提示框
        alertDialog.show();
    }

    @Override
    public void onCacheWriteException(Context context, BitmapLoader bitmapLoader, Throwable throwable) {
        throwable.printStackTrace();
    }
}
