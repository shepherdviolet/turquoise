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

package sviolet.turquoise.ui.view.shadow;

import sviolet.turquoise.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

/**
 * 线性渐变阴影<br>
 * <br>
 * 示例1:在LinearLayout中使用阴影<br>
 * 线性布局在被占满后, 会扩展到外面, 因此只需加在match_parent的控件后即可<br>
 * <br>
 * <pre>{@code
 *	<sviolet.turquoise.view.slide.view.LinearLayoutDrawer
 *		android:id="@+id/left_drawer"
 *		android:layout_width="match_parent"
 *		android:layout_height="match_parent"
 *		android:background="#00000000"
 *		android:orientation="horizontal"
 *		android:scrollX="10000dp"
 *		android:scrollY="10000dp">
 *
 *		<ListView
 *			android:id="@+id/left_drawer_listview"
 *			android:layout_width="match_parent"
 *			android:layout_height="match_parent"
 *			android:background="#FFC0C0C0"/>
 *
 *		<!-- 线性渐变阴影 -->
 *		<sviolet.turquoise.view.shadow.LinearShadowView
 *			android:layout_width="10dp"
 *			android:layout_height="match_parent"
 *			sviolet:color="#50000000"
 *			sviolet:LinearShadowView_direction="right"/>
 *
 *	 </sviolet.turquoise.view.slide.view.LinearLayoutDrawer>
 * }</pre><br>
 * <br>
 * 示例2:在RelativeLayout中使用阴影<br>
 * 关系布局在被占满后, 若使用诸如toRightOf在右侧添加控件, 会导致显示不出来,
 * 因此必须使用alignRight与之右对齐, 然后用marginRight将阴影移出去, 才能在
 * 超出屏幕范围外的地方放置阴影<br>
 * <br>
 * <pre>{@code
 *	<sviolet.turquoise.view.slide.view.RelativeLayoutDrawer
 *		android:id="@+id/left_drawer"
 *		android:layout_width="match_parent"
 *		android:layout_height="match_parent"
 *		android:background="#00000000"
 *		android:scrollX="10000dp"
 *		android:scrollY="10000dp">
 *
 *		<ListView
 *			android:id="@+id/left_drawer_listview"
 *			android:layout_width="match_parent"
 *			android:layout_height="match_parent"
 *			android:background="#FFC0C0C0"/>
 *
 *		<!-- 线性渐变阴影 -->
 *		<sviolet.turquoise.view.shadow.LinearShadowView
 *			android:layout_width="10dp"
 *			android:layout_height="match_parent"
 *			android:layout_alignRight="@id/left_drawer_listview"
 *			android:layout_marginRight="-10dp"
 *			sviolet:color="#50000000"
 *			sviolet:LinearShadowView_direction="right"/>
 *
 *		<!-- 线性渐变阴影 -->
 *		<sviolet.turquoise.view.shadow.LinearShadowView
 *			android:layout_width="10dp"
 *			android:layout_height="match_parent"
 *			android:layout_alignLeft="@id/left_drawer_listview"
 *			android:layout_marginLeft="-10dp"
 *			sviolet:color="#50000000"
 *			sviolet:LinearShadowView_direction="left"/>
 *
 *	</sviolet.turquoise.view.slide.view.RelativeLayoutDrawer>
 * }</pre>
 * 
 * @author S.Violet
 */

public class LinearShadowView extends View {

	//默认颜色
	private static final int COLOR_DEF = 0x80000000;
	
	//阴影方向
	private ShadowDirection direction;
	//阴影颜色
	private int color = 0x00000000;
	//初始化完成标志
	private boolean isInitComplete = false;
	
	private Paint shadowPaint;
	private RectF shadowRect = new RectF();
	
	public LinearShadowView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public LinearShadowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public LinearShadowView(Context context) {
		super(context);
	}

	/****************************************************
	 * init
	 */
	
	private void init(Context context, AttributeSet attrs){
		//禁用硬件加速
//		SettingUtils.disableHardwareAccelerated(this);
		//初始化参数
		initParams(context, attrs);
		//初始化画笔
		initPaint();
		//等待尺寸测量
		postInit();
	}

