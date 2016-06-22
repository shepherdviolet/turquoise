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

package sviolet.turquoise.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

import sviolet.turquoise.R;

/**
 * 
 * 可设置旋转角度的ImageView<<destroy()销毁>><p>
 *
 * rotate()调整图片角度<p>
 *
 * xmlns:sviolet="http://schemas.android.com/apk/res/应用包名
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */
public class RotateImageView extends ImageView {

	private Context context;
	private int resId;//图像id
	private Bitmap originBitmap;//原始图(未缩放)
	
	private Bitmap bitmap;//原始图(经缩放)
	private Bitmap tempBitmap;//高质量图缓存(经旋转)
	private Matrix matrix = new Matrix();
	
	private double viewRotate = 0;//图像当前旋转角度
	private boolean imageRefresh = false;//true:图片需要重新加载(init)
	private boolean highQuality = false;//高质量旋转绘图(耗资源)
	
	public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init(attrs);
	}

	public RotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(attrs);
	}

	public RotateImageView(Context context) {
		super(context);
		this.context = context;
	}

	/**
	 * 设置图片(一张大图请放置nodpi)
	 * 
	 * @param resId
	 */
	public void setSrc(int resId){
		this.resId = resId;
		this.originBitmap = null;
		imageRefresh = true;
	}
	
	/**
	 * 设置图片
	 * 
	 * @param bitmap
	 */
	public void setSrc(Bitmap bitmap){
		this.resId = 0;
		this.originBitmap = bitmap;
		imageRefresh = true;
	}
	
	/**
	 * 初始化设置(预初始化)
	 * @param attrs
	 */
	private void init(AttributeSet attrs){
		// 获取控件参数
		TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.RotateImageView);
		resId = types.getResourceId(R.styleable.RotateImageView_android_src, 0);
		types.recycle();
		imageRefresh = true;
	}
	
	/**
	 * 实际初始化操作
	 */
	private void initDo(){
		if(!imageRefresh || !hasDisplay())
			return;
		
		imageRefresh = false;
		
		Bitmap oldBitmap = bitmap;
		
		if(this.resId != 0){
			originBitmap = BitmapFactory.decodeStream(context.getResources().openRawResource(resId));
			bitmap = Bitmap.createScaledBitmap(originBitmap, getWidth(), getHeight(), true);
			originBitmap.recycle();
			originBitmap = null;
		}else if(originBitmap != null){
			bitmap = Bitmap.createScaledBitmap(originBitmap, getWidth(), getHeight(), true);
			originBitmap.recycle();
			originBitmap = null;
		}else{
			return;
		}
		
		if(oldBitmap != null && !oldBitmap.isRecycled()){
			oldBitmap.recycle();
			oldBitmap = null;
		}
	}
	
	/**
	 * 旋转(低质量绘图)
	 * 
	 * @param rotate
	 */
	public void rotate(double rotate){
		this.viewRotate = rotate;
		this.highQuality = false;
		postInvalidate();
	}
	
	/**
	 * 旋转(可选高质量绘图)<p>
	 * 高质量绘图会消耗更多资源,执行较慢,建议动画停止帧使用高质量绘图
	 * 
	 * @param rotate
	 * @param highQuality true高质量绘图
	 */
	public void rotate(double rotate, boolean highQuality){
		this.viewRotate = rotate;
		this.highQuality = highQuality;
		postInvalidate();
	}
	
	/**
	 * 销毁
	 */
	public void destroy(){
		if(bitmap != null && !bitmap.isRecycled())
			bitmap.recycle();
		if(originBitmap != null && !originBitmap.isRecycled())
			originBitmap.recycle();
		if(tempBitmap != null && !tempBitmap.isRecycled())
			tempBitmap.recycle();
	}
	
	/**
	 * 实际初始化
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		initDo();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(bitmap != null && hasDisplay()){
			drawBitmap(canvas);
		}
	}

	/**
	 * 绘制图片
	 * @param canvas
	 */
	private void drawBitmap(Canvas canvas) {
		if(highQuality){//高质量绘图(慢)
			//算法旋转,图片会增大(参考PhotoShop旋转)
			matrix.reset();
			matrix.setRotate((float)viewRotate % 360, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
			tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			//绘制图片(图片增大,需要调整位置)
			float left = -((float)tempBitmap.getWidth() - (float)bitmap.getWidth()) / 2;
			float top = -((float)tempBitmap.getHeight() - (float)bitmap.getHeight()) / 2;
			canvas.drawBitmap(tempBitmap, left, top, null);
		}else{//低质量绘图(快)
			//绘制图片时旋转
			matrix.reset();
			matrix.setRotate((float)viewRotate % 360, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
			canvas.drawBitmap(bitmap, matrix, null);
		}
	}
	
	private boolean hasDisplay(){
		return getWidth() != 0 && getHeight() != 0;
	}
}
