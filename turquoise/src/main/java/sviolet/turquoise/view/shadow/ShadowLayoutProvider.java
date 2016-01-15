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

package sviolet.turquoise.view.shadow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;

import sviolet.turquoise.R;
import sviolet.turquoise.util.sys.ApplicationUtils;
import sviolet.turquoise.util.sys.MeasureUtils;

/**
 * ShadowLayout逻辑实现
 * 
 * @author S.Violet
 */
@Deprecated
public abstract class ShadowLayoutProvider {

	private static final float DEF_SCALE = 0.96f;
	private static final int DEF_BACKGROUND = 0xFFFFFFFF;
	private static final int DEF_ALPHA = 128;
	private static final int DEF_RADIUS = 1;//dp
	private static final int DEF_COLOR = 0xFF000000;
	private static final int DEF_SHADOW_OFFSET_X = 0;//dp
	private static final int DEF_SHADOW_OFFSET_Y = 1;//dp
	
	private int background;//背景颜色
	
	private float scale;//内容缩放比例
	private int color;//阴影颜色
	private int alpha;//阴影透明度
	private int radius;//阴影半径
	private int shadowOffsetX;//阴影位移
	private int shadowOffsetY;//阴影位移
	
	//初始化完成
	private boolean isInitComplete = false;
	
	private Paint shadowPaint;
	private RectF shadowRect = new RectF();
	
	private ViewGroup mViewGroup;
	
	public ShadowLayoutProvider(ViewGroup viewGroup){
		this.mViewGroup = viewGroup;
	}
	
	/***************************************************
	 * abstract
	 */
	
	protected abstract int getShadowOffsetXStyleable();
	
	protected abstract int getShadowOffsetYStyleable();
	
	/****************************************************
	 * init
	 */
	
	protected void init(Context context, AttributeSet attrs){
		//禁用硬件加速
		ApplicationUtils.disableHardwareAccelerated(mViewGroup);
		//初始化参数
		initParams(context, attrs);
		//初始化画笔
		initPaint();
		//等待测量后完成初始化
		postInit();
	}
	
	/**
	 * 初始化参数
	 * @param context
	 * @param attrs
	 */
	private void initParams(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LinearShadowLayout);
		scale = typedArray.getFloat(R.styleable.LinearShadowLayout_scale, DEF_SCALE);
		background = typedArray.getColor(R.styleable.LinearShadowLayout_backgroundColor, DEF_BACKGROUND);
		color = typedArray.getColor(R.styleable.LinearShadowLayout_colour, DEF_COLOR);
		alpha = typedArray.getInt(R.styleable.LinearShadowLayout_alpha, DEF_ALPHA);
		radius = typedArray.getDimensionPixelOffset(R.styleable.LinearShadowLayout_radius, MeasureUtils.dp2px(mViewGroup.getContext(), DEF_RADIUS));
		shadowOffsetX = typedArray.getDimensionPixelOffset(
				getShadowOffsetXStyleable(), MeasureUtils.dp2px(mViewGroup.getContext(), DEF_SHADOW_OFFSET_X));
		shadowOffsetY = typedArray.getDimensionPixelOffset(
				getShadowOffsetYStyleable(), MeasureUtils.dp2px(mViewGroup.getContext(), DEF_SHADOW_OFFSET_Y));
        typedArray.recycle();
	}
	
	/**
	 * 初始化画笔
	 */
	private void initPaint(){
		shadowPaint= new Paint();
		shadowPaint.setAntiAlias(true);//抗锯齿
		shadowPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_OVER));//混合模式:不同的模式会有不同效果
	}
	
	/**
	 * 待Mesture完成后初始化
	 */
	private void postInit(){
		//View获取自身宽高等参数方法
		//绘制监听器, 也可以使用addOnGlobalLayoutListener监听layout事件
		mViewGroup.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				//移除监听器, 以免重复调用
				mViewGroup.getViewTreeObserver().removeOnPreDrawListener(this); 
                isInitComplete = true;
                mViewGroup.postInvalidate();
				return true;
			}
		});
	}
	
    /********************************************************
     * protected
     */
	
	/**
	 * 设置阴影参数
	 */
	protected void setShadowLayer(){
		shadowPaint.setAlpha(alpha);
		shadowPaint.setShadowLayer(radius, shadowOffsetX, shadowOffsetY, color); //设置阴影层
	}
	
    /**
     * 绘制阴影
     * 
     * @param canvas
     */
	protected void drawShadow(Canvas canvas){
		//初始化完成后绘制阴影
		if(isInitComplete){
	    	canvas.scale(scale, scale, mViewGroup.getWidth() / 2, mViewGroup.getHeight() / 2);
	        canvas.drawColor(background);//设置画布颜色
	        shadowRect.set(0, 0, mViewGroup.getWidth(), mViewGroup.getHeight());//矩形
	        canvas.drawRoundRect(shadowRect, 0, 0, shadowPaint);//绘制矩形(带阴影)
		}
    }
	
}
