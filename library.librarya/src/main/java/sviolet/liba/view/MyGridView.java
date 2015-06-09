package sviolet.liba.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

/**
 * 重写了GridView的onMeasure方法，使其不会出现滚动条，ScrollView嵌套ListView也是同样的道理
 * @author SunPeng
 * @create 2014-6-26 上午11:25:40
 * 
 * 
 * 添加空白处触摸事件监听器
 * @author S.Violet (ZhuQinChao)
 */
public class MyGridView extends GridView {

	public MyGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyGridView(Context context) {
		super(context);
	}

	public MyGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
	
	/**
	 * 空白处触摸事件监听器
	 * 
	 * 处理点击事件,建议捕捉ACTION_UP.
	 * 
	 * @param listener
	 * @return boolean true:不向下传播事件 false:向下传播
	 * @author S.Violet (zhuqinchao)
	 */
	
	private OnBlankPositionTouchListener onBlankPositionTouchListener;
	
	public interface OnBlankPositionTouchListener {
		public boolean onTouch(MotionEvent event);
	}

	/**
	 * 设置空白处触摸事件监听器
	 * 
	 * @param onBlankPositionTouchListener2
	 * @author S.Violet (zhuqinchao)
	 */
	public void setOnBlankPositionTouchListener(OnBlankPositionTouchListener listener) {
		onBlankPositionTouchListener = listener;
	}

	/**
	 * 重写onTouchEvent(), 当触摸在空白处则回调onBlankPositionTouchListener.onTouch()
	 * 
	 * @author S.Violet (zhuqinchao)
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (onBlankPositionTouchListener != null) {
			if (!isEnabled()) {
				// A disabled view that is clickable still consumes the touch events, it just doesn't respond to them.
				return isClickable() || isLongClickable();
			}
			final int motionPosition = pointToPosition((int) event.getX(),(int) event.getY());
			if (motionPosition == INVALID_POSITION) {
				return onBlankPositionTouchListener.onTouch(event);
			}
		}
		return super.onTouchEvent(event);
	}

}