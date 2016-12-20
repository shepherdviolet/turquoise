/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.turquoise.enhance.app;

import sviolet.turquoise.enhance.app.utils.RuntimePermissionManager;
import sviolet.turquoise.utilx.lifecycle.listener.LifeCycle;

/**
 * 所有强化的Activity都实现该接口
 *
 * Created by S.Violet on 2016/12/20.
 */
public interface EnhancedContext {
    
    /**********************************************
     * public
     */

    /**
     * 将生命周期监听器绑定在该Activity上<p/>
     *
     * LifeCycleUtils不会强引用监听器, 需自行持有对象.<p/>
     *
     * @param lifeCycle 生命周期监听器
     */
    void attachLifeCycle(LifeCycle lifeCycle);

    /**********************************************
     * Public
     *
     * Runtime Permission
     */

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 所需权限 android.Manifest.permission....
     * @param task 需要权限的任务
     */
    void executePermissionTask(String permission, RuntimePermissionManager.RequestPermissionTask task);

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permissions 所需权限 android.Manifest.permission....
     * @param task 需要权限的任务
     */
    void executePermissionTask(String[] permissions, RuntimePermissionManager.RequestPermissionTask task);

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<br/>
     *
     * @param permission 所需权限 android.Manifest.permission....
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param task 需要权限的任务
     */
    void executePermissionTask(String permission, String rationaleTitle, String rationaleContent, RuntimePermissionManager.RequestPermissionTask task);

    /**
     * 执行一个需要权限的任务, 兼容低版本<br/>
     * 检查权限->显示说明->请求权限->回调{@link RuntimePermissionManager.RequestPermissionTask}<br/>
     * 目的任务在{@link RuntimePermissionManager.RequestPermissionTask}中实现, 需要判断权限是否被授予<p/>
     *
     * 注意:会占用requestCode 201-250 , 建议不要与原生方法requestPermission同时使用.<br/>
     *
     * @param permissions 所需权限 android.Manifest.permission....
     * @param rationaleTitle 权限说明标题(标题和内容都送空, 则不提示)
     * @param rationaleContent 权限说明内容(标题和内容都送空, 则不提示)
     * @param task 需要权限的任务
     */
    void executePermissionTask(String[] permissions, String rationaleTitle, String rationaleContent, RuntimePermissionManager.RequestPermissionTask task);

}
