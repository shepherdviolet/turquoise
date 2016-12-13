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

package sviolet.turquoise.util.droid;

import android.annotation.TargetApi;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import sviolet.turquoise.util.common.DateTimeUtils;

/**
 * <p>创建MotionEvent的工具</p>
 *
 * <p>
 *  1.一个事件最大支持{@value TEMP_SIZE}个触点, 若超过{@value TEMP_SIZE}将会抛出异常<br/>
 *  2.必须在UI线程调用, 为了优化性能, 每次创建事件都公用相同的缓存实例, 多线程调用会发生问题.<br/>
 *  3.MotionEventUtils.TouchPoints建议使用单例, 每次用setCapacity设置触点数, 并设置id/x/y值, 这样可以优化性能.<br/>
 * </p>
 *
 * Created by S.Violet on 2016/11/4.
 */
@TargetApi(14)
public class MotionEventUtils {

    private static final int TEMP_SIZE = 20;//最大支持的触点数

    private static final float DEFAULT_PRESSURE = 1.0f;//压力
    private static final float DEFAULT_SIZE = 1.0f;
    private static final int DEFAULT_TOOL_TYPE = MotionEvent.TOOL_TYPE_FINGER;//触摸工具类型
    private static final int DEFAULT_META_STATE = 0;
    private static final int DEFAULT_BUTTON_STATE = 0;
    private static final float DEFAULT_X_PRECISION = 1.0f;//X精度
    private static final float DEFAULT_Y_PRECISION = 1.0f;//Y精度
    private static final int DEFAULT_DEVICE_ID = 0;//设备ID
    private static final int DEFAULT_EDGE_FLAGS = 0;
    private static final int DEFAULT_SOURCE = 0x1002;//源
    private static final int DEFAULT_FLAGS = 0;

    private static MotionEvent.PointerCoords[] gSharedTempPointerCoords;
    private static MotionEvent.PointerProperties[] gSharedTempPointerProperties;
    private static int[] ids;
    private static TouchPoints gSharedTempTouchPoints;
    private static int[] locationCache = new int[2];

    static{
        gSharedTempPointerCoords = new MotionEvent.PointerCoords[TEMP_SIZE];
        for (int i = 0; i < TEMP_SIZE; i++) {
            gSharedTempPointerCoords[i] = new MotionEvent.PointerCoords();
        }
        if (DeviceUtils.getVersionSDK() >= 14) {
            gSharedTempPointerProperties = new MotionEvent.PointerProperties[TEMP_SIZE];
            for (int i = 0; i < TEMP_SIZE; i++) {
                gSharedTempPointerProperties[i] = new MotionEvent.PointerProperties();
            }
        }else{
            ids = new int[TEMP_SIZE];
        }
        gSharedTempTouchPoints = new TouchPoints();
    }

    /**
     * [UI线程限定]生成一个MotionEvent
     * @param action MotionEvent.ACTION_DOWN...
     * @param points 触点id/坐标信息, 建议用单例
     */
    public static MotionEvent obtain(int action, TouchPoints points){
        return obtain(action, points, -1);
    }

