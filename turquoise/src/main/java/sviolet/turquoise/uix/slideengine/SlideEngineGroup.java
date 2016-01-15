/*
 * Copyright (C) 2015 S.Violet
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

package sviolet.turquoise.uix.slideengine;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sviolet.turquoise.common.exception.DeprecatedException;

/**
 * 
 * SlideEngine组, 滑动引擎组<p/>
 * 
 * 适用于GestureDriver和SlideEngine一对多的情况, 塞入复数个SlideEngine, 
 * GestrueDrive的输出由此引擎组分发到其中一个SlideEngine<p/>
 * 
 * [View] TouchEvent--> [GestureDrive] <--bind--> [SlideEngineGroup] <--add [SlideEngine] notify--> [SlideView]<p/>
 * 
 * SlideEngineGroup对于GestureDriver是一个虚拟的SlideEngine, 对于SlideEngine是一个虚拟的GestureDriver,
 * 但SlideEngineGroup实现GestureDriver接口不完整, 部分方法不可调用.<p/>
 * 
 * @author S.Violet
 *
 */

public abstract class SlideEngineGroup implements SlideEngine, GestureDriver {
	
	protected GestureDriver mGestureDriver = null;
	protected Map<String, SlideEngine> mSlideEngines = null;
	
	protected List<String> onGestureDriveEngines = new ArrayList<String>();
	protected List<String> onGestureHoldEngines = new ArrayList<String>();
	protected List<String> onGestureReleaseEngines = new ArrayList<String>();
	protected List<String> onIsSlidingEngines = new ArrayList<String>();

	private SlideEngine parentSlideEngine = null;//外部引擎（嵌套时）
	
	/***************************************************
	 * override SlideEngine
	 */
	
	/**
	 * 销毁
	 */
	@Override
	public void destroy() {
		
	}
	
	//绑定/////////////////////////////
	
	/**
	 * 绑定GestureDriver
	 */
	@Override
	public void bind(GestureDriver gestureDriver) {
		onBind(gestureDriver);
		if(mGestureDriver != null)
			mGestureDriver.onBind(this);
	}

	@Override
	public void onBind(GestureDriver gestureDriver) {
		mGestureDriver = gestureDriver;
	}

	//输入输出分发/////////////////////////////
	
	@Override
	public void onGestureDrive(int x, int y, int offsetX, int offsetY, int velocityX, int velocityY) {
		onGestureDriveEngines.clear();
		dispatchGestureDrive(onGestureDriveEngines, x, y, offsetX, offsetY, velocityX, velocityY);
		if(onGestureDriveEngines != null && onGestureDriveEngines.size() > 0)
			for(String alias : onGestureDriveEngines)
				if(mSlideEngines.containsKey(alias))
					handleGestureDrive(mSlideEngines.get(alias), alias, x, y, offsetX, offsetY, velocityX, velocityY);
	}

	@Override
	public void onGestureDrive(int curr, int offset, int velocity) {
		onGestureDriveEngines.clear();
		dispatchGestureDrive(onGestureDriveEngines, curr, offset, velocity);
		if(onGestureDriveEngines != null && onGestureDriveEngines.size() > 0)
			for(String alias : onGestureDriveEngines)
				if(mSlideEngines.containsKey(alias))
					handleGestureDrive(mSlideEngines.get(alias), alias, curr, offset, velocity);
	}
	
	@Override
	public void onGestureHold() {
		onGestureHoldEngines.clear();
		dispatchGestureHold(onGestureHoldEngines);
		if(onGestureHoldEngines != null && onGestureHoldEngines.size() > 0)
			for(String alias : onGestureHoldEngines)
				if(mSlideEngines.containsKey(alias))
					handleGestureHold(mSlideEngines.get(alias), alias);
	}

	@Override
	public void onGestureRelease(int velocity) {
		onGestureReleaseEngines.clear();
		dispatchGestureRelease(onGestureReleaseEngines, velocity);
		if(onGestureReleaseEngines != null && onGestureReleaseEngines.size() > 0)
			for(String alias : onGestureReleaseEngines)
				if(mSlideEngines.containsKey(alias))
					handleGestureRelease(mSlideEngines.get(alias), alias, velocity);
	}

