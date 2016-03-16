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
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.lang.ref.WeakReference;

import sviolet.turquoise.x.imageloader.task.Task;

/**
 * this drawable used to launch Task (load image)
 *
 * Created by S.Violet on 2016/3/16.
 */
public class LaunchDrawable extends Drawable {

    private WeakReference<Task> task;

    public LaunchDrawable(Task task){
        this.task = new WeakReference<>(task);
    }

    @Override
    public void draw(Canvas canvas) {
        final Task task = getTask();
        if (task != null){
            if (task.getState() == Task.State.INITIALIZED){
                task.load();
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return -1;//match parent
    }

    @Override
    public int getIntrinsicHeight() {
        return -1;//match parent
    }

    private Task getTask(){
        if (task != null){
            return task.get();
        }
        return null;
    }

}