    /**
     * [UI线程限定]生成一个MotionEvent
     * @param action MotionEvent.ACTION_DOWN...
     * @param points 触点id/坐标信息, 建议用单例
     * @param downTime 按下事件的时间, 设置-1则自动分配当前时间, 必须用SystemClock.uptimeMillis()的时间!
     */
    public static MotionEvent obtain(int action, TouchPoints points, long downTime){
        if (Looper.myLooper() != Looper.getMainLooper()){
            //必须主线程调用, 避免线程同步问题(因为共用gSharedTempPointerCoords和gSharedTempPointerProperties)
            throw new RuntimeException("[MotionEventUtils]you must call obtain method in ui thread");
        }

        //事件发生时间
        long eventTime = DateTimeUtils.getUptimeMillis();
        //默认按下时间
        if (downTime <= 0){
            downTime = eventTime;
        }

        //触点数量
        int pointCapacity = 0;
        if (points != null){
            pointCapacity = points.getCapacity();
        }

        //坐标
        for (int i = 0 ; i < pointCapacity ; i++){
            if (DeviceUtils.getVersionSDK() >= 12) {
                gSharedTempPointerCoords[i].clear();
            }
            gSharedTempPointerCoords[i].x = points.getX(i);
            gSharedTempPointerCoords[i].y = points.getY(i);
            gSharedTempPointerCoords[i].pressure = DEFAULT_PRESSURE;
            gSharedTempPointerCoords[i].size = DEFAULT_SIZE;
        }

        if (DeviceUtils.getVersionSDK() >= 14) {

            //id
            for (int i = 0 ; i < pointCapacity ; i++){
                gSharedTempPointerProperties[i].clear();
                gSharedTempPointerProperties[i].id = points.getId(i);
                gSharedTempPointerProperties[i].toolType = DEFAULT_TOOL_TYPE;
            }

            return MotionEvent.obtain(
                    downTime,//按下时间
                    eventTime,//事件时间
                    action,//事件类型
                    pointCapacity,
                    gSharedTempPointerProperties,
                    gSharedTempPointerCoords,
                    DEFAULT_META_STATE,
                    DEFAULT_BUTTON_STATE,
                    DEFAULT_X_PRECISION,
                    DEFAULT_Y_PRECISION,
                    DEFAULT_DEVICE_ID,
                    DEFAULT_EDGE_FLAGS,
                    DEFAULT_SOURCE,
                    DEFAULT_FLAGS
            );

        }else{

            /*
             * 需要找一个API14以下的设备或虚拟机中测试以下, ids固定20长度, 但实际点的数量小于20时会不会报错
             */

            //id
            for (int i = 0 ; i < pointCapacity ; i++){
                ids[i] = points.getId(i);
            }

            return MotionEvent.obtain(
                    downTime,//按下时间
                    eventTime,//事件时间
                    action,//事件类型
                    pointCapacity,
                    ids,
                    gSharedTempPointerCoords,
                    DEFAULT_META_STATE,
                    DEFAULT_X_PRECISION,
                    DEFAULT_Y_PRECISION,
                    DEFAULT_DEVICE_ID,
                    DEFAULT_EDGE_FLAGS,
                    DEFAULT_SOURCE,
                    DEFAULT_FLAGS
            );

        }
    }

    /**
     * 根据指定的View, 将MotionEvent的坐标修正(offsetLocation)到符合该View的坐标, 无论原来被修正与否
     * @param motionEvent motionEvent
     * @param view 指定View
     */
    public static void offsetLocationByView(MotionEvent motionEvent, View view){
        if (Looper.myLooper() != Looper.getMainLooper()){
            //必须主线程调用, 避免线程同步问题(因为共用gSharedTempPointerCoords和gSharedTempPointerProperties)
            throw new RuntimeException("[MotionEventUtils]you must call offsetLocationByView method in ui thread");
        }
        if (motionEvent == null || view == null){
            return;
        }
        //获得View在屏幕上的坐标
        view.getLocationOnScreen(locationCache);
        //修正坐标
        motionEvent.offsetLocation(motionEvent.getRawX() - motionEvent.getX() - locationCache[0],
                motionEvent.getRawY() - motionEvent.getY() - locationCache[1]);
    }

    /**
     * [UI线程限定]获得共享的TouchPoints
     */
    public static TouchPoints getSharedTouchPoints(){
        if (Looper.myLooper() != Looper.getMainLooper()){
            //必须主线程调用, 避免线程同步问题(因为共用gSharedTempPointerCoords和gSharedTempPointerProperties)
            throw new RuntimeException("[MotionEventUtils]you must call getSharedTouchPoints method in ui thread");
        }
        return gSharedTempTouchPoints;
    }

    /**
     * [UI线程限定]把原始的事件模拟成CANCEL事件分发给子控件
     * @param ev 原事件
     * @param executor 在这个接口中实现将事件分发给子控件, 注意MotionEvent未修正过坐标
     */
    public static void emulateCancelEvent(MotionEvent ev, EmulateMotionEventExecutor executor){
        if (Looper.myLooper() != Looper.getMainLooper()){
            //必须主线程调用, 避免线程同步问题(因为共用gSharedTempPointerCoords和gSharedTempPointerProperties)
            throw new RuntimeException("[MotionEventUtils]you must call emulateCancelEvent method in ui thread");
        }
        if (ev == null || executor == null){
            return;
        }
        gSharedTempTouchPoints.setCapacity(ev.getPointerCount());
        for (int i = 0 ; i < gSharedTempTouchPoints.getCapacity() ; i++){
            gSharedTempTouchPoints.setX(i, ev.getX(i));
            gSharedTempTouchPoints.setY(i, ev.getY(i));
            gSharedTempTouchPoints.setId(i, ev.getPointerId(i));
        }
        MotionEvent emuEvent = MotionEventUtils.obtain(MotionEvent.ACTION_CANCEL, gSharedTempTouchPoints, ev.getDownTime());
        executor.dispatchTouchEvent(emuEvent);
    }

