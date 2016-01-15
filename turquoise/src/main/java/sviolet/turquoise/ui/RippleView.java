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

package sviolet.turquoise.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import sviolet.turquoise.R;
import sviolet.turquoise.enhance.common.WeakHandler;

/**
 * 水波纹触摸效果控件(RelativeLayout)
 *
 * 默认abordTouchEvent=false;<p/>
 *
 * action代表动画结束时, 之前的点击事件为无/单击/长按<p/>
 *
 * <pre>{@code
 *		RippleView ripple = (RippleView) findViewById(R.id.ripple);
 *		//波纹动画播放85%后触发
 *		ripple.setOnAnimationFinishListener(0.85f, new RippleView.OnAnimationFinishListener() {
 *			public void onAnimationFinished(int action) {
 *
 *			}
 *		});
 *
 *  <!-- 本身也会触发onClick事件 -->
 *  <sviolet.lib.android.view.RippleView
 *      android:layout_width="300dp"
 *      android:layout_height="50dp"
 *      android:background="#209090">
 *      <!-- 内部只能含一个View, RippleView的event会传递给该子View -->
 *      <TextView
 *          android:layout_width="wrap_content"
 *          android:layout_height="wrap_content"/>
 *  </sviolet.lib.android.view.RippleView>
 *
 *	}</pre>
 * 
 * @author S.Violet
 *
 */

public class RippleView extends RelativeLayout{
	
	public static int ACTION_NULL = 0;//无操作
	public static int ACTION_CLICK = 1;//单击
	public static int ACTION_LONGPRESS = 2;//长按
	
	private static int SLOW_DOWN = 4;//长按时减速倍率
	private static int TIMER_MIN = 1 * SLOW_DOWN;//影响波纹初始大小
	private static float NEAR_RANGE = 5F;
	
	private static int RIPPLE_TYPE_DEF = 0;
	private static int FRAME_RATE_DEF = 15;
	private static int DURATION_DEF = 150;
	private static int PAINT_ALPHA_DEF = 90;
	private static int RIPPLE_PADDING_DEF = 0;
	private static int ZOOM_DURATION_DEF= 200;
	private static float ZOOM_SCALE_DEF= 1.03f;
	private static int RIPPLE_COLOR_DEF = 0xFFFFFFFF;
	private static float CALLBACK_PERCENT_DEF = 1f;
	
    //配置
    private int rippleType = RIPPLE_TYPE_DEF;//波纹类型
    private int frameRate = FRAME_RATE_DEF;//波纹动画刷新间隔(帧间隔)
    private int duration = DURATION_DEF * SLOW_DOWN;//波纹动画持续时间(减速)
    private int paintAlpha = PAINT_ALPHA_DEF;//波纹透明度
    private int zoomDuration = ZOOM_DURATION_DEF;//触摸时变大动画持续时间
    private float zoomScale = ZOOM_SCALE_DEF;//触摸时变大比例
    private int rippleColor = RIPPLE_COLOR_DEF;//波纹颜色
    private int ripplePadding = RIPPLE_PADDING_DEF;//波纹内边距
    private boolean isZoom = false;//触摸时变大
    private boolean isCenter = false;//波纹中心居中
    private boolean longpressEnabled = true;//允许长按
    private boolean abordTouchEvent = false;//阻断触摸事件(不向下传递)
    
    private float callbackPercent = CALLBACK_PERCENT_DEF;//触发animation_finish事件的动画进度
	
	//长宽
    private int width;
    private int height;
    
    private float radiusMax = 0;
    private boolean animationRunning = false;//是否在执行动画
    private boolean isPressing = false;//是否按下状态
    private int timer = TIMER_MIN;
    private float x = -1;
    private float y = -1;
    
    private float ox = -1;//按下时的坐标
    private float oy = -1;
    private boolean hasMoved = false;//按下后是否有移动
    private boolean hasCallback = false;//是否触发过animation_finish事件
    
    private View childView;
    private ScaleAnimation scaleAnimation;
    
    private Paint paint;
    
    private OnAnimationFinishListener mOnAnimationFinishListener;//波纹动画结束监听器

    public RippleView(Context context){
        super(context);
    }

