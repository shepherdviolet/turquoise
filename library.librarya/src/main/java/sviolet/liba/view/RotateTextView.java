package sviolet.liba.view;

import sviolet.liba.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

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
