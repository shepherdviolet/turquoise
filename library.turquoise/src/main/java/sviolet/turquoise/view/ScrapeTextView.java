package sviolet.turquoise.view;

import sviolet.turquoise.utils.bitmap.BitmapUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import sviolet.turquoise.R;

/**
 * 刮刮乐TextView<<destroy()销毁>><p>
 * xmlns:sviolet="http://schemas.android.com/apk/res/应用包名
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

/*
 * 例:
	xmlns:sviolet="http://schemas.android.com/apk/res/包名"

	    <com.csii.yzbank.view.widget.ScrapeTextView 
        android:layout_width="300dp"
        android:layout_height="100dp"
        android:gravity="center"
        android:textSize="25sp"
        sviolet:height="100dp"
        sviolet:width="300dp"
        sviolet:color="#FF00FF"
        sviolet:strokeWidth="50"/>

    例2:
    	长宽必须设置,但无用,控件长宽会根据widthProportion/widthHeightRatio自动调整
    	
		<com.csii.yzbank.view.widget.ScrapeTextView 
		android:layout_width="wrap_content"
		android:layout_height="wrap_content"
		android:gravity="center"
		android:textSize="25sp"
		android:textColor="#ee5a00"
		sviolet:widthProportion="0.85"
		sviolet:widthHeightRatio="1.66"
		sviolet:color="#808080"
		sviolet:strokeWidth="30dp"
		sviolet:src="@drawable/lottery_0_ticket"/>
    
    方法:
   	reset(String):重置覆盖面/内容
	getPercent():获得刮开部分的比例
	destroy():销毁控件
 */

public class ScrapeTextView extends TextView {

	private boolean hasCallback = false;
	private double callbackPercent = 1f;//回调的刮开比例
	private Runnable onPercentListener;//刮开比例回调
	
	private int width, height;//控件宽高
	
	//例:widthProportion=0.5 屏幕宽度1080px, 则控件宽度540px, 设置widthHeightRatio=0.5, 则控件高度为1080px
	private float widthProportion, heightProportion;//控件宽高(占比模式)
	private float widthHeightRatio;//长宽比

	private Bitmap mBitmap;//当前覆盖面
	private Bitmap oBitmap;//初始覆盖面
	
	private Context mContext;
	private Paint mPaint;
	private Canvas tempCanvas;
	private float x, y;//当前点
	private float ox, oy;//前次点
	private Path mPath;
	
	private Handler calculateHandler;
	private MyThread mThread;
	private int messageCount;
	private int[] pixels;
	
	private int color;//覆盖面颜色
	private int strokeWidth;//画笔宽度
	private double scrapePercent;//刮开部分百分比
	private int resId;//覆盖面资源ID
	private boolean touchEnabled = true;

	/**
	 * 销毁, 回收资源
	 */
	public void destroy(){
		if(mBitmap != null && !mBitmap.isRecycled()){
			mBitmap.recycle();
		}
		if(oBitmap != null && !oBitmap.isRecycled()){
			oBitmap.recycle();
		}
		Bitmap background = getBackgroundBitmap();
		if(background != null && !background.isRecycled()){
			background.recycle();
		}
	}
	