	@Override
	public boolean isSliding() {
		onIsSlidingEngines.clear();
		dispatchIsSliding(onIsSlidingEngines);
		if(onIsSlidingEngines != null && onIsSlidingEngines.size() > 0)
			for(String alias : onIsSlidingEngines)
				if(mSlideEngines.containsKey(alias))
					if(handleIsSliding(mSlideEngines.get(alias), alias))
						return true;
		return false;
	}

	/**
	 * 
	 * 添加一个内部引擎<p/>
	 *
	 * 作用:<br/>
	 * 1.内部引擎拦截到事件后, 会调用外部引擎对应驱动的skipIntercept()方法,
	 *      阻断外部引擎的本次事件拦截, 防止内部控件滑动时被外部拦截<br/>
	 * 
	 * @param slideEngine 内部引擎
	 */
	@Override
	public void addInnerEngine(SlideEngine slideEngine) {
		if (slideEngine != null)
			slideEngine.setParentEngine(this);
	}

	@Override
	public void setParentEngine(SlideEngine slideEngine) {
		this.parentSlideEngine = slideEngine;
	}

	@Override
	public SlideEngine getParentEngine() {
		return parentSlideEngine;
	}

	@Override
	public GestureDriver getGestureDriver() {
		return mGestureDriver;
	}

	@Override
	public void skipIntercepted() {
		//手势驱动器跳过本次拦截
		if (getGestureDriver() != null)
			getGestureDriver().skipIntercepted();
		//外部驱动跳过本次拦截
		if (parentSlideEngine != null)
			parentSlideEngine.skipIntercepted();
	}

	@Override
	@Deprecated
	public void onStaticTouchAreaCaptureEscapedTouch() {
	}

	@Override
	@Deprecated
	public void onStaticTouchAreaClick() {
	}

	@Override
	@Deprecated
	public void onStaticTouchAreaLongPress() {
	}
	
	/***************************************************
	 * override GestureDriver
	 */
	
	/**
	 * 获得手势驱动的状态
	 * @return
	 */
	@Override
	public int getState() {
		if(mGestureDriver != null)
			return mGestureDriver.getState();
		return 0;
	}
	
	/**
	 * SlideEngineGroup不是一个真正的GestureDriver, 
	 * 不能使用bind绑定SlideEngine, 请用addSlideEngine()方法添加
	 * @param slideEngine
	 */
	@Deprecated
	@Override
	public void bind(SlideEngine slideEngine) {
		throw new DeprecatedException("[SlideEngineGroup]group can't bind SlideEngine, please use addSlideEngine()");
	}

	@Deprecated
	@Override
	public void onBind(SlideEngine slideEngine) {
	}

