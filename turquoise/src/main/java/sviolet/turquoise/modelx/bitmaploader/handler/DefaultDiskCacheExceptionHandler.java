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

package sviolet.turquoise.modelx.bitmaploader.handler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import sviolet.turquoise.modelx.bitmaploader.BitmapLoader;
import sviolet.turquoise.modelx.bitmaploader.entity.BitmapRequest;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.util.droid.DeviceUtils;

/**
 * 磁盘缓存异常处理器默认实现<p/>
 *
 * ***************************************************<p/>
 *
 * 设置磁盘缓存打开失败的处理模式:<p/>
 *
 * <pre>{@code
 * BitmapLoader.Builder.setDiskCacheExceptionHandler(
 *      new DefaultDiskCacheExceptionHandler(OpenFailedHandleMode) //设置处理模式
 *          .setViewRefreshListener(new Runnable(){ //设置禁用磁盘缓存启动后刷新UI回调监听(可选)
 *              public void run() {
 *                  //例如ListView适配器刷新
 *                  if (adapter != null)
 *                      adapter.notifyDataSetChanged();
 *              }
 *          })
 * )
 * }</pre><p/>
 *
 * 可选模式:<p/>
 *
 * OpenFailedHandleMode.NOTICE_ONLY_BY_DIALOG : <br/>
 * 仅弹出对话框提示, 停止加载图片<p/>
 *
 * OpenFailedHandleMode.NOTICE_ONLY_BY_TOAST : <br/>
 * 仅弹出Toast提示, 停止加载图片<p/>
 *
 * OpenFailedHandleMode.CHOICE_TO_OPEN_WITHOUT_DISK_CACHE_OR_NOT : <br/>
 * 弹出对话框提示, 由用户选择是否禁用磁盘缓存继续加载图片,
 * 建议配合setViewRefreshListener()方法使用, 在启用磁盘缓存禁用模式后,
 * 会回调该监听器, 用来刷新显示(重新加载图片).<p/>
 *
 * OpenFailedHandleMode.OPEN_WITHOUT_DISK_CACHE_SILENCE : <br/>
 * 静默处理(无提示), 禁用磁盘缓存继续加载图片, 慎用此种方式,
 * 建议配合setViewRefreshListener()方法使用, 在启用磁盘缓存禁用模式后,
 * 会回调该监听器, 用来刷新显示(重新加载图片).<p/>
 *
 * Created by S.Violet on 2015/11/4.
 */
public class DefaultDiskCacheExceptionHandler implements DiskCacheExceptionHandler {

    private TLogger logger = TLogger.get(this);

    private OpenFailedHandleMode mode;//磁盘缓存打开失败处理模式
    private Runnable viewRefreshListener;//显示刷新回调(CHOICE_TO_OPEN_WITHOUT_DISK_CACHE_OR_NOT/OPEN_WITHOUT_DISK_CACHE_SILENCE)

    /**
     * 磁盘缓存打开失败时弹出对话框提示, 不继续加载图片
     */
    public DefaultDiskCacheExceptionHandler() {
        this(OpenFailedHandleMode.NOTICE_ONLY_BY_DIALOG);
    }

    /**
     * 磁盘缓存打开失败的处理模式:<p/>
     *
     * OpenFailedHandleMode.NOTICE_ONLY_BY_DIALOG : <br/>
     * 仅弹出对话框提示, 停止加载图片<p/>
     *
     * OpenFailedHandleMode.NOTICE_ONLY_BY_TOAST : <br/>
     * 仅弹出Toast提示, 停止加载图片<p/>
     *
     * OpenFailedHandleMode.CHOICE_TO_OPEN_WITHOUT_DISK_CACHE_OR_NOT : <br/>
     * 弹出对话框提示, 由用户选择是否禁用磁盘缓存继续加载图片,
     * 建议配合setViewRefreshListener()方法使用, 在启用磁盘缓存禁用模式后,
     * 会回调该监听器, 用来刷新显示(重新加载图片).<p/>
     *
     * OpenFailedHandleMode.OPEN_WITHOUT_DISK_CACHE_SILENCE : <br/>
     * 静默处理(无提示), 禁用磁盘缓存继续加载图片, 慎用此种方式,
     * 建议配合setViewRefreshListener()方法使用, 在启用磁盘缓存禁用模式后,
     * 会回调该监听器, 用来刷新显示(重新加载图片).<p/>
     *
     * @param mode 磁盘缓存打开失败的处理模式
     */
    public DefaultDiskCacheExceptionHandler(OpenFailedHandleMode mode) {
        this.mode = mode;
    }

