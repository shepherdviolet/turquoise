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

package sviolet.demoa.info.utils;

import android.graphics.Rect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * LayoutInspector视图节点信息
 *
 * Created by S.Violet on 2017/9/11.
 */
public class LayoutInspectorNodeInfo {

    /**
     * 因为AccessibilityService.onAccessibilityEvent是在UI线程调用, 图形绘制也在UI线程, 因此不需要
     * 考虑线程安全问题, 可以复用Node(不存在绘制被回收掉的节点数据)
     */
    private static LinkedBlockingQueue<LayoutInspectorNodeInfo> cache = new LinkedBlockingQueue<>(100);

    public static LayoutInspectorNodeInfo obtain(){
        LayoutInspectorNodeInfo nodeInfo = cache.poll();
        if (nodeInfo == null){
            return new LayoutInspectorNodeInfo();
        }
        return nodeInfo;
    }

    public static void recycle(LayoutInspectorNodeInfo nodeInfo){
        if (nodeInfo == null){
            return;
        }
        nodeInfo.getSubs().clear();
        cache.offer(nodeInfo);
    }

    private String id;//视图ID
    private String clazz;//视图类型
    private Rect rect = new Rect();//视图在屏幕中的位置
    private List<LayoutInspectorNodeInfo> subs = new ArrayList<>();//子视图

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public List<LayoutInspectorNodeInfo> getSubs() {
        return subs;
    }

    public void setSubs(List<LayoutInspectorNodeInfo> subs) {
        this.subs = subs;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
