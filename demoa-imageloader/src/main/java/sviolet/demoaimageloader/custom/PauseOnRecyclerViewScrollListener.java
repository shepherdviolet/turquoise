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

package sviolet.demoaimageloader.custom;

import android.support.v7.widget.RecyclerView;

import sviolet.turquoise.x.imageloader.node.NodeRemoter;

/**
 * 用于优化RecyclerView滑动时的加载(非必须)
 *
 * Created by S.Violet on 2016/4/28.
 */
public class PauseOnRecyclerViewScrollListener extends RecyclerView.OnScrollListener{

    private NodeRemoter nodeRemoter;
    private RecyclerView.OnScrollListener customOnScrollListener;

    private PauseOnRecyclerViewScrollListener(NodeRemoter nodeRemoter){
        this.nodeRemoter = nodeRemoter;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        switch (newState){
            case RecyclerView.SCROLL_STATE_IDLE:
            case RecyclerView.SCROLL_STATE_DRAGGING:
                nodeRemoter.resume();
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                nodeRemoter.pause();
                break;
        }

        if (customOnScrollListener != null) {
            customOnScrollListener.onScrollStateChanged(recyclerView, newState);
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (customOnScrollListener != null) {
            customOnScrollListener.onScrolled(recyclerView, dx, dy);
        }
    }

    public void setCustomOnScrollListener(RecyclerView.OnScrollListener customOnScrollListener){
        this.customOnScrollListener = customOnScrollListener;
    }
}