    public RippleView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context, attrs);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * XML中包含的子View(仅支持一个)
     */
    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params){
        childView = child;
        super.addView(child, index, params);
    }

    /**
     * 绘图
     */
    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        
		if (animationRunning)
			drawCircle(canvas);//绘制圆圈
    }

    /**
     * 触摸事件
     */
	@Override
	@SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			//记录按下时的坐标
			hasMoved = false;
			setOriginPosition(event);
			//按下触发波纹(减速波纹)
			if(!animationRunning){
				isPressing = true;
				triggerRipple(event);//触发波纹
			}
		}else if(event.getAction() == MotionEvent.ACTION_MOVE){
			if(calculateIfMoved(event)){
				hasMoved = true;//按下后有移动
				isPressing = false;//松开时加速波纹
			}
		}else if(event.getAction() == MotionEvent.ACTION_UP ){
			isPressing = false;//松开时加速波纹
		}
		
        if(childView != null &&	!abordTouchEvent)
        	childView.onTouchEvent(event);//向子控件分发触摸事件
        return true;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        scaleAnimation = new ScaleAnimation(1.0f, zoomScale, 1.0f, zoomScale, w / 2, h / 2);
        scaleAnimation.setDuration(zoomDuration);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatCount(1);
    }

    /**
     * 拦截触摸事件
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
       return true;
    }

    /************************************************************************
     * 							init
     */
    
    /**
     * 初始化
     * @param context
     * @param attrs
     */
	private void init(final Context context, final AttributeSet attrs){
    	//预览跳过
        if (isInEditMode())
            return;
        //取配置
        initSetting(context, attrs);
        //初始化画笔
        initPaint();
        //不调用onDraw
        setWillNotDraw(false);
        //允许绘图缓存
        setDrawingCacheEnabled(true);
    }

	/**
	 * 初始化配置
	 * 
	 * @param context
	 * @param attrs
	 */
	private void initSetting(final Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        rippleColor = typedArray.getColor(R.styleable.RippleView_colour, RIPPLE_COLOR_DEF);
        rippleType = typedArray.getInt(R.styleable.RippleView_RippleView_type, RIPPLE_TYPE_DEF);
        duration = typedArray.getInteger(R.styleable.RippleView_duration, DURATION_DEF) * SLOW_DOWN;//不松开时减速
        frameRate = typedArray.getInteger(R.styleable.RippleView_framerate, FRAME_RATE_DEF);
        paintAlpha = typedArray.getInteger(R.styleable.RippleView_alpha, PAINT_ALPHA_DEF);
        ripplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_padding, RIPPLE_PADDING_DEF);
        zoomScale = typedArray.getFloat(R.styleable.RippleView_zoomScale, ZOOM_SCALE_DEF);
        zoomDuration = typedArray.getInt(R.styleable.RippleView_zoomDuration, ZOOM_DURATION_DEF);
        isCenter = typedArray.getBoolean(R.styleable.RippleView_center, false);
        isZoom = typedArray.getBoolean(R.styleable.RippleView_zoomEnable, false);
        longpressEnabled = typedArray.getBoolean(R.styleable.RippleView_longpressEnabled, true);
        abordTouchEvent = typedArray.getBoolean(R.styleable.RippleView_abordTouchEvent, false);
        typedArray.recycle();
	}
    
	/**
	 * 初始化画笔
	 */
	private void initPaint() {
		paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(rippleColor);
        paint.setAlpha(paintAlpha);
	}
	
	/***********************************************************************************
	 * 										inter
	 */
	
	private final MyHandler handler = new MyHandler(Looper.getMainLooper(), this);

	private static class MyHandler extends WeakHandler<RippleView>{

		private static final int HANDLER_REFRESH_VIEW = 0;//刷新动画
		private static final int HANDLER_ANIMATION_FIHISH = 1;//动画结束回调

		public MyHandler(Looper looper, RippleView host) {
			super(looper, host);
		}

		@Override
		protected void handleMessageWithHost(Message msg, RippleView host) {
			switch(msg.what){
				case HANDLER_REFRESH_VIEW:
                    host.invalidate();
					break;
				case HANDLER_ANIMATION_FIHISH:
                    host.onAnimationFinish();
					break;
				default:
					break;
			}
		}
	}
		
    /**
     * 波纹动画结束监听器<p>
     * action:
     *
     */
    public interface OnAnimationFinishListener{
    	public void onAnimationFinished(int action);
    }
    
    /*************************************************************************************
     * 											M
     */
    
    /**
     * 当UI回调动画结束时
     */
	private void onAnimationFinish() {
		int action = ACTION_NULL;
		if(!hasMoved){
			if(isPressing && longpressEnabled){
				action = ACTION_LONGPRESS;
				performLongClick();
			}else if(!isPressing){
				action = ACTION_CLICK;
				performClick();
			}
		}
		if(mOnAnimationFinishListener != null)
			mOnAnimationFinishListener.onAnimationFinished(action);
	}

	/**
	 * 触发波纹事件
	 * @param event
	 */
	private void triggerRipple(MotionEvent event) {
		//是否需要触摸放大
		if (isZoom)
		    startAnimation(scaleAnimation);
		//波纹最大半径
		radiusMax = Math.max(width, height);
		//圆形半径计算
		if (rippleType == 1)
		    radiusMax /= 2;
		//除去padding值
		radiusMax -= ripplePadding;
		//计算波纹中心坐标
		if (isCenter){
			//设置居中的情况下, 居中
		    x = getMeasuredWidth() / 2;
		    y = getMeasuredHeight() / 2;
		}else{
			//波纹中心为触点坐标
		    x = event.getX();
		    y = event.getY();
		}
		//标志位
		animationRunning = true;
		hasCallback = false;
		//刷新显示(触发draw)
		invalidate();
		//触发点击事件
		performClick();
	}
	
	/**
	 * 绘制波纹
	 * @param canvas
	 */
	private void drawCircle(Canvas canvas) {
		//动画进度超过callbackPercent后, 触发animation_finish事件
		if(!hasCallback && timer * frameRate >= duration * callbackPercent){
			handler.sendEmptyMessage(MyHandler.HANDLER_ANIMATION_FIHISH);
			hasCallback = true;
		}
		
		//时间到动画结束
		if (timer * frameRate >= duration) {
			resetCanvas(canvas);
			return;
		} else {
			handler.sendEmptyMessageDelayed(MyHandler.HANDLER_REFRESH_VIEW, frameRate);
		}

		//画布保存初始状态
		if (timer <= TIMER_MIN)
			canvas.save();

		//绘制圆形
		canvas.drawCircle(x, y, calculateRadius(), paint);
		paint.setAlpha(calculateAlpha());

		if(isPressing && longpressEnabled)//按下时减速
			timer++;
		else//松开加速
			timer+=SLOW_DOWN;
	}

	/**
	 * 重置显示
	 * @param canvas
	 */
	private void resetCanvas(Canvas canvas) {
		animationRunning = false;
		timer = TIMER_MIN;
		canvas.restore();//画布恢复初始状态
		invalidate();
	}

	/**
	 * 计算圆形透明度
	 * @return
	 */
	private int calculateAlpha() {
		return (int) (paintAlpha * (1 - calculateProcess()));
	}

	/**
	 * 计算圆形半径
	 * @return
	 */
	private float calculateRadius() {
		return radiusMax * calculateProcess();
	}

	/**
	 * 计算动画进度
	 * @return
	 */
	private float calculateProcess() {
		return ((float) timer * frameRate) / duration;
	}
	
	/**
	 * 计算按下后是否有位移
	 * @param event
	 * @return
	 */
	private boolean calculateIfMoved(MotionEvent event){
		if (event.getX() < (ox - NEAR_RANGE) || event.getX() > (ox + NEAR_RANGE)) {
			return true;
		}
		if (event.getY() < (oy - NEAR_RANGE) || event.getY() > (oy + NEAR_RANGE)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 记录按下时的坐标
	 * @param event
	 */
	private void setOriginPosition(MotionEvent event) {
		ox = event.getX();
		oy = event.getY();
	}

	/*************************************************************8
	 * FUNC
	 */
	
	/**
	 * 设置动画结束后的回调监听(主线程)<br>
	 * 重置回调进度为默认(100%)
	 * 
	 * @param mOnAnimationFinishListener
	 */
	public void setOnAnimationFinishListener(OnAnimationFinishListener mOnAnimationFinishListener){
		this.callbackPercent = CALLBACK_PERCENT_DEF;
		this.mOnAnimationFinishListener = mOnAnimationFinishListener;
	}
	
	/**
	 * 设置动画结束后的回调监听(主线程)<br>
	 * 当波纹动画进度超过callbackPercent, 即触发animationFinish
	 * 
	 * @param callbackPercent 回调进度, 范围(0-1f]
	 * @param mOnAnimationFinishListener
	 */
	public void setOnAnimationFinishListener(float callbackPercent, OnAnimationFinishListener mOnAnimationFinishListener){
		if(callbackPercent > 0f && callbackPercent <= 1)
			this.callbackPercent = callbackPercent;
		this.mOnAnimationFinishListener = mOnAnimationFinishListener;
	}
	
}