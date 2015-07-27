package sviolet.liba.view.locus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import sviolet.turquoise.utils.BitmapUtils;

/**
 * 轨迹密码控件(务必调用destroy()销毁)<p>
 * 
 * 如需加密请复写encrypt()方法,默认返回原值
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

@SuppressLint("ClickableViewAccessibility")
public class LocusView extends View {

	// 清除时延
	public static final long CLEAR_TIME = 800;
	// 圈和线的大小(默认1.0F)
	public static final float ZOOM = 1.0F;
	// 点的触摸灵敏度(1.2F表示手指触点在圈显示范围1.2倍的范围内判定为经过该点)
	public static final float POINT_SENSITIVITY = 1.2F;

	// 点(Point)状态
	public static int STATE_NORMAL = 0; // 未选中
	public static int STATE_CHECK = 1; // 选中 e
	public static int STATE_CHECK_ERROR = 2; // 已经选中,但是输错误

	//宽高
	private float w = 0;
	private float h = 0;
	//圆的半径
	private float r = 0;
	// 圈的缩放大小
	private float zoom = ZOOM;
	// 圈触摸灵敏度(1.2F表示手指触点在圈显示范围1.2倍的范围内判定为经过该点)
	private float pointSensitivity = POINT_SENSITIVITY;
	//连线透明度
	private int lineAlpha = 100;
	//是否已加载图片
	private boolean isCache = false;

	//点阵
	private Point[][] mPoints = new Point[3][3];
	//选中的点
	private List<Point> sPoints = new ArrayList<Point>();
	//密码缓存
	private String cache;
	//画笔
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	//
	private Matrix mMatrix = new Matrix();

	private boolean checking = false;
	private boolean isTouchEnable = true; // 是否可操作
	
	private Bitmap locus_round_original;
	private Bitmap locus_round_click;
	private Bitmap locus_round_click_error;
	private Bitmap locus_line;
	private Bitmap locus_line_semicircle;
	private Bitmap locus_line_semicircle_error;
	private Bitmap locus_arrow;
	private Bitmap locus_line_error;

	public LocusView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public LocusView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LocusView(Context context) {
		super(context);
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (!isCache) {
			initCache();
		}
		drawToCanvas(canvas);
	}

	private void drawToCanvas(Canvas canvas) {

		// mPaint.setColor(Color.RED);
		// Point p1 = mPoints[1][1];
		// Rect r1 = new Rect(p1.x - r,p1.y - r,p1.x +
		// locus_round_click.getWidth() - r,p1.y+locus_round_click.getHeight()-
		// r);
		// canvas.drawRect(r1, mPaint);
		// 画所有点
		for (int i = 0; i < mPoints.length; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j];
				if (p.state == STATE_CHECK) {
					canvas.drawBitmap(locus_round_click, p.x - r, p.y - r,
							mPaint);
				} else if (p.state == STATE_CHECK_ERROR) {
					canvas.drawBitmap(locus_round_click_error, p.x - r,
							p.y - r, mPaint);
				} else {
					canvas.drawBitmap(locus_round_original, p.x - r, p.y - r,
							mPaint);
				}
			}
		}
		// mPaint.setColor(Color.BLUE);
		// canvas.drawLine(r1.left+r1.width()/2, r1.top, r1.left+r1.width()/2,
		// r1.bottom, mPaint);
		// canvas.drawLine(r1.left, r1.top+r1.height()/2, r1.right,
		// r1.bottom-r1.height()/2, mPaint);

		// 画连线
		if (sPoints.size() > 0) {
			int tmpAlpha = mPaint.getAlpha();
			mPaint.setAlpha(lineAlpha);
			Point tp = sPoints.get(0);
			for (int i = 1; i < sPoints.size(); i++) {
				Point p = sPoints.get(i);
				drawLine(canvas, tp, p);
				tp = p;
			}
			if (this.movingNoPoint) {
				drawLine(canvas, tp, new Point((int) moveingX, (int) moveingY));
			}
			mPaint.setAlpha(tmpAlpha);
			lineAlpha = mPaint.getAlpha();
		}

	}

	/**
	 * 初始化Cache信息
	 *
	 */
	private void initCache() {

		w = this.getWidth();
		h = this.getHeight();
		float x = 0;
		float y = 0;

		// 以最小的为准
		// 纵屏
		if (w > h) {
			x = (w - h) / 2;
			w = h;
		}
		// 横屏
		else {
			y = (h - w) / 2;
			h = w;
		}

		//特制,从assets读取
		try {
			locus_round_original = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_round_original.png"));
			locus_round_click = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_round_click.png"));
			locus_round_click_error = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_round_click_error.png"));
			locus_line = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_line.png"));
			locus_line_semicircle = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_line_semicircle.png"));
			locus_line_error = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_line_error.png"));
			locus_line_semicircle_error = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_line_semicircle_error.png"));
			locus_arrow = BitmapFactory.decodeStream(getContext().getAssets().open("locus/locus_arrow.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 计算圆圈图片的大小
		float canvasMinW = w;
		if (w > h) {
			canvasMinW = h;
		}
		float roundMinW = canvasMinW / 8.0f * 2;
		float roundW = roundMinW / 2.f;
		//
		float deviation = canvasMinW % (8 * 2) / 2;
		x += deviation;
		x += deviation;

		//无论大小都进行缩放
//		if (locus_round_original.getWidth() > roundMinW) {
			float sf = roundMinW * zoom / locus_round_original.getWidth(); // 取得缩放比例，将所有的图片进行缩放
			
			locus_round_original = BitmapUtils.zoom(locus_round_original, sf, true);
			locus_round_click = BitmapUtils.zoom(locus_round_click, sf, true);
			locus_round_click_error = BitmapUtils.zoom(locus_round_click_error,sf, true);

			locus_line = BitmapUtils.zoom(locus_line, sf, true);
			locus_line_semicircle = BitmapUtils.zoom(locus_line_semicircle, sf, true);

			locus_line_error = BitmapUtils.zoom(locus_line_error, sf, true);
			locus_line_semicircle_error = BitmapUtils.zoom(locus_line_semicircle_error, sf, true);
			locus_arrow = BitmapUtils.zoom(locus_arrow, sf, true);
			
			roundW = locus_round_original.getWidth() / 2;
//		}

		mPoints[0][0] = new Point(x + 0 + roundW, y + 0 + roundW);
		mPoints[0][1] = new Point(x + w / 2, y + 0 + roundW);
		mPoints[0][2] = new Point(x + w - roundW, y + 0 + roundW);
		mPoints[1][0] = new Point(x + 0 + roundW, y + h / 2);
		mPoints[1][1] = new Point(x + w / 2, y + h / 2);
		mPoints[1][2] = new Point(x + w - roundW, y + h / 2);
		mPoints[2][0] = new Point(x + 0 + roundW, y + h - roundW);
		mPoints[2][1] = new Point(x + w / 2, y + h - roundW);
		mPoints[2][2] = new Point(x + w - roundW, y + h - roundW);
		int k = 0;
		for (Point[] ps : mPoints) {
			for (Point p : ps) {
				p.index = k;
				k++;
			}
		}
		r = locus_round_original.getHeight() / 2;// roundW;
		isCache = true;
	}

	/**
	 * 画两点的连接
	 * 
	 * @param canvas
	 * @param a
	 * @param b
	 */
	private void drawLine(Canvas canvas, Point a, Point b) {
		float ah = (float) LocusUtil.distance(a.x, a.y, b.x, b.y);
		float degrees = getDegrees(a, b);
		canvas.rotate(degrees, a.x, a.y);

//		if (a.state == STATE_CHECK_ERROR) {
//			mMatrix.setScale((ah - locus_line_semicircle_error.getWidth())
//					/ locus_line_error.getWidth(), 1);
//			mMatrix.postTranslate(a.x, a.y - locus_line_error.getHeight() / 2.0f);
//			canvas.drawBitmap(locus_line_error, mMatrix, mPaint);
//			canvas.drawBitmap(locus_line_semicircle_error, a.x
//					+ locus_line_error.getWidth(),
//					a.y - locus_line_error.getHeight() / 2.0f, mPaint);
//		} else {
		mMatrix.setScale(
				(ah - locus_line_semicircle.getWidth()) / locus_line.getWidth(),
				1);
		mMatrix.postTranslate(a.x, a.y - locus_line.getHeight() / 2.0f);
		canvas.drawBitmap(locus_line, mMatrix, mPaint);
		canvas.drawBitmap(locus_line_semicircle, a.x + ah
				- locus_line_semicircle.getWidth(),
				a.y - locus_line.getHeight() / 2.0f, mPaint);
//		}

		canvas.drawBitmap(locus_arrow, a.x, a.y - locus_arrow.getHeight()
				/ 2.0f, mPaint);

		canvas.rotate(-degrees, a.x, a.y);

	}

	private float getDegrees(Point a, Point b) {
		float ax = a.x;// a.index % 3;
		float ay = a.y;// a.index / 3;
		float bx = b.x;// b.index % 3;
		float by = b.y;// b.index / 3;
		float degrees = 0;
		if (bx == ax) // y轴相等 90度或270
		{
			if (by > ay) // 在y轴的下边 90
			{
				degrees = 90;
			} else if (by < ay) // 在y轴的上边 270
			{
				degrees = 270;
			}
		} else if (by == ay) // y轴相等 0度或180
		{
			if (bx > ax) // 在y轴的下边 90
			{
				degrees = 0;
			} else if (bx < ax) // 在y轴的上边 270
			{
				degrees = 180;
			}
		} else {
			if (bx > ax) // 在y轴的右边 270~90
			{
				if (by > ay) // 在y轴的下边 0 - 90
				{
					degrees = 0;
					degrees = degrees
							+ switchDegrees(Math.abs(by - ay),
									Math.abs(bx - ax));
				} else if (by < ay) // 在y轴的上边 270~0
				{
					degrees = 360;
					degrees = degrees
							- switchDegrees(Math.abs(by - ay),
									Math.abs(bx - ax));
				}

			} else if (bx < ax) // 在y轴的左边 90~270
			{
				if (by > ay) // 在y轴的下边 180 ~ 270
				{
					degrees = 90;
					degrees = degrees
							+ switchDegrees(Math.abs(bx - ax),
									Math.abs(by - ay));
				} else if (by < ay) // 在y轴的上边 90 ~ 180
				{
					degrees = 270;
					degrees = degrees
							- switchDegrees(Math.abs(bx - ax),
									Math.abs(by - ay));
				}

			}

		}
		return degrees;
	}

	/**
	 * 1=30度 2=45度 4=60度
	 *
	 * @return
	 */
	private float switchDegrees(float x, float y) {
		return (float) LocusUtil.pointTotoDegrees(x, y);
	}

//	/**
//	 * 取得数组下标
//	 * 
//	 * @param index
//	 * @return
//	 */
//	private int[] getArrayIndex(int index) {
//		int[] ai = new int[2];
//		ai[0] = index / 3;
//		ai[1] = index % 3;
//		return ai;
//	}

	/**
	 * 
	 * 检查
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Point checkSelectPoint(float x, float y) {
		for (int i = 0; i < mPoints.length; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j];
				if (LocusUtil.checkInRound(p.x, p.y, r * pointSensitivity, (int) x, (int) y)) {
					return p;
				}
			}
		}
		return null;
	}

	/**
	 * 判断点是否有交叉 返回 0,新点 ,1 与上一点重叠 2,与非最后一点重叠
	 * 
	 * @param p
	 * @return
	 */
	private int crossPoint(Point p) {
		// 重叠的不最后一个则 reset
		if (sPoints.contains(p)) {
			if (sPoints.size() > 2) {
				// 与非最后一点重叠
				if (sPoints.get(sPoints.size() - 1).index != p.index) {
					return 2;
				}
			}
			return 1; // 与最后一点重叠
		} else {
			return 0; // 新点
		}
	}

	/**
	 * 添加一个点
	 * 
	 * @param point
	 */
	private void addPoint(Point point) {
		this.sPoints.add(point);
	}

	boolean movingNoPoint = false;
	float moveingX, moveingY;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 不可操作
		if (!isTouchEnable) {
			return false;
		}

		movingNoPoint = false;
		float ex = event.getX();
		float ey = event.getY();
		boolean isFinish = false;
		boolean redraw = false;
		Point p = null;
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // 点下
			// 如果正在清除密码,则取消
			if (task != null) {
				task.cancel();
				task = null;
			}
			// 删除之前的点
			resetPoints();
			enableTouch();
			p = checkSelectPoint(ex, ey);
			if (p != null) {
				checking = true;
			}
			break;
		case MotionEvent.ACTION_MOVE: // 移动
			if (checking) {
				p = checkSelectPoint(ex, ey);
				if (p == null) {
					movingNoPoint = true;
					moveingX = ex;
					moveingY = ey;
				}
			}
			break;
		case MotionEvent.ACTION_UP: // 提起
			p = checkSelectPoint(ex, ey);
			checking = false;
			isFinish = true;
			break;
		}
		
		if (!isFinish && checking && p != null) {
			int rk = crossPoint(p);
			//与非最后一重叠
			if (rk == 2){
				// reset();
				// checking = false;
				movingNoPoint = true;
				moveingX = ex;
				moveingY = ey;
				redraw = true;
			//一个新点
			} else if (rk == 0){
				p.state = STATE_CHECK;
				addPoint(p);
				redraw = true;
			}
			//rk == 1 不处理
		}

		// 是否重画
		if (redraw) {

		}
		
		//是否完成
		if (isFinish) {
			if (sPoints.size() <= 1) {
				resetPoints();
				enableTouch();
			} else if (mCompleteListener != null) {
				onTouchComplete();
			}
		}
		
		this.postInvalidate();
		return true;
	}

	/**
	 * 
	 * 内部类: 点
	 * 
	 * @author S.Violet (ZhuQinChao)
	 *
	 */
	public class Point {

		public float x;
		public float y;
		public int state = 0;
		public int index = 0;// 下标

		public Point() {

		}

		public Point(float x, float y) {
			this.x = x;
			this.y = y;
		}

	};
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 重置点状态
	 */
	public void resetPoints() {
		setPointsNormal();
		if(sPoints != null)
			sPoints.clear();
	}

	/**
	 * 设置点状态为正常
	 */
	public void setPointsNormal() {
		if(sPoints != null)
			for (Point p : sPoints) {
				p.state = STATE_NORMAL;
			}
	}

	/**
	 * 设置点状态为错误
	 */
	public void setPointsError() {
		if(sPoints != null)
			for (Point p : sPoints) {
				p.state = STATE_CHECK_ERROR;
			}
	}

	/**
	 * 设置为可操作
	 */
	public void enableTouch() {
		isTouchEnable = true;
	}

	/**
	 * 设置为不可操作
	 */
	public void disableTouch() {
		isTouchEnable = false;
	}

	private Timer timer = new Timer();
	private TimerTask task = null;
	
	/**
	 * 清除密码(重置点/允许触摸)
	 * 
	 * 默认延时
	 */
	public void clearPassword() {
		clearPassword(CLEAR_TIME);
	}

	/**
	 * 清除密码(重置点/允许触摸)
	 */
	public void clearPassword(final long time) {
		if (time > 1) {
			
			if (task != null)
				task.cancel();
			
			lineAlpha = 130;
			postInvalidate();
			
			if(timer !=null){
				task = new TimerTask() {
					public void run() {
						resetPoints();
						enableTouch();
						postInvalidate();
					}
				};
				timer.schedule(task, time);
			}
			
		} else {
			resetPoints();
			enableTouch();
			postInvalidate();
		}
	}

	/**
	 * 销毁
	 */
	public void destroy() {
		
		if(task != null){
			task.cancel();
			task = null;
		}
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		
		clearPassword(0);
		cache = null;
		mPoints = null;
		
		recycleBitmap(locus_round_original);
		recycleBitmap(locus_round_click);
		recycleBitmap(locus_round_click_error);
		recycleBitmap(locus_line);
		recycleBitmap(locus_line_semicircle);
		recycleBitmap(locus_line_semicircle_error);
		recycleBitmap(locus_arrow);
		recycleBitmap(locus_line_error);
	}
	
	public void recycleBitmap(Bitmap bitmap){
		if(bitmap != null && !bitmap.isRecycled()){
			bitmap.recycle();
			bitmap = null;
		}
	}
	
	/**
	 * 缓存密码
	 */
	public void cachePassword(){
		cache = getPassword();
	}
	
	/**
	 * 获得缓存的密码(明文)
	 * 
	 * @return
	 */
	public String getCachePassword(){
		if(cache != null)
			return cache;
		else
			return "";
	}
	
	/**
	 * 获得密码(密文)
	 * 
	 * 若密码缓存过,则返回缓存密码的密文
	 * 
	 * @param timeStamp 时间戳
	 * @return 密文
	 */
	public String getPassword(String timeStamp) {
		if(cache != null)
			return encrypt(timeStamp,cache);
		else
			return encrypt(timeStamp,getPassword());
	}
	
	/**
	 * 加密算法
	 * (如有加密需要请复写该方法,默认返回原数据)
	 * 
	 * @param timeStamp
	 * @param content
	 * @return
	 */
	public String encrypt(String timeStamp,String content){
		return content;
	}
	
	/**
	 * 获得密码(明文)
	 * 
	 * @return 明文
	 */
	public String getPassword() {
		if (sPoints.size() > 0) {
			StringBuffer sf = new StringBuffer();
			for (Point p : sPoints) {
				sf.append(p.index + 1);
			}
			return sf.toString();
		} else {
			return "";
		}
	}
	
	/**
	 * 设置圈的缩放大小(默认1.0F),
	 * 值越大,圈越大
	 * 
	 * @param zoom
	 */
	public void setZoom(float zoom){
		this.zoom = zoom;
	}
	
	/**
	 * 圈的触摸灵敏度(默认1.2F),
	 * 1.2F表示手指触点在圈大小1.2倍的范围内判定为经过该点
	 * 
	 * @param sensitivity
	 */
	public void setPointSensitivity(float sensitivity){
		this.pointSensitivity = sensitivity;
	}
	
	/**
	 * 当滑动完成后调用
	 */
	public void onTouchComplete(){
		disableTouch();
		mCompleteListener.onComplete(this.sPoints.size());
	}
	
	private OnCompleteListener mCompleteListener;

	public interface OnCompleteListener {
		public void onComplete(int length);
	}
	
	/**
	 * 设置监听器
	 *
	 */
	public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
		this.mCompleteListener = onCompleteListener;
	}
}
