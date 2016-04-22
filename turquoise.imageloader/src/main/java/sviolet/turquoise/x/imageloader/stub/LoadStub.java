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

package sviolet.turquoise.x.imageloader.stub;

import android.graphics.drawable.Drawable;
import android.view.View;

import java.lang.ref.WeakReference;

import sviolet.turquoise.common.statics.SpecialResourceId;
import sviolet.turquoise.x.imageloader.drawable.ContainerDrawable;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 * Load Stub<br/>
 * loading image/background to View<br/>
 *
 * Created by S.Violet on 2016/2/19.
 */
public abstract class LoadStub<V extends View> extends AbsStub {

    private WeakReference<V> view;

    public LoadStub(String url, Params params, V view){
        super(url, params);
        this.view = new WeakReference<>(view);
    }

    @Override
    public void initialize(NodeController controller) {
        super.initialize(controller);
        bindView(getView());
        showLoading();
    }

    /*******************************************************8
     * control inner
     */

    /**
     * override this method to judge if it's time to launch
     * @return SUCCEED:ready to launch  RETRY:try launch again  FAILED:can't launch any more
     */
    @Override
    protected LaunchResult readyForLaunch() {
        //check view
        final V view = getView();
        if (view == null){
            return LaunchResult.FAILED;
        }
        //check if size match view
        if (getParams().isSizeMatchView()){
            //size match view
            if (getParams().adjustByView(view)){
                //adjust succeed
                return LaunchResult.SUCCEED;
            }else{
                //adjust failed
                /*
                 * if Params->sizeMatchView is true, make sure View's size > 1, TILoader will
                 * waiting until view's size > 1. if ImageView size is wrap_content, you should
                 * set a LoadingDrawable which size > 1;
                 */
                getLogger().w("[LoadStub]\'Params->sizeMatchView\'mode: view's size <= 1, retry until view's size > 1, key" + getKey());
                return LaunchResult.RETRY;
            }
        }else{
            //size not match view
            return LaunchResult.SUCCEED;
        }
    }

    @Override
    protected LaunchResult onLaunch() {
        return super.onLaunch();
    }

    @Override
    protected LaunchResult onRelaunch() {
        return showLoading() ? LaunchResult.SUCCEED : LaunchResult.FAILED;
    }

    @Override
    protected boolean load() {
        //get & check view
        final V view = getView();
        if (view == null){
            onDestroy();
            return false;
        }
        return super.load();
    }

    @Override
    protected boolean reload() {
        //get & check view
        final V view = getView();
        if (view == null){
            onDestroy();
            return false;
        }
        return super.reload();
    }

    protected boolean showLoading(){
        //get & check view
        final V view = getView();
        if (view == null){
            onDestroy();
            return false;
        }
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null){
            onDestroy();
            return false;
        }
        //create and set drawable
        Drawable drawable = controller.getLoadingDrawableFactory().create(controller.getApplicationContextImage(), controller.getContextImage(), getParams(), getLogger());
        if (drawable == null){
            throw new RuntimeException("[LoadStub]LoadingDrawableFactory create a null drawable");
        }
        if (getState() == State.DESTROYED){
            return false;
        }
        setDrawableToView(new ContainerDrawable(drawable).launchEnable().bindStub(this), view);
        return true;
    }

    protected boolean showImage(ImageResource<?> resource){
        //get & check view
        final V view = getView();
        if (view == null){
            onDestroy();
            return false;
        }
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null){
            onDestroy();
            return false;
        }
        //create and set drawable
        Drawable drawable = controller.getBackgroundDrawableFactory().create(controller.getApplicationContextImage(), controller.getContextImage(), getParams(), getLogger());
        if (drawable == null){
            throw new RuntimeException("[LoadStub]BackgroundDrawableFactory create a null drawable");
        }
        Drawable imageDrawable = controller.getServerSettings().getImageResourceHandler().toDrawable(resource, false);
        if (imageDrawable == null){
            shiftSucceedToFailed();
            return false;
        }
        if (getState() == State.DESTROYED){
            return false;
        }
        ContainerDrawable containerDrawable = new ContainerDrawable(drawable, imageDrawable).relaunchEnable().bindStub(this);
        setDrawableToView(containerDrawable, view);
        //start animation
        containerDrawable.startTransition(controller.getNodeSettings().getImageAppearDuration());
        return true;
    }

    protected boolean showFailed(){
        //get & check view
        final V view = getView();
        if (view == null){
            onDestroy();
            return false;
        }
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null){
            onDestroy();
            return false;
        }
        //create and set drawable
        Drawable drawable = controller.getFailedDrawableFactory().create(controller.getApplicationContextImage(), controller.getContextImage(), getParams(), getLogger());
        if (drawable == null){
            throw new RuntimeException("[LoadStub]FailedDrawableFactory create a null drawable");
        }
        if (getState() == State.DESTROYED){
            return false;
        }
        setDrawableToView(new ContainerDrawable(drawable).bindStub(this), view);
        return true;
    }

    /**
     * @param drawable set drawable to view
     * @param view target view
     */
    protected abstract void setDrawableToView(Drawable drawable, V view);

    /*******************************************************8
     * callbacks inner
     */

    @Override
    protected void onLoadSucceedInner(ImageResource<?> resource) {
        super.onLoadSucceedInner(resource);
        showImage(resource);
    }

    @Override
    protected void onLoadFailedInner() {
        super.onLoadFailedInner();
    }

    @Override
    protected void onLoadCanceledInner() {
        showFailed();
    }

    @Override
    protected void onDestroyInner() {
        super.onDestroyInner();
        if (view != null){
            view.clear();
        }
    }

    /***********************************************************
     * protected
     */

    protected void bindView(V view){
        if (view == null){
            onDestroy();
            return;
        }
        synchronized (view) {
            //get Stub from View Tag
            Object tag = view.getTag(SpecialResourceId.ViewTag.TILoaderStub);
            //destroy obsolete Stub
            if (tag != null && tag instanceof Stub) {
                ((Stub) tag).onDestroy();
            }
            //bind Stub on View
            view.setTag(SpecialResourceId.ViewTag.TILoaderStub, this);
        }
    }

    /***********************************************************
     * Getter
     */

    protected V getView(){
        if (view != null){
            return view.get();
        }
        return null;
    }

    @Override
    public Type getType() {
        return Type.LOAD;
    }
}
