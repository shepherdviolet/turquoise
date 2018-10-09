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

package sviolet.turquoise.enhance.app.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;

import java.util.concurrent.atomic.AtomicInteger;

import sviolet.thistle.entity.common.Destroyable;
import sviolet.turquoise.common.statics.PublicConstants;

/**
 * <p>将原先需要重写Activity.onActivityResult处理的事件转为回调形式,
 * 占用Activity的requestCode(见PublicConstants.ActivityRequestCode)</p>
 *
 * @author S.Violet
 */
public class ActivityResultCallbackManager implements Destroyable {

    private AtomicInteger requestCode = new AtomicInteger(0);

    private SparseArray<OnActivityResultCallback> callbacks = new SparseArray<>();

    /**
     * Activity结果回调接口
     */
    public interface OnActivityResultCallback {
        /**
         * @param requestCode requestCode
         * @param resultCode resultCode
         * @param data intent
         */
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    /**
     * 销毁管理器
     */
    @Override
    public void onDestroy() {
        callbacks.clear();
    }

    /**
     * 注册一个回调并返回一个请求号
     *
     * <pre>{@code
     *      startActivityForResult(new Intent(SourceActivity.this, TargetActivity.class), activityResultCallbackManager.register(new ActivityResultCallbackManager.OnActivityResultCallback{
     *          public void onActivityResult(int requestCode, int resultCode, Intent data){
     *              ......
     *          }
     *      }));
     * }</pre>
     *
     * @param callback 回调
     * @return 回调对应的请求号
     */
    public int register(OnActivityResultCallback callback) {
        //生成请求号
        final int requestCode = this.requestCode.getAndIncrement() % PublicConstants.ActivityRequestCode.ActivityResultCallbackMax
                + PublicConstants.ActivityRequestCode.ActivityResultCallbackStart;
        //注册回调
        callbacks.put(requestCode, callback);
        return requestCode;
    }

    /**
     * 处理Activity结果(拦截{@link Activity#onActivityResult(int, int, Intent)})
     * @return true:管理器处理了响应, false:管理器未处理响应, 需要自行处理
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        //取出回调
        OnActivityResultCallback callback = callbacks.get(requestCode);

        if(callback != null) {
            //从回调中移除
            callbacks.remove(requestCode);
            //回调任务
            callback.onActivityResult(requestCode, resultCode, data);
            return true;
        }
        return false;
    }

}
