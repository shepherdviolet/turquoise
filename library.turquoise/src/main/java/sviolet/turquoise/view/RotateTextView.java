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

package sviolet.turquoise.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import sviolet.turquoise.R;

/**
 * 可旋转角度的TextView
 * @author S.Violet
 *
 */
public class RotateTextView extends TextView {

	private float angle = 0;
//	private PaintFlagsDrawFilter paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG);//抗锯齿
	
	public RotateTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public RotateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RotateTextView(Context context) {
		super(context);
	}
	
	/**
	 * 初始化
	 * @param context
	 * @param attrs
	 */
	private void init(Context context, AttributeSet attrs){
		TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.RotateTextView);
		angle = types.getFloat(R.styleable.RotateTextView_angle, 0f);
		types.recycle();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
//		canvas.setDrawFilter(paintFlagsDrawFilter);
		canvas.rotate(angle, getMeasuredWidth()/2, getMeasuredHeight()/2);  
		super.onDraw(canvas);
	}
	
	/**
	 * 设置旋转角度
	 * @param angle
	 */
	public void setAngle(float angle){
		this.angle = angle % 360;
		postInvalidate();
	}

	/**
	 * 获取旋转角度
	 * @return
	 */
	public float getAngle(){
		return angle;
	}
	
}