    /**
     * [UI线程限定]把原始的事件模拟成DOWN事件分发给子控件
     * @param ev 原事件
     * @param precise true:精确模拟, 每一个触点分发一次事件, 模拟所有手指依次按下
     * @param executor 在这个接口中实现将事件分发给子控件, 注意MotionEvent未修正过坐标
     */
    public static void emulateDownEvent(MotionEvent ev, boolean precise, EmulateMotionEventExecutor executor){
        if (Looper.myLooper() != Looper.getMainLooper()){
            //必须主线程调用, 避免线程同步问题(因为共用gSharedTempPointerCoords和gSharedTempPointerProperties)
            throw new RuntimeException("[MotionEventUtils]you must call emulateDownEvent method in ui thread");
        }
        if (ev == null || executor == null){
            return;
        }
        if (precise){
            //精确模拟, 每一个触点分发一次事件
            for (int i = 0 ; i < ev.getPointerCount() ; i++){
                gSharedTempTouchPoints.setCapacity(i + 1);
                for (int j = 0; j < gSharedTempTouchPoints.getCapacity(); j++) {
                    gSharedTempTouchPoints.setX(j, ev.getX(j));
                    gSharedTempTouchPoints.setY(j, ev.getY(j));
                    gSharedTempTouchPoints.setId(j, ev.getPointerId(j));
                }
                int action = i == 0 ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_POINTER_DOWN;
                MotionEvent emuEvent = MotionEventUtils.obtain(action, gSharedTempTouchPoints, ev.getDownTime());
                executor.dispatchTouchEvent(emuEvent);
            }
        }else {
            //简单模拟, 只分发一次down事件
            gSharedTempTouchPoints.setCapacity(ev.getPointerCount());
            for (int i = 0; i < gSharedTempTouchPoints.getCapacity(); i++) {
                gSharedTempTouchPoints.setX(i, ev.getX(i));
                gSharedTempTouchPoints.setY(i, ev.getY(i));
                gSharedTempTouchPoints.setId(i, ev.getPointerId(i));
            }
            MotionEvent emuEvent = MotionEventUtils.obtain(MotionEvent.ACTION_DOWN, gSharedTempTouchPoints, ev.getDownTime());
            executor.dispatchTouchEvent(emuEvent);
        }
    }


    /**
     * [UI线程限定]把原始的事件模拟成CANCEL事件分发给子控件, 会自动修正坐标
     * @param ev 原事件
     * @param view 目标子控件
     */
    public static void emulateCancelEvent(MotionEvent ev, View view){
        if (Looper.myLooper() != Looper.getMainLooper()){
            //必须主线程调用, 避免线程同步问题(因为共用gSharedTempPointerCoords和gSharedTempPointerProperties)
            throw new RuntimeException("[MotionEventUtils]you must call emulateCancelEvent method in ui thread");
        }
        if (ev == null || view == null){
            return;
        }
        gSharedTempTouchPoints.setCapacity(ev.getPointerCount());
        for (int i = 0 ; i < gSharedTempTouchPoints.getCapacity() ; i++){
            gSharedTempTouchPoints.setX(i, ev.getX(i));
            gSharedTempTouchPoints.setY(i, ev.getY(i));
            gSharedTempTouchPoints.setId(i, ev.getPointerId(i));
        }
        MotionEvent emuEvent = MotionEventUtils.obtain(MotionEvent.ACTION_CANCEL, gSharedTempTouchPoints, ev.getDownTime());
        offsetLocationByView(emuEvent, view);
        view.dispatchTouchEvent(emuEvent);
    }

