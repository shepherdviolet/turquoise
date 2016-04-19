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

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.util.conversion.ByteUtils;
import sviolet.turquoise.util.crypt.DigestCipher;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.node.NodeController;

/**
 *
 * Created by S.Violet on 2016/2/26.
 */
public abstract class AbsStub implements Stub {

    private static final String NULL = "null";

    //params///////////////////////////////

    private String url;//loading url
    private Params params;//loading params
    private String key;//key (include params)
    private String resourceKey;//resource key

    //stat///////////////////////////////

    private volatile State state = State.INITIAL;
    private int reloadTimes = 0;

    private WeakReference<NodeController> nodeController;
    private final ReentrantLock stateLock = new ReentrantLock();

    public AbsStub(String url, Params params){
        if (url == null){
            throw new RuntimeException("[TILoader:AbsStub]url must not be null");
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
        boolean execute = false;
        try {
            stateLock.lock();
            if (state == State.INITIAL) {
                state = State.LAUNCHING;
                execute = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (execute){
            return onLaunch();
        }
        return LaunchResult.FAILED;
    }

    /**
     * 1.check state
     * 2.set state to INITIAL
     * @return true if relaunch valid
     */
    @Override
    public final LaunchResult relaunch() {
        //get & check controller
        final NodeController controller = getNodeController();
        if (controller == null || controller.isDestroyed()){
            onDestroy();
            return LaunchResult.FAILED;
        }
        boolean execute = false;
        try {
            stateLock.lock();
            if (state == State.LOAD_SUCCEED || state == State.LOAD_FAILED || state == State.LOAD_CANCELED) {
                state = State.INITIAL;
                execute = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (execute){
            return onRelaunch();
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
        boolean execute = false;
        try {
            stateLock.lock();
            if (state == State.LAUNCHING || state == State.LOAD_SUCCEED || state == State.LOAD_FAILED || state == State.LOAD_CANCELED) {
                state = State.LOADING;
                reloadTimes = 0;
                execute = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (execute) {
            controller.execute(this);
        }
        return execute;
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
        boolean reload = false;
        try {
            stateLock.lock();
            if (state == State.LAUNCHING || state == State.LOAD_SUCCEED  || state == State.LOAD_FAILED || state == State.LOAD_CANCELED) {
                if (canReload(controller)){
                    reloadTimes++;
                    state = State.LOADING;
                    reload = true;
                }
            }
        }finally {
            stateLock.unlock();
        }
        if (reload) {
            controller.execute(this);
        }
        return reload;
    }

    /**
     * shift LOAD_SUCCEED state to LOAD_FAILED, and than invoke onLoadFailedInner();
     * @return true:shift succeed
     */
    protected boolean shiftSucceedToFailed(){
        boolean finish = false;
        try {
            stateLock.lock();
            if (state == State.LOAD_SUCCEED) {
                state = State.LOAD_FAILED;
                finish = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (finish) {
            onLoadFailedInner();
        }
        return finish;
    }

    /**
     * shift LOAD_FAILED state to LOAD_CANCELED, and than invoke onLoadFailedInner();
     * @return true:shift succeed
     */
    protected boolean shiftFailedToCanceled(){
        boolean finish = false;
        try {
            stateLock.lock();
            if (state == State.LOAD_FAILED) {
                state = State.LOAD_CANCELED;
                finish = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (finish) {
            onLoadCanceledInner();
        }
        return finish;
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
        try {
            stateLock.lock();
            if (reloadTimes < controller.getNodeSettings().getReloadTimes()){
                return true;
            }
        }finally {
            stateLock.unlock();
        }
        return false;
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
    public final void onLoadSucceed(ImageResource<?> resource) {
        //check resource
        if (!TILoaderUtils.isImageResourceValid(resource)){
            onLoadFailed();
            return;
        }
        boolean finish = false;
        try {
            stateLock.lock();
            if (state == State.LOADING) {
                state = State.LOAD_SUCCEED;
                finish = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (finish) {
            onLoadSucceedInner(resource);
        }
    }

    /**
     * 1.update state<br/>
     * 2.invoke inner method<br/>
     */
    @Override
    public final void onLoadFailed() {
        boolean finish = false;
        try {
            stateLock.lock();
            if (state == State.LOADING) {
                state = State.LOAD_FAILED;
                finish = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (finish) {
            onLoadFailedInner();
        }
    }

    /**
     * 1.update state<br/>
     * 2.invoke inner method<br/>
     */
    @Override
    public final void onLoadCanceled() {
        boolean cancel = false;
        try {
            stateLock.lock();
            if (state == State.LOADING) {
                state = State.LOAD_CANCELED;
                cancel = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (cancel) {
            onLoadCanceledInner();
        }
    }

    /**
     * 1.update state<br/>
     * 2.invoke inner method<br/>
     */
    @Override
    public final void onDestroy() {
        boolean destroy = false;
        try {
            stateLock.lock();
            if (state != State.DESTROYED) {
                state = State.DESTROYED;
                destroy = true;
            }
        }finally {
            stateLock.unlock();
        }
        if (destroy){
            onDestroyInner();
        }
    }

    /***********************************************************
     * callbacks inner
     */

    protected void onLoadSucceedInner(ImageResource<?> resource){

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
        if (nodeController != null){
            nodeController.clear();
        }
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
        return getResourceKey() + params.getKeySuffix();
    }

    @Override
    public String getResourceKey() {
        if (resourceKey == null){
            String url = getUrl();
            if (url == null)
                url = NULL;
            resourceKey = ByteUtils.bytesToHex(DigestCipher.digestStr(url, DigestCipher.TYPE_SHA1));
        }
        return resourceKey;
    }

    @Override
    public State getState() {
        try{
            stateLock.lock();
            return state;
        }finally {
            stateLock.unlock();
        }
    }

    @Override
    public TLogger getLogger() {
        final NodeController controller = getNodeController();
        if (controller != null){
            return controller.getLogger();
        }
        return TLogger.getNullLogger();
    }

    protected NodeController getNodeController(){
        if (this.nodeController!= null){
            return nodeController.get();
        }
        return null;
    }

}
