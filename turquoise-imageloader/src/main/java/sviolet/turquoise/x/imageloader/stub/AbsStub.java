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

package sviolet.turquoise.x.imageloader.stub;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.DigestCipher;
import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.LoadProgress;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 * <p>Manage image loading process of view or extract affairs: launch, relaunch, reload, update view or callback to listener.
 * Maintain the relationship between the view/listener and the load node</p>
 *
 * <p>Achieve the basic functions of loading</p>
 *
 * Created by S.Violet on 2016/2/26.
 */
public abstract class AbsStub implements Stub {

    private static final String NULL = "TILoader_Special_Key_Null_@a%#5r#$6t4";

    //params///////////////////////////////

    private String url;//loading url
    private Params params;//loading params
    private String resourceKey;//resource key

    //stat///////////////////////////////

    private AtomicInteger state = new AtomicInteger(State.INITIAL);
    private LoadProgress loadProgress = new LoadProgress();
    private int reloadTimes = 0;

    private WeakReference<NodeController> nodeController;

    public AbsStub(String url, Params params){
        if (url == null){
            throw new RuntimeException("[AbsStub]url must not be null");
        }
        if (params == null){
            params = new Params.Builder().build();
        }
        this.url = url;
        this.params = params;
    }

    /**
     * 1.bind nodeController<br/>
     * 2.set state to INITIALIZED<br/>
     *
     * @param nodeController nodeController
     */
    @Override
    public void initialize(NodeController nodeController) {
        this.nodeController = new WeakReference<>(nodeController);
    }

    /******************************************************************
     * control
     */

