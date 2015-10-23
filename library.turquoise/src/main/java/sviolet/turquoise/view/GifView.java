package sviolet.turquoise.view;

import java.io.InputStream;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import sviolet.turquoise.R;

/**
 * GIF动画显示控件<br/>
 * sviolet:src gif资源<br/>
 * sviolet:repeat 动画是否重复 默认true<br/>
 * sviolet:duration 动画默认持续时间 默认1000(若GIF内置时间,则为GIF内置时间)<br/>
 * 
 * @author S.Violet
 *
 */
public class GifView extends View {
	
	//GIF管理类
	private Movie mMovie;
	
	private boolean repeat = true;//是否重复
	private long duration = 1000;//动画持续时间
	
	//动画起始时间
	private long startTime;

	public GifView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr);
	}

	public GifView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public GifView(Context context) {
		super(context);
	}
	
	/*****************************************************************************
	 * 					init
	 */
	
	private void init(Context context, AttributeSet attrs, int defStyleAttr){
        TypedArray typedArray = context.obtainStyledAttributes(attrs,  R.styleable.GifView, defStyleAttr, 0);  
        int src = typedArray.getResourceId(R.styleable.GifView_src, 0);
        repeat = typedArray.getBoolean(R.styleable.GifView_repeat, true);
        duration = typedArray.getInteger(R.styleable.GifView_duration, 1000);
        typedArray.recycle();  
        
        mMovie = null;
        startTime = 0;  
        
        if(src > 0){
            InputStream is = context.getResources().openRawResource(src);  
            mMovie = Movie.decodeStream(is);  
    		//计算动画持续时间
    		int _duration = mMovie.duration();
    		if (_duration > 0)
    			duration = _duration;
        }  
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (mMovie == null)
			return;
		
		//计算时间
		if (startTime == 0) {
			startTime = android.os.SystemClock.uptimeMillis();
		}
		int passTime = (int) (android.os.SystemClock.uptimeMillis() - startTime);
		
		//绘制图形
		mMovie.setTime((int)(passTime % duration));
		mMovie.draw(canvas, 0, 0);
		
		if(repeat || passTime < duration){
			invalidate();
		}
	}
}