	/**
	 * 
	 * SlideEngineGroup不是一个真正的GestureDriver, 
	 * 不能调用他的onInterceptTouchEvent()方法, 
	 * 请将它与一个GestureDriver绑定后使用
	 * 
	 * @param event
	 * @return
	 */
	@Deprecated
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		throw new DeprecatedException("[SlideEngineGroup]group can't call onInterceptTouchEvent()");
	}

	/**
	 * 
	 * SlideEngineGroup不是一个真正的GestureDriver, 
	 * 不能调用它的onTouchEvent()方法, 
	 * 请将它与一个GestureDriver绑定后使用
	 * 
	 * @param event
	 * @return
	 */
	@Deprecated
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		throw new DeprecatedException("[SlideEngineGroup]group can't call onTouchEvent()");
	}
	
	/***************************************************
	 * 待实现
	 */
	
	//分发////////////////////////////////////

	/**
	 * 
	 * [实现]通用分发方法<p/>
	 * 
	 * 若对应事件的dispatch......方法未复写, 则通过此通用方法分发<p/>
	 * 
	 * @param handlingEngines 本次需要处理事件的Engine别名列表.
	 * 		例:若需要分发给"a","b"两个引擎, 
	 * 			handlingEngines.add("a");
	 * 			handlingEngines.add("b");
	 * 		这两个引擎必须提前添加到SlideEngineGroup中.
	 */
	public abstract void dispatch(List<String> handlingEngines);

	/**
	 * 
	 * [实现]分发二维OnGestureDrive事件<p/>
	 * 
	 * 若不复写该方法, 则通过dispatch()通用方法分发<p/>
	 * 
	 * @param handlingEngines 本次需要处理事件的Engine别名列表.
	 * 		例:若需要分发给"a","b"两个引擎, 
	 * 			handlingEngines.add("a");
	 * 			handlingEngines.add("b");
	 * 		这两个引擎必须提前添加到SlideEngineGroup中.
	 */
	public void dispatchGestureDrive(List<String> handlingEngines, int x, int y, int offsetX, int offsetY, int velocityX, int velocityY){
		dispatch(handlingEngines);
	}
	
	/**
	 * 
	 * [实现]分发一维OnGestureDrive事件<p/>
	 * 
	 * 若不复写该方法, 则通过dispatch()通用方法分发<p/>
	 * 
	 * @param handlingEngines 本次需要处理事件的Engine别名列表.
	 * 		例:若需要分发给"a","b"两个引擎, 
	 * 			handlingEngines.add("a");
	 * 			handlingEngines.add("b");
	 * 		这两个引擎必须提前添加到SlideEngineGroup中.
	 */
	public void dispatchGestureDrive(List<String> handlingEngines, int curr, int offset, int velocity){
		dispatch(handlingEngines);
	}
	
	/**
	 * 
	 * [实现]分发onGestureHold事件<p/>
	 * 
	 * 若不复写该方法, 则通过dispatch()通用方法分发<p/>
	 * 
	 * @param handlingEngines 本次需要处理事件的Engine别名列表.
	 * 		例:若需要分发给"a","b"两个引擎, 
	 * 			handlingEngines.add("a");
	 * 			handlingEngines.add("b");
	 * 		这两个引擎必须提前添加到SlideEngineGroup中.
	 */
	public void dispatchGestureHold(List<String> handlingEngines) {
		dispatch(handlingEngines);
	}
	
	/**
	 * 
	 * [实现]分发onGestureRelease事件<p/>
	 * 
	 * 若不复写该方法, 则通过dispatch()通用方法分发<p/>
	 * 
	 * @param handlingEngines 本次需要处理事件的Engine别名列表.
	 * 		例:若需要分发给"a","b"两个引擎, 
	 * 			handlingEngines.add("a");
	 * 			handlingEngines.add("b");
	 * 		这两个引擎必须提前添加到SlideEngineGroup中.
	 */
	public void dispatchGestureRelease(List<String> handlingEngines, int velocity) {
		dispatch(handlingEngines);
	}
	
	/**
	 * 
	 * [实现]分发isSliding事件<p/>
	 * 
	 * 若不复写该方法, 则通过dispatch()通用方法分发<p/>
	 * 
	 * @param handlingEngines 本次需要处理事件的Engine别名列表.
	 * 		例:若需要分发给"a","b"两个引擎, 
	 * 			handlingEngines.add("a");
	 * 			handlingEngines.add("b");
	 * 		这两个引擎必须提前添加到SlideEngineGroup中.
	 */
	public void dispatchIsSliding(List<String> handlingEngines) {
		dispatch(handlingEngines);
	}
	
	/**
	 * 
	 * [实现]分发otherIntercepted事件<p/>
	 * 
	 * 若不复写该方法, 则通过dispatch()通用方法分发<p/>
	 * 
	 * @param handlingEngines 本次需要处理事件的Engine别名列表.
	 * 		例:若需要分发给"a","b"两个引擎, 
	 * 			handlingEngines.add("a");
	 * 			handlingEngines.add("b");
	 * 		这两个引擎必须提前添加到SlideEngineGroup中.
	 */
	public void dispatchOtherIntercepted(List<String> handlingEngines) {
		dispatch(handlingEngines);
	}
	
	//处理////////////////////////////////////
	
	/**
	 * 
	 * [实现]处理二维OnGestureDrive事件<p/>
	 * 
	 * 若不复写该方法, 默认直接调用SlideEngine.OnGestureDrive(...);<p/>
	 * 
	 */
	public void handleGestureDrive(SlideEngine engine, String alias, int x, int y, int offsetX, int offsetY, int velocityX, int velocityY){
		engine.onGestureDrive(x, y, offsetX, offsetY, velocityX, velocityY);
	}
	
	/**
	 * 
	 * [实现]处理一维OnGestureDrive事件<p/>
	 * 
	 * 若不复写该方法, 默认直接调用SlideEngine.OnGestureDrive(...);<p/>
	 * 
	 */
	public void handleGestureDrive(SlideEngine engine, String alias, int curr, int offset, int velocity){
		engine.onGestureDrive(curr, offset, velocity);
	}
	
	/**
	 * 
	 * [实现]处理onGestureHold事件<p/>
	 * 
	 * 若不复写该方法, 默认直接调用SlideEngine.onGestureHold();<p/>
	 * 
	 */
	public void handleGestureHold(SlideEngine engine, String alias) {
		engine.onGestureHold();
	}
	
	/**
	 * 
	 * [实现]处理onGestureRelease事件<p/>
	 * 
	 * 若不复写该方法, 默认直接调用SlideEngine.onGestureRelease(velocity);<br/>
	 * 若该引擎本次未被驱动(onGestureDrive), 则SlideEngine.onGestureRelease(0);<br/>
	 * 
	 */
	public void handleGestureRelease(SlideEngine engine, String alias, int velocity) {
		//若改引擎未被驱动, 则释放时速度为0
		if(onGestureDriveEngines.contains(alias))
			engine.onGestureRelease(velocity);
		else
			engine.onGestureRelease(0);
	}
	
	/**
	 * 
	 * [实现]处理IsSliding事件<p/>
	 * 
	 * 若不复写该方法, 默认直接调用SlideEngine.isSliding();<p/>
	 * 
	 */
	public boolean handleIsSliding(SlideEngine engine, String alias) {
		return engine.isSliding();
	}

	/***************************************************
	 * public
	 */
	
	/**
	 * 添加一个SlideEngine并设置别名
	 * @param slideEngine
	 * @param alias 别名
	 */
	public void addSlideEngine(SlideEngine slideEngine, String alias){
		if(slideEngine != null && alias != null){
			getAllSlideEngines().put(alias, slideEngine);
			slideEngine.bind(this);
		}
	}
	
	/**
	 * 添加多个SlideEngine
	 * @param slideEngines Map<别名, SlideEngine>
	 */
	public void addSlideEngine(Map<String, SlideEngine> slideEngines){
		if(slideEngines != null && slideEngines.size() > 0)
			getAllSlideEngines().putAll(slideEngines);
	}
	
	/**
	 * 移除所有SlideEngine
	 */
	public void removeAllSlideEngine(){
		getAllSlideEngines().clear();
	}
	
	/**
	 * 通过别名取得其中一个SlideEngine
	 * @param alias
	 * @return 可能为null
	 */
	public SlideEngine getSlideEngine(String alias){
		if(alias == null)
			return null;
		return getAllSlideEngines().get(alias);
	}
	
	/**
	 * 获得Group中所有的SlideEngine
	 * @return
	 */
	public Map<String, SlideEngine> getAllSlideEngines(){
		if(mSlideEngines == null)
			mSlideEngines = new HashMap<String, SlideEngine>();
		return mSlideEngines;
	}
	
	/**
	 * 获得Group中所有SlideEngine的别名列表
	 * @return
	 */
	public List<String> getAllSlideEngineAliases(){
		List<String> aliases = new ArrayList<String>();
		for(Entry<String, SlideEngine> entry : getAllSlideEngines().entrySet()){
			aliases.add(entry.getKey());
		}
		return aliases;
	}
	
	/**********************************************
	 * private
	 */
	
}
