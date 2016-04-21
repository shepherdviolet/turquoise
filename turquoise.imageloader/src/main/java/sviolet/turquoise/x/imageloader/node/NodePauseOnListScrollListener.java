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

package sviolet.turquoise.x.imageloader.node;

import android.widget.AbsListView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by S.Violet on 2016/4/21.
 */
public class NodePauseOnListScrollListener implements AbsListView.OnScrollListener {

    private AtomicBoolean pause = new AtomicBoolean(false);
    private WeakReference<NodeController> controller;

    private AbsListView.OnScrollListener customOnScrollListener;

    NodePauseOnListScrollListener(NodeController controller){
        this.controller = new WeakReference<>(controller);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        final NodeController controller = this.controller.get();

        if (controller != null) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    /**
                     * when pause update succeed, decrement nodePauseCount,
                     * if count <= 0, notify Engine.
                     */
                    if (pause.compareAndSet(true, false)) {
                        int count = controller.getNodePauseCount().decrementAndGet();
                        if (count <= 0) {
                            controller.postIgnite();
                        }
                        controller.getLogger().d("[NodePauseOnListScrollListener]resume");
                    }
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    /**
                     * when pause update succeed, increment nodePauseCount
                     */
                    if (pause.compareAndSet(false, true)) {
                        controller.getNodePauseCount().incrementAndGet();
                        controller.getLogger().d("[NodePauseOnListScrollListener]pause");
                    }
                    break;
            }
        }

        if (customOnScrollListener != null) {
            customOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (customOnScrollListener != null){
            customOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setCustomOnScrollListener(AbsListView.OnScrollListener customOnScrollListener){
        this.customOnScrollListener = customOnScrollListener;
    }

}