    /**
     * 1.check state
     * 2.set state to LAUNCHING
     * @return true if launch valid
     */
    @Override
    public final LaunchResult launch() {
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null || controller.isDestroyed()){
            onDestroy();
            return LaunchResult.FAILED;
        }
        //check if ready
        LaunchResult check;
        if ((check = readyForLaunch()) != LaunchResult.SUCCEED){
            //try again
            return check;
        }
        //check state
        if (state.compareAndSet(State.INITIAL, State.LAUNCHING)){
            return onLaunch();
        }
        return LaunchResult.FAILED;
    }

    /**
     * 1.check state
     * 2.set state to INITIAL
     * @param force false : relaunch only when loading canceled, true : force relaunch when loading succeed/failed/canceled
     * @return true if relaunch valid
     */
    @Override
    public final LaunchResult relaunch(boolean force) {
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null || controller.isDestroyed()){
            onDestroy();
            return LaunchResult.FAILED;
        }

        if (force) {
            //check state
            if (state.compareAndSet(State.LOAD_SUCCEED, State.INITIAL) ||
                    state.compareAndSet(State.LOAD_FAILED, State.INITIAL) ||
                    state.compareAndSet(State.LOAD_CANCELED, State.INITIAL)) {
                return onRelaunch();
            }
        }else{
            //check state
            if (state.compareAndSet(State.LOAD_CANCELED, State.INITIAL)) {
                return onRelaunch();
            }
        }

        return LaunchResult.FAILED;
    }

    /******************************************************************
     * control inner
     */

    /**
     * override this method to judge if it's time to launch
     * @return SUCCEED:ready to launch  RETRY:try launch again  FAILED:can't launch any more
     */
    protected LaunchResult readyForLaunch(){
        return LaunchResult.SUCCEED;
    }

    /**
     * launch process
     * @return true if launch valid
     */
    protected LaunchResult onLaunch(){
        //load image
        return load() ? LaunchResult.SUCCEED : LaunchResult.FAILED;
    }

    /**
     * relaunch process
     * @return true if relaunch valid
     */
    protected LaunchResult onRelaunch(){
        //launch
        return launch();
    }

    /**
     * <p>basic load</p>
     *
     * 1.check state<br/>
     * 2.execute by nodeController<br/>
     *
     * @return true if stub executed by nodeController
     */
    protected boolean load() {
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null || controller.isDestroyed()){
            onDestroy();
            return false;
        }

        if (state.compareAndSet(State.LAUNCHING, State.LOADING) ||
                state.compareAndSet(State.LOAD_SUCCEED, State.LOADING) ||
                state.compareAndSet(State.LOAD_FAILED, State.LOADING) ||
                state.compareAndSet(State.LOAD_CANCELED, State.LOADING)){
            reloadTimes = 0;
            controller.execute(this);
            if (getLogger().checkEnable(TLogger.DEBUG)) {
                getLogger().d("[AbsStub]load: key:" + getKey() + " url:" + getUrl());
            }
            return true;
        }

        return false;
    }

    /**
     * <p>basic reload</p>
     *
     * 1.check state<br/>
     * 2.check reload times<br/>
     * 3.execute by nodeController<br/>
     *
     * @return true if stub executed by nodeController
     */
    protected boolean reload() {
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null || controller.isDestroyed()){
            onDestroy();
            return false;
        }

        if (canReload(controller)) {
            if (state.compareAndSet(State.LAUNCHING, State.LOADING) ||
                    state.compareAndSet(State.LOAD_SUCCEED, State.LOADING) ||
                    state.compareAndSet(State.LOAD_FAILED, State.LOADING) ||
                    state.compareAndSet(State.LOAD_CANCELED, State.LOADING)){
                reloadTimes++;
                controller.execute(this);
                if (getLogger().checkEnable(TLogger.DEBUG)) {
                    getLogger().d("[AbsStub]reload: key:" + getKey());
                }
                return true;
            }
        }

        return false;
    }

    /**
     * shift LOAD_SUCCEED state to LOAD_FAILED, and than invoke onLoadFailedInner();
     * @return true:shift succeed
     */
    protected boolean shiftSucceedToFailed(){

        if (state.compareAndSet(State.LOAD_SUCCEED, State.LOAD_FAILED)){
            onLoadFailedInner();
            return true;
        }

        return false;
    }

    /**
     * shift LOAD_FAILED state to LOAD_CANCELED, and than invoke onLoadFailedInner();
     * @return true:shift succeed
     */
    protected boolean shiftFailedToCanceled(){

        if (state.compareAndSet(State.LOAD_FAILED, State.LOAD_CANCELED)){
            onLoadCanceledInner();
            return true;
        }

        return false;
    }

    /**
     * if reloadTimes < limit
     * @param controller controller, might be null
     * @return true reloadTimes < limit
     */
    private boolean canReload(NodeController controller){
        //get & check controller
        if (controller == null){
            controller = getNodeController();
        }
        if (controller == null || controller.isDestroyed()) {
            return false;
        }

        return reloadTimes < controller.getReloadTimes();

    }

    /******************************************************************
     * callbacks
     */

    /**
     * 1.check resource valid<br/>
     * 2.update state<br/>
     * 3.invoke inner method<br/>
     *
     * @param resource loaded Image, may be null
     */
    @Override
    public final void onLoadSucceed(ImageResource resource) {
        //check resource
        if (!TILoaderUtils.isImageResourceValid(resource)){
            onLoadFailed();
            return;
        }

        if (state.compareAndSet(State.LOADING, State.LOAD_SUCCEED)){
            onLoadSucceedInner(resource);
        }

    }

    /**
     * 1.update state<br/>
     * 2.invoke inner method<br/>
     */
    @Override
    public final void onLoadFailed() {

        if (state.compareAndSet(State.LOADING, State.LOAD_FAILED)){
            onLoadFailedInner();
        }

    }

    /**
     * 1.update state<br/>
     * 2.invoke inner method<br/>
     */
    @Override
    public final void onLoadCanceled() {

        if (state.compareAndSet(State.LOADING, State.LOAD_CANCELED)){
            onLoadCanceledInner();
        }

    }

    /**
     * 1.update state<br/>
     * 2.invoke inner method<br/>
     */
    @Override
    public final void onDestroy() {

        if (state.get() != State.DESTROYED) {
            state.set(State.DESTROYED);//destroyed state
            onDestroyInner();
            getLogger().d("[AbsStub]destroyed: key:" + getKey());
        }

    }

    /***********************************************************
     * callbacks inner
     */

    protected void onLoadSucceedInner(ImageResource resource){

    }

    protected void onLoadFailedInner(){
        if (!canReload(null)){
            shiftFailedToCanceled();
            return;
        }
        reload();
    }

    protected void onLoadCanceledInner(){

    }

    /**
     * called when destroy
     */
    protected void onDestroyInner(){

    }

    /***********************************************************
     * params & getter
     */

    @Override
    public String getUrl(){
        return this.url;
    }

    @Override
    public Params getParams(){
        return this.params;
    }

    @Override
    public String getKey(){
        if (getType() == Type.EXTRACT){
            return getResourceKey() + "@EXT" + params.getKeySuffix();
        }
        return getResourceKey() + params.getKeySuffix();
    }

    @Override
    public String getResourceKey() {
        if (resourceKey == null){
            String url = getUrl();
            if (url == null) {
                url = NULL;
            }
            resourceKey = params.getSourceType().getMark() + ByteUtils.bytesToHex(DigestCipher.digestStr(url, DigestCipher.TYPE_SHA1));
        }
        return resourceKey;
    }

    @Override
    public int getState() {
        return state.get();
    }

    @Override
    public LoadProgress getLoadProgress() {
        return loadProgress;
    }

    @Override
    public StubRemoter getStubRemoter(){
        return new StubRemoter(this);
    }

    @Override
    public TLogger getLogger() {
        final NodeController controller = getNodeController();
        if (controller != null){
            return controller.getLogger();
        }
        return TLogger.get(null);
    }

    protected NodeController getNodeController(){
        if (this.nodeController!= null){
            return nodeController.get();
        }
        return null;
    }

}
