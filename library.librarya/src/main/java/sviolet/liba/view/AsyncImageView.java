package sviolet.liba.view;

import sviolet.liba.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

/**
 * 异步加载图片控件(务必destroy()销毁)<br>
 * 用法1.新建一个类继承此类,onLoad()中复写图片加载代码, load()方法开始加载
 * 用法2.通过setNoImage()/setLoadingImage()/setImage(Bitmap)改变状态
 * 
 * @author S.Violet
 *
 */

/*例:
    <sviolet.lib.android.view.CacheImageView
        android:id="@+id/image"
        android:layout_width="match_parent"
        android:layout_height="100dp" 
        sviolet:src="@drawable/x"
        sviolet:color="#209090"/>
 */

/*加载:
		image = (AsyncImageView) findViewById(R.id.image);
		image.load(null);
 */

public abstract class AsyncImageView extends RecycleImageView {

	private static final long INVALIDATE_INTERVAL = 500;
	private static final float PROGRESSBAR_X = 0.5f;//进度条水平位置
	private static final float PROGRESSBAR_Y = 0.6f;//进度条垂直位置
	private static final float PROGRESSBAR_R = 0.03f;//进度条圆点半径
	private static final float PROGRESSBAR_INTERVAL = 0.04f;//进度条圆点间隔
	
	private Thread loadThread;
	private Bitmap bitmap;
	private Paint paint;
	
	private int src;
	
	private boolean isDestory = false;
	private int loadCount = 0;

	public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public AsyncImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AsyncImageView(Context context) {
		super(context);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawProgressbar(canvas);
	}
	
	/**
	 * 实现图片加载的过程
	 * @param params load方法传入的参数
	 * @return 
	 */
	public abstract Bitmap onLoad(String[] params);
	
	/**********************************************************
	 * init
	 */
	
	/**
	 * 初始化<br>
	 * 尚未完成: 配置进度条属性
	 * 
	 * @param context
	 * @param attrs
	 */
	private void init(Context context, AttributeSet attrs){
		TypedArray types = context.obtainStyledAttributes(attrs, R.styleable.AsyncImageView);
		src = types.getResourceId(R.styleable.AsyncImageView_src, 0);
		int color = types.getColor(R.styleable.AsyncImageView_color, 0x808080);
		types.recycle();
		
		paint = new Paint();
		paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
		
		setNoImage();
	}
	
	/***********************************************************
	 * inter
	 */
	
	private static final int HANDLER_NO_IMAGE = 0;//无图或失败
	private static final int HANDLER_LOADING_IMAGE = 1;//加载中
	private static final int HANDLER_LOADED_IMAGE = 2;//加载成功
	private static final int HANDLER_INVALIDATE = 3;//刷新显示(进度条绘图)
	
	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			synchronized (AsyncImageView.this) {
				super.handleMessage(msg);
				switch(msg.what){
				case HANDLER_NO_IMAGE:
					loadCount = -1;
					if(src != 0){
						setImageResource(src);
					}else{
						invalidate();
					}
					break;
				case HANDLER_LOADING_IMAGE:
					loadCount = 0;
					invalidate();
					break;
				case HANDLER_LOADED_IMAGE:
					loadCount = -1;
					if(bitmap != null && !bitmap.isRecycled()){
						setImageBitmap(bitmap);
					}else{
						invalidate();
					}
					break;
				case HANDLER_INVALIDATE:
					invalidate();
				default:
					break;
				}
			}
		}
	};
	
	/******************************************************************
	 * 						M
	 */
	
	/**
	 * 绘制进度条
	 * 
	 * @param canvas
	 */
	private void drawProgressbar(Canvas canvas){
		if(loadCount >= 0){
			drawProgressbar(canvas, loadCount);
			loadCount++;
			if(loadCount > getMaxLoadCount())
				loadCount = 0;
			handler.sendEmptyMessageDelayed(HANDLER_INVALIDATE, INVALIDATE_INTERVAL);
		}
	}

	/**
	 * 进度条最大值(可重写)
	 * @return
	 */
	public int getMaxLoadCount(){
		return 3;
	}
	
	/**
	 * 绘制进度条(可重写)
	 * 
	 * @param canvas
	 * @param num
	 */
	public void drawProgressbar(Canvas canvas, int loadCount) {
		int x = (int) (getWidth() * (PROGRESSBAR_X - PROGRESSBAR_INTERVAL));
		int y = (int) (getHeight() * PROGRESSBAR_Y);
		int r = (int) (getHeight() * PROGRESSBAR_R);
		
		for(int i = 1 ; i <= 3 ; i++){
			if(i == loadCount){
				paint.setAlpha(255);
			}else{
				paint.setAlpha(64);
			}
			canvas.drawCircle(x, y, r, paint);
			x += getWidth() * PROGRESSBAR_INTERVAL;
		}
	}
	
	/*****************************************************************
	 * 						FUNC
	 */
	
	/**
	 * 开始加载图片
	 * @param params
	 */
	public void load(final String[] params){
		loadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				setLoadingImage();
				bitmap = onLoad(params);
				if(!isDestory)
					setLoadedImage();
			}
		});
		loadThread.start();
	}
	
	/**
	 * 显示无图状态
	 */
	public void setNoImage(){
		handler.sendEmptyMessage(HANDLER_NO_IMAGE);
	}
	
	/**
	 * 显示加载中状态
	 */
	public void setLoadingImage() {
		handler.sendEmptyMessage(HANDLER_LOADING_IMAGE);
	}
	
	/**
	 * 显示由onLoad()加载完成的图片
	 */
	public void setLoadedImage() {
		handler.sendEmptyMessage(HANDLER_LOADED_IMAGE);
	}
	
	/**
	 * 显示指定图片
	 */
	public void setImage(Bitmap bitmap) {
		this.bitmap = bitmap;
		handler.sendEmptyMessage(HANDLER_LOADED_IMAGE);
	}
	
	/**
	 * 销毁控件, 回收Bitmap资源
	 */
	public void destory(){
		isDestory = true;
		releaseBitmap(bitmap);
		super.destroy();
	}
}