    /**
     * [UI线程限定]把原始的事件模拟成DOWN事件分发给子控件, 会自动修正坐标
     * @param ev 原事件
     * @param precise true:精确模拟, 每一个触点分发一次事件, 模拟所有手指依次按下
     * @param view 目标子控件
     */
    public static void emulateDownEvent(MotionEvent ev, boolean precise, View view){
        if (Looper.myLooper() != Looper.getMainLooper()){
            //必须主线程调用, 避免线程同步问题(因为共用gSharedTempPointerCoords和gSharedTempPointerProperties)
            throw new RuntimeException("[MotionEventUtils]you must call emulateDownEvent method in ui thread");
        }
        if (ev == null || view == null){
            return;
        }
        if (precise){
            //精确模拟, 每一个触点分发一次事件
            for (int i = 0 ; i < ev.getPointerCount() ; i++){
                gSharedTempTouchPoints.setCapacity(i + 1);
                for (int j = 0; j < gSharedTempTouchPoints.getCapacity(); j++) {
                    gSharedTempTouchPoints.setX(j, ev.getX(j));
                    gSharedTempTouchPoints.setY(j, ev.getY(j));
                    gSharedTempTouchPoints.setId(j, ev.getPointerId(j));
                }
                int action = i == 0 ? MotionEvent.ACTION_DOWN : MotionEvent.ACTION_POINTER_DOWN;
                MotionEvent emuEvent = MotionEventUtils.obtain(action, gSharedTempTouchPoints, ev.getDownTime());
                offsetLocationByView(emuEvent, view);
                view.dispatchTouchEvent(emuEvent);
            }
        }else {
            //简单模拟, 只分发一次down事件
            gSharedTempTouchPoints.setCapacity(ev.getPointerCount());
            for (int i = 0; i < gSharedTempTouchPoints.getCapacity(); i++) {
                gSharedTempTouchPoints.setX(i, ev.getX(i));
                gSharedTempTouchPoints.setY(i, ev.getY(i));
                gSharedTempTouchPoints.setId(i, ev.getPointerId(i));
            }
            MotionEvent emuEvent = MotionEventUtils.obtain(MotionEvent.ACTION_DOWN, gSharedTempTouchPoints, ev.getDownTime());
            offsetLocationByView(emuEvent, view);
            view.dispatchTouchEvent(emuEvent);
        }
    }

    public static final class TouchPoints {

        private int capacity = 0;
        private int[] ids = new int[TEMP_SIZE];
        private float[] xs = new float[TEMP_SIZE];
        private float[] ys = new float[TEMP_SIZE];

        public TouchPoints setCapacity(int capacity){
            if (capacity < 0){
                throw new RuntimeException("[MotionEventUtils]capacity must >= 0");
            }
            if (capacity > TEMP_SIZE){
                throw new RuntimeException("[MotionEventUtils]max point num is " + TEMP_SIZE + ", but your capacity is " + capacity);
            }
            this.capacity = capacity;
            return this;
        }

        public TouchPoints setId(int position, int id){
            if (position < 0){
                throw new RuntimeException("[MotionEventUtils]position must >= 0");
            }
            if (position >= TEMP_SIZE){
                throw new RuntimeException("[MotionEventUtils]max point num is " + TEMP_SIZE + ", but your position is " + position);
            }
            ids[position] = id;
            return this;
        }

        public TouchPoints setX(int position, float x){
            if (position < 0){
                throw new RuntimeException("[MotionEventUtils]position must >= 0");
            }
            if (position >= TEMP_SIZE){
                throw new RuntimeException("[MotionEventUtils]max point num is " + TEMP_SIZE + ", but your position is " + position);
            }
            xs[position] = x;
            return this;
        }

        public TouchPoints setY(int position, float y){
            if (position < 0){
                throw new RuntimeException("[MotionEventUtils]position must >= 0");
            }
            if (position >= TEMP_SIZE){
                throw new RuntimeException("[MotionEventUtils]max point num is " + TEMP_SIZE + ", but your position is " + position);
            }
            ys[position] = y;
            return this;
        }

        public int getCapacity(){
            return capacity;
        }

        public int getId(int position){
            if (position < 0){
                throw new RuntimeException("[MotionEventUtils]position must >= 0");
            }
            if (position >= TEMP_SIZE){
                throw new RuntimeException("[MotionEventUtils]max point num is " + TEMP_SIZE + ", but your position is " + position);
            }
            return ids[position];
        }

        public float getX(int position){
            if (position < 0){
                throw new RuntimeException("[MotionEventUtils]position must >= 0");
            }
            if (position >= TEMP_SIZE){
                throw new RuntimeException("[MotionEventUtils]max point num is " + TEMP_SIZE + ", but your position is " + position);
            }
            return xs[position];
        }

        public float getY(int position){
            if (position < 0){
                throw new RuntimeException("[MotionEventUtils]position must >= 0");
            }
            if (position >= TEMP_SIZE){
                throw new RuntimeException("[MotionEventUtils]max point num is " + TEMP_SIZE + ", but your position is " + position);
            }
            return ys[position];
        }

    }

    /**
     * 在这个接口中实现: 将模拟出来的MotionEvent分发给View的过程
     */
    public interface EmulateMotionEventExecutor{

        /**
         * @param emulateMotionEvent emulateMotionEvent未修正过坐标
         */
        void dispatchTouchEvent(MotionEvent emulateMotionEvent);

    }

}
