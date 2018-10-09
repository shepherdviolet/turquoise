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

package sviolet.turquoise.x.common.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.support.annotation.IntRange;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * 辅助功能模块
 *
 * @see AccessibilityContainerService
 */
public abstract class AccessibilityModule {

    private AccessibilityContainerService service;//持有服务容器

    protected AccessibilityModule(AccessibilityContainerService service) {
        if (service == null){
            throw new NullPointerException("AccessibilityContainerService is null");
        }
        this.service = service;
    }

    /**
     * 如果AccessibilityModule的实现类有@AccessibilityModule.Api注释, 容器会判断当前设备是否符合版本要求,
     * 如果低于版本要求, 会调用该方法, 如果返回true, 则继续启用该模块, 如果返回false, 不启用模块.
     * 该模块
     */
    protected abstract boolean onLowApi();

    /**
     * This method is a part of the {@link AccessibilityService} lifecycle and is
     * called after the system has successfully bound to the service. If is
     * convenient to use this method for setting the {@link AccessibilityServiceInfo}.
     */
    protected void onServiceConnected() {

    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    protected void onDestroy() {

    }

    /**
     * Called by the system when the user performs a specific gesture on the
     * touch screen.
     *
     * <strong>Note:</strong> To receive gestures an accessibility service must
     * request that the device is in touch exploration mode by setting the
     * {@link android.accessibilityservice.AccessibilityServiceInfo#FLAG_REQUEST_TOUCH_EXPLORATION_MODE}
     * flag.
     *
     * @param gestureId The unique id of the performed gesture.
     *
     * @return Whether the gesture was handled.
     *
     * @see AccessibilityService#GESTURE_SWIPE_UP
     * @see AccessibilityService#GESTURE_SWIPE_UP_AND_LEFT
     * @see AccessibilityService#GESTURE_SWIPE_UP_AND_DOWN
     * @see AccessibilityService#GESTURE_SWIPE_UP_AND_RIGHT
     * @see AccessibilityService#GESTURE_SWIPE_DOWN
     * @see AccessibilityService#GESTURE_SWIPE_DOWN_AND_LEFT
     * @see AccessibilityService#GESTURE_SWIPE_DOWN_AND_UP
     * @see AccessibilityService#GESTURE_SWIPE_DOWN_AND_RIGHT
     * @see AccessibilityService#GESTURE_SWIPE_LEFT
     * @see AccessibilityService#GESTURE_SWIPE_LEFT_AND_UP
     * @see AccessibilityService#GESTURE_SWIPE_LEFT_AND_RIGHT
     * @see AccessibilityService#GESTURE_SWIPE_LEFT_AND_DOWN
     * @see AccessibilityService#GESTURE_SWIPE_RIGHT
     * @see AccessibilityService#GESTURE_SWIPE_RIGHT_AND_UP
     * @see AccessibilityService#GESTURE_SWIPE_RIGHT_AND_LEFT
     * @see AccessibilityService#GESTURE_SWIPE_RIGHT_AND_DOWN
     */
    protected boolean onGesture(int gestureId) {
        return false;
    }

    /**
     * Callback that allows an accessibility service to observe the key events
     * before they are passed to the rest of the system. This means that the events
     * are first delivered here before they are passed to the device policy, the
     * input method, or applications.
     * <p>
     * <strong>Note:</strong> It is important that key events are handled in such
     * a way that the event stream that would be passed to the rest of the system
     * is well-formed. For example, handling the down event but not the up event
     * and vice versa would generate an inconsistent event stream.
     * </p>
     * <p>
     * <strong>Note:</strong> The key events delivered in this method are copies
     * and modifying them will have no effect on the events that will be passed
     * to the system. This method is intended to perform purely filtering
     * functionality.
     * <p>
     *
     * @param event The event to be processed. This event is owned by the caller and cannot be used
     * after this method returns. Services wishing to use the event after this method returns should
     * make a copy.
     * @return If true then the event will be consumed and not delivered to
     *         applications, otherwise it will be delivered as usual.
     */
    protected boolean onKeyEvent(KeyEvent event) {
        return false;
    }

    /**
     * Callback for {@link android.view.accessibility.AccessibilityEvent}s.
     *
     * @param event The new event. This event is owned by the caller and cannot be used after
     * this method returns. Services wishing to use the event after this method returns should
     * make a copy.
     */
    protected abstract void onAccessibilityEvent(AccessibilityEvent event);

    /**
     * Callback for interrupting the accessibility feedback.
     */
    protected abstract void onInterrupt();

    /**
     * @return get AccessibilityService
     */
    protected AccessibilityContainerService getService(){
        return service;
    }

    /**
     * 从容器中移除该模块
     */
    protected void removeModuleFromContainer(){
        getService().removeModule(this);
        onDestroy();
    }

    /**
     * 使用该注释可以指定模块需要的最低API版本, 当设备版本不满足要求时, 会回调onLowApi方法, 并选择是否不启动模块
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(TYPE)
    @Inherited
    public @interface Api {
        @IntRange(from=1)
        int value() default 1;
    }

}
