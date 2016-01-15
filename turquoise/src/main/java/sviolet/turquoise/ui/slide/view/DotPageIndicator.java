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

package sviolet.turquoise.ui.slide.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import sviolet.turquoise.R;
import sviolet.turquoise.ui.slide.SlideIndicator;

/**
 * 点状翻页指示器
 * 
 * @author S.Violet
 */
public class DotPageIndicator extends View implements SlideIndicator {

	private static final int COLOR_DEF = 0x70FFFFFF;
	private static final int COLOR_HIGH_LIGHT_DEF = 0xC0FFFFFF;
	private static final int RADIUS_DEF = 5;
	private static final int INTERVAL_DEF = 15;
	
	//配置
	private int color = COLOR_DEF;//圆点颜色
	private int colorHighLight = COLOR_HIGH_LIGHT_DEF;//当前页圆点颜色
	private int radius = RADIUS_DEF;//圆点半径
	private int interval = INTERVAL_DEF;//点间隔
	private int quantity = 0;//页数
	
	//变量
	private int page = 0;
	private Paint paint;//圆点画笔
	private Paint paintHighLight;//当前页圆点画笔
	
	public DotPageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public DotPageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public DotPageIndicator(Context context) {
		super(context);
	}
	
	/************************************************************
	 * init
	 */
	
	/**
	 * 初始化
	 * @param context
	 * @param attrs
	 */
	private void init(Context context, AttributeSet attrs){
		initParams(context, attrs);
		initPaint();
	}

	/**
	 * 初始化参数
	 * @param context
	 * @param attrs
	 */
	private void initParams(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DotPageIndicator);
		color = typedArray.getColor(R.styleable.DotPageIndicator_colour, COLOR_DEF);
		colorHighLight = typedArray.getColor(R.styleable.DotPageIndicator_highLightColor, COLOR_HIGH_LIGHT_DEF);
		radius = typedArray.getDimensionPixelOffset(R.styleable.DotPageIndicator_radius, RADIUS_DEF);
		interval = typedArray.getDimensionPixelOffset(R.styleable.DotPageIndicator_interval, INTERVAL_DEF);
		quantity = typedArray.getInt(R.styleable.DotPageIndicator_quantity, 0);
        typedArray.recycle();
	}
	
	/**
	 * 初始化画笔
	 */
	private void initPaint() {
		paint = new Paint();
		paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        
		paintHighLight = new Paint();
		paintHighLight.setColor(colorHighLight);
		paintHighLight.setAntiAlias(true);
		paintHighLight.setStyle(Paint.Style.FILL);
	}
	
	/********************************************************
	 * public
	 */
	
	/**
	 * 设置当前页数
	 */
	@Override
	public void setStage(float stage){
		this.page = (int) (stage + 0.5f);//四舍五入
		postInvalidate();
	}
	
	/**
	 * 设置总页数
	 */
	@Override
	public void setQuantity(int quantity){
		this.quantity = quantity;
		postInvalidate();
	}
	
	/********************************************************
	 * override
	 */
	
	/**
	 * 绘制逻辑
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		if(quantity <= 0 || radius <= 0 || interval < 0)
			return;
		
		int width = getWidth();
		int height = getHeight();
		int length = (quantity - 1) * interval;
		
		int x = width / 2 - length / 2;
		int y = height / 2;
		
		for(int i = 0 ; i < quantity ; i++){
			canvas.drawCircle(x, y, radius, i == page ? paintHighLight : paint);
			x += interval;
		}
	}
	
}
