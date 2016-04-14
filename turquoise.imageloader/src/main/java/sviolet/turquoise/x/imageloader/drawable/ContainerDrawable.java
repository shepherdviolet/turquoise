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

package sviolet.turquoise.x.imageloader.drawable;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import java.lang.ref.WeakReference;

import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.stub.Stub;

/**
 *
 * Created by S.Violet on 2016/4/13.
 */
public class ContainerDrawable extends TransitionDrawable {

    private boolean transitionMode = false;
    private boolean launchEnable = false;
    private boolean relaunchEnable = false;

    private WeakReference<Stub> stub;

    public ContainerDrawable(Drawable drawable){
        super(new Drawable[]{drawable});
        transitionMode = false;
        setCrossFadeEnabled(true);
    }

    public ContainerDrawable(Drawable background, Drawable drawable) {
        super(new Drawable[]{background, drawable});
        transitionMode = true;
        setCrossFadeEnabled(true);
    }

    @Override
    public void draw(Canvas canvas) {
        launch();
        try {
            super.draw(canvas);
        } catch (Exception e){
            relaunch();
        }
    }

    @Override
    public void startTransition(int durationMillis) {
        //skip method if it's not in transitionMode
        if (transitionMode)
            super.startTransition(durationMillis);
    }

    @Override
    public void resetTransition() {
        //skip method if it's not in transitionMode
        if (transitionMode)
            super.resetTransition();
    }

    @Override
    public void reverseTransition(int duration) {
        //skip method if it's not in transitionMode
        if (transitionMode)
            super.reverseTransition(duration);
    }

    public ContainerDrawable bindStub(Stub stub){

        return this;
    }

    public ContainerDrawable launchEnable(){
        launchEnable = true;
        return this;
    }

    public ContainerDrawable relaunchEnable(){
        relaunchEnable = true;
        return this;
    }

    private void launch(){
        if (!launchEnable){
            return;
        }
        final Stub stub = getStub();
        if (stub != null){
            if (stub.launch()){
                getLogger().d("[ContainerDrawable]launch: key:" + stub.getKey());
            }else{
                getLogger().d("[ContainerDrawable]launch failed: key:" + stub.getKey());
            }
        }
    }

    private void relaunch(){
        if (!relaunchEnable){
            return;
        }
        final Stub stub = getStub();
        if (stub != null) {
            if (stub.relaunch()) {
                getLogger().d("[ContainerDrawable]relaunch: key:" + stub.getKey());
                return;
            }
        }
        getLogger().d("[ContainerDrawable]error when drawing, relaunch failed");
    }

    private TLogger getLogger(){
        final Stub stub = getStub();
        if (stub != null){
            return stub.getLogger();
        }
        return TLogger.getNullLogger();
    }

    private Stub getStub(){
        if (stub != null){
            return stub.get();
        }
        return null;
    }

}