	/**
	 * 取背景图
	 * @return
	 */
	private Bitmap getBackgroundBitmap(){
		Drawable _drawable = getBackground();
        if (_drawable != null && _drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) _drawable).getBitmap();
        }
        return null;
	}
	
	/**
	 * 获得刮开部分的百分比
	 * 
	 * @return
	 */
	public double getPercent(){
		return scrapePercent;
	}

	public ScrapeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init(attrs);
	}

	public ScrapeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		init(attrs);
	}

	/**
	 * 重置覆盖层
	 */
	public void reset(String text) {
		hasCallback = false;
		scrapePercent = 0;
		messageCount = 0;
		
		if(oBitmap == null){
			tempCanvas.drawColor(color);
		}else{
			if(oBitmap.isRecycled())
				initOriginalBitmap();
			if(mBitmap.isRecycled())
				initDrawCanvas();
			if(tempCanvas != null && oBitmap != null)
				tempCanvas.drawBitmap(oBitmap, 0, 0, null);
		}
		
		setText(text);
	}

	/**
	 * 初始化
	 * 
	 * @param attrs
	 */
	private void init(AttributeSet attrs) {
		// 获取控件大小值
		TypedArray types = mContext.obtainStyledAttributes(attrs, R.styleable.ScrapeTextView);
		width = (int) types.getDimension(R.styleable.ScrapeTextView_width, 0);
		height = (int) types.getDimension(R.styleable.ScrapeTextView_height, 0);
		widthProportion = types.getFloat(R.styleable.ScrapeTextView_widthProportion, -1);
		heightProportion = types.getFloat(R.styleable.ScrapeTextView_heightProportion, -1);
		widthHeightRatio = types.getFloat(R.styleable.ScrapeTextView_widthHeightRatio, -1);
		strokeWidth = (int)types.getDimension(R.styleable.ScrapeTextView_strokeWidth, 50);
		touchEnabled = types.getBoolean(R.styleable.ScrapeTextView_touchEnabled, true);
		color = types.getColor(R.styleable.ScrapeTextView_colour, 0xFFC0C0C0);
		String text = types.getString(R.styleable.ScrapeTextView_text);
		resId = types.getResourceId(R.styleable.ScrapeTextView_src, 0);
		types.recycle();

		// 初始化路径
		mPath = new Path();

		// 初始化画笔
		mPaint = new Paint();
		mPaint.setColor(color);
		mPaint.setAlpha(0);
		mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(strokeWidth);//画笔宽度
		
		initSize();
		initOriginalBitmap();
		initDrawCanvas();

		reset(text);

		// 在自线程中创建Handler接收像素消息
		mThread = new MyThread();
		mThread.start();
	}
	
	/**
	 * 根据widthProportion/heightProportion计算宽高, 并调整控件尺寸
	 */
	@SuppressWarnings("deprecation")
	private void initSize(){
		WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		int windowWidth = windowManager.getDefaultDisplay().getWidth();
		int windowHeight = windowManager.getDefaultDisplay().getHeight();
		if(widthProportion > 0 && windowWidth > 0){
			width = (int) (windowWidth * widthProportion);
			if(heightProportion < 0 && widthHeightRatio > 0)
				height = (int) (width / widthHeightRatio);
		}
		if(heightProportion > 0 && windowHeight > 0){
			height = (int) (windowHeight * heightProportion);
			if(widthProportion < 0 && widthHeightRatio > 0)
				width = (int) (height * widthHeightRatio);
		}
		setWidth(width);
		setHeight(height);
	}
	
	/**
	 * 初始化原图层
	 */
	private void initOriginalBitmap() {
		// 初始化初始覆盖面图形(0则为颜色模式)
		if(resId != 0 && width > 0 && height > 0){
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
			oBitmap = BitmapUtils.zoom(bitmap, width, height, true);
		}
	}
	
	private void initDrawCanvas(){
		// 初始化Bitmap并且锁定到临时画布上
		if(width > 0 && height > 0){
			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
			tempCanvas = new Canvas();
			tempCanvas.setBitmap(mBitmap);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 将处理过的bitmap画上去
		if(mBitmap == null || mBitmap.isRecycled())
			initDrawCanvas();
		if(canvas != null && mBitmap != null)
			canvas.drawBitmap(mBitmap, 0, 0, null);
	}

	@Override
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		if(!touchEnabled)
			return false;
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchDown(event);
			break;
		case MotionEvent.ACTION_MOVE:
			touchMove(event);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			break;
		}
		return true;
	}

	/**
	 *  移动的时候
	 * @param event
	 */
	private void touchMove(MotionEvent event) {
		x = event.getX();
		y = event.getY();
		mPath.quadTo((x + ox) / 2, (y + oy) / 2, x, y);//二次贝塞尔，实现平滑曲线；oX,oY为操作点 x,y为终点 
		tempCanvas.drawPath(mPath, mPaint);
		ox = x;
		oy = y;
		invalidate();//刷新
		calculateScale();
	}
	/**
	 * 按下来的时候
	 * 
	 * @param event
	 */
	private void touchDown(MotionEvent event) {
		ox = x = event.getX();
		oy = y = event.getY();
		mPath.reset();//路径重置
		mPath.moveTo(ox, oy);//路径起点
	}
	
	/**
	 * 计算揭开的百分比
	 */
	private void calculateScale() {
		Message msg = calculateHandler.obtainMessage(0);
		msg.obj = ++messageCount;
		calculateHandler.sendMessage(msg);
	}
	
	/**
	 * 计算解开部分占比,
	 * 异步线程，作用是创建handler接收处理消息。
	 * @author Administrator
	 *
	 */
	@SuppressLint("HandlerLeak")
	class MyThread extends Thread {

		public MyThread() {
		}

		@Override
		public void run() {
			super.run();
			//创建 handler前先初始化Looper.
			Looper.prepare();

			calculateHandler = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					// 只处理最后一次的百分比
					if ((Integer) (msg.obj) != messageCount) {
						return;
					}
					// 取出像素点
					synchronized (mBitmap) {
						if (pixels == null)
							pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
						mBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
					}

					int sum = pixels.length;
					int num = 0;
					for (int i = 0; i < sum; i++) {
						if (pixels[i] == 0) {
							num++;
						}
					}
					scrapePercent = num / (double) sum;
					
					if(!hasCallback && onPercentListener != null)
						if(scrapePercent >= callbackPercent)
							mHandler.sendEmptyMessage(HANDLER_PERCENT_CALLBACK);
				}
			};
			//启动该线程的消息队列
			Looper.loop();
		}
	}
	
	private static final int HANDLER_PERCENT_CALLBACK = 1;
	
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_PERCENT_CALLBACK:
				if(onPercentListener != null)
					onPercentListener.run();
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * 当刮开指定比例时回调监听器
	 * 
	 * @param callbackPercent 指定比例 0~1f
	 * @param onPercentListener
	 */
	public void setOnPercentListener(float callbackPercent, Runnable onPercentListener){
		this.callbackPercent = callbackPercent;
		this.onPercentListener = onPercentListener;
	}
	
}
