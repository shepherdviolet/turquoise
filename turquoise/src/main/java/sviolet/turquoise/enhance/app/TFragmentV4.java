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

package sviolet.turquoise.enhance.app;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sviolet.turquoise.enhance.app.mvp.TView;
import sviolet.turquoise.enhance.app.utils.InjectUtils;
import sviolet.turquoise.enhance.async.WeakHandler;

/**
 * [组件扩展]Fragment<br>
 * <br>
 * 0.InjectUtils注释式注入控件对象/绑定监听<br/>
 * {@link InjectUtils};<br/>
 * <br>
 *
 * @author S.Violet
 */
public abstract class TFragmentV4 extends Fragment implements TView {

    //View复用
    private View fragmentViewCache;

    //Lazy Load
    private boolean lazyLoaded = false;
    private boolean viewInitialized = false;
    private boolean visibility = true;
    private boolean activityStarted = false;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //get cache
        View fragmentView = fragmentViewCacheEnabled() ? this.fragmentViewCache : null;

        //create
        if(fragmentView == null){
            fragmentView = InjectUtils.inject(this, inflater, container);
            onInitView(fragmentView, savedInstanceState);
            viewInitialized = true;
            if (fragmentViewCacheEnabled()){
                this.fragmentViewCache = fragmentView;
            }
        }

        //clear parent
        ViewGroup parent = (ViewGroup) fragmentView.getParent();
        if (parent != null) {
            parent.removeView(fragmentView);
        }

        //refresh
        onRefreshView(fragmentView, savedInstanceState);

        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        //Lazy Load
        //reset lazy flag, if cache not enabled
        if (!fragmentViewCacheEnabled()){
            lazyLoaded = false;
            viewInitialized = false;
            visibility = true;
        }
    }

    @Override
    public final void onStart() {
        super.onStart();
        activityStarted = true;

        //Lazy Load
        //lazy load if still visible while onStart
        if (!lazyLoaded && visibility){
            lazyLoad();
        }

        afterStart();
    }

    @Override
    public final void onStop() {
        super.onStop();
        activityStarted = false;

        afterStop();
    }

    @Override
    public void onPresenterRefresh(int code) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        //Lazy Load
        if (!hidden){
            visibility = true;
            //lazy load if call onHiddenChanged with false params
            if (!lazyLoaded && viewInitialized) {
                lazyLoad();
            }
        } else {
            visibility = false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        //Lazy Load
        if (isVisibleToUser){
            visibility = true;
            //lazy load if call setUserVisibleHint with true params
            if (!lazyLoaded && viewInitialized) {
                lazyLoad();
            }
        } else {
            visibility = false;
        }
    }

    private void lazyLoad(){
        lazyLoaded = true;
        onLazyLoad();
    }

    private void resetLazyLoadInner(){
        if (!lazyLoaded){
            return;
        }
        if (!visibility){
            lazyLoaded = false;
            return;
        }
        if (activityStarted){
            lazyLoad();
            return;
        }
        lazyLoaded = false;
    }

    /**
     * 复写该方法实现View的创建, View复用模式下只会调用一次, 非复用模式下, 每次都会调用
     */
    protected abstract void onInitView(View fragmentView, Bundle savedInstanceState);

    /**
     * 复写该方法实现View的刷新, 每次都会调用
     */
    protected abstract void onRefreshView(View fragmentView, Bundle savedInstanceState);

    /**
     * <p>true:Fragment复用同一个view, onCreateView方法只会创建一次View, 流畅, 但是费内存</p>
     * <p>false:Fragment默认的机制, 每次attach都会执行onCreateView并创建新的View, 省内存, 但是卡顿</p>
     */
    protected abstract boolean fragmentViewCacheEnabled();

    /**
     * 等同于onStart
     */
    protected void afterStart(){

    }

    /**
     * 等同于onStop
     */
    protected void afterStop(){

    }

    /**
     * 复写该方法实现懒加载
     */
    protected void onLazyLoad(){

    }

    /**
     * 重置LazyLoad状态, 使得数据重新加载一次
     */
    public void resetLazyLoad(){
        myHandler.sendEmptyMessage(MyHandler.HANDLER_RESET_LAZY_LOAD);
    }

    /***********************************************************************************
     * handler
     */

    private final MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<TFragmentV4> {

        private static final int HANDLER_RESET_LAZY_LOAD = 1;

        public MyHandler(TFragmentV4 host) {
            super(Looper.getMainLooper(), host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, TFragmentV4 host) {
            switch (msg.what){
                case HANDLER_RESET_LAZY_LOAD:
                    host.resetLazyLoadInner();
                    break;
                default:
                    break;
            }
        }
    }

}