    /**
     * 在启用磁盘缓存禁用模式后, 会回调该监听器, 用来刷新显示(重新加载图片).<p/>
     *
     * 适用于如下模式:<br/>
     * OpenFailedHandleMode.CHOICE_TO_OPEN_WITHOUT_DISK_CACHE_OR_NOT <br/>
     * OpenFailedHandleMode.OPEN_WITHOUT_DISK_CACHE_SILENCE <br/>
     *
     * @param viewRefreshListener 在启用磁盘缓存禁用模式后, 会回调该监听器, 用来刷新显示(重新加载图片)
     */
    public DefaultDiskCacheExceptionHandler setViewRefreshListener(Runnable viewRefreshListener){
        this.viewRefreshListener = viewRefreshListener;
        return this;
    }

    @Override
    public void onCacheOpenException(Context context, BitmapLoader bitmapLoader, Throwable throwable) {

        //打印日志
        logger.e("DiskCache open failed, use BitmapLoader.Builder.setDiskCacheExceptionHandler to custom processing", throwable);

        //提示或开启磁盘缓存禁用模式
        try {
            switch (mode) {
                case NOTICE_ONLY_BY_DIALOG://仅弹出对话框提示, 停止加载图片
                    noticeOnlyByDialog(context, bitmapLoader);
                    break;
                case NOTICE_ONLY_BY_TOAST://仅弹出Toast提示, 停止加载图片
                    noticeOnlyByToast(context, bitmapLoader);
                    break;
                case CHOICE_TO_OPEN_WITHOUT_DISK_CACHE_OR_NOT://弹出对话框提示, 由用户选择是否继续加载图片(禁用磁盘缓存)
                    choiceToOpenWithoutDiskCacheOrNot(context, bitmapLoader);
                    break;
                case OPEN_WITHOUT_DISK_CACHE_SILENCE://静默处理, 继续加载图片(禁用磁盘缓存)
                    openWithoutDiskCacheSilence(bitmapLoader);
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 磁盘缓存写入异常, 打印日志
     */
    @Override
    public void onCacheWriteException(Context context, BitmapLoader bitmapLoader, BitmapRequest request, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onDestroy() {

    }

    /**
     * 磁盘缓存打开失败处理方式
     */
    public enum OpenFailedHandleMode{
        NOTICE_ONLY_BY_DIALOG,//仅弹出对话框提示, 停止加载图片
        NOTICE_ONLY_BY_TOAST,//仅弹出Toast提示, 停止加载图片
        CHOICE_TO_OPEN_WITHOUT_DISK_CACHE_OR_NOT,//弹出对话框提示, 由用户选择是否继续加载图片(禁用磁盘缓存)
        OPEN_WITHOUT_DISK_CACHE_SILENCE//静默处理, 继续加载图片(禁用磁盘缓存)
    }

    private void noticeOnlyByDialog(final Context context, BitmapLoader bitmapLoader){
        if (context == null){
            logger.e("context is null, can't show dialog");
            return;
        }

        final String dialogTitle;
        final String dialogMessage;
        final String buttonMessageYes;

        if (DeviceUtils.isLocaleZhCn(context)){
            dialogTitle = "图片缓存访问失败, 无法加载图片";
            dialogMessage = "1.检查内存是否已满.\n" +
                    "2.尝试重启手机.";
            buttonMessageYes = "确认";
        }else{
            dialogTitle = "ImageCache open failed, can't load image";
            dialogMessage = "1.Check if the memory is full.\n" +
                    "2.Try to reboot this device.";
            buttonMessageYes = "confirm";
        }

        //提醒客户缓存访问失败
        Dialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setCancelable(false)
                .setPositiveButton(buttonMessageYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //无操作
                    }
                })
                .create();

        //显示提示框
        alertDialog.show();
    }

    private void noticeOnlyByToast(Context context, BitmapLoader bitmapLoader){
        if (context == null){
            logger.e("context is null, can't show toast");
            return;
        }

        final String toastMessage;

        if (DeviceUtils.isLocaleZhCn(context)){
            toastMessage = "图片缓存访问失败, 无法加载图片";
        }else{
            toastMessage = "ImageCache open failed, can't load image";
        }

        //Toast提示
        Toast.makeText(context, toastMessage, Toast.LENGTH_LONG).show();

    }

    private void openWithoutDiskCacheSilence (BitmapLoader bitmapLoader){
        if (bitmapLoader == null)
            return;

        bitmapLoader.openWithoutDiskCache();//禁用磁盘缓存后启动

        if (viewRefreshListener != null)
            viewRefreshListener.run();

        logger.e("openWithoutDiskCacheSilence");
    }

    private void choiceToOpenWithoutDiskCacheOrNot(final Context context, final BitmapLoader bitmapLoader) {
        //加载器为空, 只能提示
        if (bitmapLoader == null) {
            noticeOnlyByDialog(context, null);
            return;
        }

        if (context == null){
            logger.e("context is null, can't show dialog");
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
                        //禁用磁盘缓存后启动
                        bitmapLoader.openWithoutDiskCache();
                        //刷新UI
                        if (viewRefreshListener != null)
                            viewRefreshListener.run();
                    }
                })
                .setNegativeButton(buttonMessageNo, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //提示不加载图片
                        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                    }
                })
                .create();

        //显示提示框
        alertDialog.show();
    }

}
