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
import android.widget.GridView;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

import sviolet.turquoise.util.droid.DeviceUtils;

/**
 * <p>Remoter of Node, helps smooth slide, for {@link ListView} / {@link GridView} / RecyclerView,
 * but not necessary.</p>
 *
 * <p>Engine will not execute tasks which in paused Node. Node will pause util all NodeRemotes are resumed(not pause).
 * As long as there is a paused NodeRemoter, Node will keep pause status.</p>
 *
 * <p>Example for {@link ListView} / {@link GridView}:</p>
 *
 * <pre>{@code
 *      //Node will pause util all ListViews are not fling.
 *      //You should new different NodeRemoter for each ListView, Do not re-use.
 *      listView1.setOnScrollListener(TILoader.node(this).newNodeRemoter().getPauseOnListViewScrollListener());
 *      listView2.setOnScrollListener(TILoader.node(this).newNodeRemoter().getPauseOnListViewScrollListener().setCustomOnScrollListener(...));
 * }</pre>
 *
 * <p>Example for RecyclerView:</p>
 *
 * <pre>{@code
 *      //Node will pause util all RecyclerViews are not fling.
 *      //You should new different NodeRemoter for each RecyclerViews, Do not re-use.
 *      recyclerView1.addOnScrollListener(new PauseOnRecyclerViewScrollListener(TILoader.node(this).newNodeRemoter()));
 *      recyclerView2.addOnScrollListener(new PauseOnRecyclerViewScrollListener(TILoader.node(this).newNodeRemoter()));
 *
 *      //implement RecyclerView.OnScrollListener
 *      public class PauseOnRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
 *
 *          private NodeRemoter nodeRemoter;
 *
 *          private PauseOnRecyclerViewScrollListener(NodeRemoter nodeRemoter) {
 *              this.nodeRemoter = nodeRemoter;
 *          }
 *
 *          public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
 *              switch (newState) {
 *                  case RecyclerView.SCROLL_STATE_IDLE:
 *                  case RecyclerView.SCROLL_STATE_DRAGGING:
 *                      nodeRemoter.resume();
 *                      break;
 *                  case RecyclerView.SCROLL_STATE_SETTLING:
 *                      nodeRemoter.pause();
 *                      break;
 *              }
 *          }
 *
 *          public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
 *
 *          }
 *
 *      }
 * }</pre>
 *
 * Created by S.Violet on 2016/4/28.
 */
public class NodeRemoter {

    private AtomicBoolean pause = new AtomicBoolean(false);
    private WeakReference<NodeController> controller;

    NodeRemoter(NodeController controller){
        this.controller = new WeakReference<>(controller);
    }

    /**
     * pause Node
     */
    public void pause(){
        final NodeController controller = this.controller.get();
        if (controller != null) {
            /**
             * when pause update succeed, increment nodePauseCount
             */
            if (pause.compareAndSet(false, true)) {
                controller.getNodePauseCount().incrementAndGet();
                controller.getLogger().d("[NodeRemoter]pause, nodeId:" + controller.getNodeId());
            }
        }
    }

    /**
     * resume Node
     */
    public void resume(){
        final NodeController controller = this.controller.get();
        if (controller != null) {
            /**
             * when pause update succeed, decrement nodePauseCount,
             * if count <= 0, notify Engine.
             */
            if (pause.compareAndSet(true, false)) {
                int count = controller.getNodePauseCount().decrementAndGet();
                if (count <= 0) {
                    controller.postIgnite();
                    controller.getLogger().d("[NodeRemoter]resume and re-ignite, nodeId:" + controller.getNodeId());
                }else {
                    controller.getLogger().d("[NodeRemoter]resume, nodeId:" + controller.getNodeId());
                }
            }
        }
    }

    /**
     * destroy Node, this method only for api < 11, do not call if min api 11+
     * @deprecated this method only for api < 11, do not call if min api 11+
     */
    @Deprecated
    public void destroy(){
        if (DeviceUtils.getVersionSDK() >= 11){
            //just for api < 11
            return;
        }
        final NodeController controller = this.controller.get();
        if (controller != null) {
            controller.onDestroy();
            controller.getLogger().d("[NodeRemoter]destroy (api<11), nodeId:" + controller.getNodeId());
        }
    }

    /**
     * @return get OnScrollListener for ListView / GridView, which can pause Node on fling
     */
    public PauseOnListViewScrollListener getPauseOnListViewScrollListener(){
        return new PauseOnListViewScrollListener(this);
    }

    /**
     * Helper for ListView / GridView
     */
    public static class PauseOnListViewScrollListener implements AbsListView.OnScrollListener {

        private NodeRemoter nodeRemoter;

        private AbsListView.OnScrollListener customOnScrollListener;

        private PauseOnListViewScrollListener(NodeRemoter nodeRemoter){
            this.nodeRemoter = nodeRemoter;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    nodeRemoter.resume();
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                    nodeRemoter.pause();
                    break;
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

}