	/**
	 * 初始化参数
	 * @param context
	 * @param attrs
	 */
	private void initParams(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LinearShadowView);
		color = typedArray.getColor(R.styleable.LinearShadowView_android_color, COLOR_DEF);
		direction = ShadowDirection.values()[
		                               typedArray.getInt(R.styleable.LinearShadowView_LinearShadowView_direction, ShadowDirection.DIRECTION_BOTTOM.getValue())];
        typedArray.recycle();
	}
	
	/**
	 * 初始化画笔
	 */
	private void initPaint(){
		shadowPaint= new Paint();
		shadowPaint.setAntiAlias(true);//抗锯齿
		setShader();  
	}
	
	/**
	 * 待Measure完成后初始化
	 */
	private void postInit(){
		//View获取自身宽高等参数方法
		//绘制监听器, 也可以使用addOnGlobalLayoutListener监听layout事件
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				//移除监听器, 以免重复调用
				getViewTreeObserver().removeOnPreDrawListener(this); 
                isInitComplete = true;
                postInvalidate();
                return true;
            }
        });
	}
	
	/********************************************************
	 * override
	 */
	
	@Override
	public void invalidate() {
		setShader();
		super.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//初始化完成后绘制阴影
		if(isInitComplete) {
            drawShadow(canvas);
        }
	}

	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		postInvalidate();
	}

	/********************************************************
     * private
     */
    
	/**
	 * 设置渐变参数
	 */
	private void setShader() {
		
		if(shadowPaint == null) {
            return;
        }
			
		Shader mShader;
		switch(direction){
		case DIRECTION_TOP:
			mShader = new LinearGradient(0, getHeight(), 0, 0, new int[]{color, 0x00000000}, new float[]{0, 1}, Shader.TileMode.CLAMP);
			break;
		case DIRECTION_BOTTOM:
			mShader = new LinearGradient(0, 0, 0, getHeight(), new int[]{color, 0x00000000}, new float[]{0, 1}, Shader.TileMode.CLAMP);
			break;
		case DIRECTION_LEFT:
			mShader = new LinearGradient(getWidth(), 0, 0, 0, new int[]{color, 0x00000000}, new float[]{0, 1}, Shader.TileMode.CLAMP);
			break;
		case DIRECTION_RIGHT:
			mShader = new LinearGradient(0, 0, getWidth(), 0, new int[]{color, 0x00000000}, new float[]{0, 1}, Shader.TileMode.CLAMP);
			break;
		default:
			mShader = new LinearGradient(0, 0, 0, 0, 0, 0, Shader.TileMode.CLAMP);
			break;
		}
		shadowPaint.setShader(mShader);
	}
	
    /**
     * 绘制阴影
     * 
     * @param canvas
     */
    private void drawShadow(Canvas canvas){
    	
    	if(shadowPaint == null) {
            return;
        }
    		
    	shadowRect.set(0, 0, getWidth(), getHeight());//矩形
    	canvas.drawRoundRect(shadowRect, 0f, 0f, shadowPaint);//绘制矩形(带阴影)
    }
	
    /********************************************************
     * public
     */
    
    /**
     * 设置阴影颜色
     * @param color
     */
    public void setColor(int color){
    	this.color = color;
    	postInvalidate();
    }
    
    /**
     * 设置阴影方向<Br>
     * <br>
     * {@link Direction}
     * 
     * @param direction
     */
    public void setDirection(ShadowDirection direction){
    	this.direction = direction;
    	postInvalidate();
    }
    
    /********************************************************
     * class
     */
    
	/**
	 * 阴影方向
	 */
	public enum ShadowDirection {
		DIRECTION_TOP (0),
		DIRECTION_BOTTOM (1),
		DIRECTION_LEFT (2),
		DIRECTION_RIGHT (3);
		
		private int value;
		
		ShadowDirection(int value){
			this.value = value;
		}
		
		public int getValue(){
			return value;
		}
		
	}
    
}
