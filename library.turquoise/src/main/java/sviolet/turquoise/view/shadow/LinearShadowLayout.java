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

import sviolet.turquoise.R;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 带阴影效果的LinearLayout<br>
 * <br>
 * 绘制效率较低,慎用<Br>
 * 该控件禁用了硬件加速, 以实现setShadowLayer<Br>
 * <br>
 * 实例:<br>
 * <br>
 * <pre>{@code
 *  <!-- 若android:background未设置, 则有可能显示无效 -->
 *  <sviolet.turquoise.view.shadow.LinearShadowLayout
 *      android:layout_width="match_parent"
 *      android:layout_height="match_parent"
 *      android:background="#209090"
 *      sviolet:padding="6dp"
 *      sviolet:backgroundColor="#FFF0F0F0"
 *      sviolet:color="#FF000000"
 *      sviolet:alpha="200"
 *      sviolet:radius="4dp"
 *      sviolet:LinearShadowLayout_shadowOffsetX="1dp"
 *      sviolet:LinearShadowLayout_shadowOffsetY="1dp">
 *
 *      <TextView
 *          android:layout_width="wrap_content"
 *          android:layout_height="wrap_content"
 *          android:text="@string/hello_world" />
 *
 *  </sviolet.turquoise.view.shadow.LinearShadowLayout>
 * }</pre>
 *
 * @author S.Violet
 *
 */
@Deprecated
public class LinearShadowLayout extends LinearLayout {

	private ShadowLayoutProvider mProvider = new ShadowLayoutProvider(this){

		@Override
		protected int getShadowOffsetXStyleable() {
			return R.styleable.LinearShadowLayout_LinearShadowLayout_shadowOffsetX;
		}

		@Override
		protected int getShadowOffsetYStyleable() {
			return R.styleable.LinearShadowLayout_LinearShadowLayout_shadowOffsetY;
		}
		
	};
	
	public LinearShadowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mProvider.init(context, attrs);
	}

	public LinearShadowLayout(Context context) {
		super(context);
	}
	
	/********************************************************
	 * override
	 */
	
	/**
	 * 绘制前
	 */
	@Override
	public void invalidate() {
		//设置阴影参数
		mProvider.setShadowLayer();
		super.invalidate();
	}
	
	/**
	 * 绘制
	 */
	@Override
    public void draw(Canvas canvas){
		//绘制阴影
		mProvider.drawShadow(canvas);
        super.draw(canvas);
     }
	
}
