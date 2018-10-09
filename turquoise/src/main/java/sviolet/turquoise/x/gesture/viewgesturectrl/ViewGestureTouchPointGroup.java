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

package sviolet.turquoise.x.gesture.viewgesturectrl;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>触点组:记录从ACTION_DOWN事件开始,到ACTION_UP或ACTION_CANCEL事件结束的多触点信息</p>
 *
 * Created by S.Violet on 2016/9/22.
 */

class ViewGestureTouchPointGroup {

    private List<ViewGestureTouchPoint> points = new ArrayList<>();
    private int maxPointNum = 0;
    private int mTouchSlop;


    ViewGestureTouchPointGroup(Context context) {
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    ViewGestureTouchPoint update(MotionEvent event){

        ViewGestureTouchPoint point;
        ViewGestureTouchPoint abandonedPoint = null;
        int index;

        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                points.clear();
                maxPointNum = 0;
                point = new ViewGestureTouchPoint();
                resetPoint(point, event, 0);
                points.add(point);
                break;
            case MotionEvent.ACTION_MOVE:
                for (index = 0 ; index < points.size() ; index++){
                    updatePoint(points.get(index), event, index);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                index = getPointIndexFromEvent(event);
                point = new ViewGestureTouchPoint();
                resetPoint(point, event, index);
                points.add(index, point);//插入到index位置
                break;
            case MotionEvent.ACTION_POINTER_UP:
                index = getPointIndexFromEvent(event);
                if (index < 0 || index >= points.size()){
                    break;
                }
                abandonedPoint = points.remove(index);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (points.size() < 1){
                    break;
                }
                abandonedPoint = points.remove(0);
                break;
            default:
                return null;
        }

        int pointNum = getPointNum();
        if (pointNum > maxPointNum){
            maxPointNum = pointNum;
        }

        return abandonedPoint;
    }

    private void resetPoint(ViewGestureTouchPoint point, MotionEvent event, int index){
        point.currX = getXFromEvent(event, index);
        point.currY = getYFromEvent(event, index);
        point.downX = point.currX;
        point.downY = point.currY;
        point.id = event.getPointerId(index);
    }

    private void updatePoint(ViewGestureTouchPoint point, MotionEvent event, int index){
        float _currX = getXFromEvent(event, index);
        float _currY = getYFromEvent(event, index);
        point.stepX = _currX - point.currX;
        point.stepY = _currY - point.currY;
        point.currX = _currX;
        point.currY = _currY;

        if (!point.isEffectiveMoved){
            if (Math.abs(point.currX - point.downX) > mTouchSlop || Math.abs(point.currY - point.downY) > mTouchSlop){
                point.isEffectiveMoved = true;
            }
        }
    }

    private int getPointIndexFromEvent(MotionEvent event){
        return (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

    private float getXFromEvent(MotionEvent event, int index){
        return event.getX(index);//相对于容器的坐标
    }

    private float getYFromEvent(MotionEvent event, int index){
        return event.getY(index);//相对于容器的坐标
    }

    int getPointNum(){
        return points.size();
    }

    int getMaxPointNum(){
        return maxPointNum;
    }

    ViewGestureTouchPoint getPoint(int index){
        if (index >= points.size()){
            return null;
        }
        return points.get(index);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("========ViewGestureTouchPointGroup[" + getPointNum() + "]========\n");
        for (ViewGestureTouchPoint point : points){
            stringBuilder.append(point.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
