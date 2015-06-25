package sviolet.turquoise.view.slide.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import sviolet.turquoise.view.slide.logic.LinearGestureDriver;

/**
 * 内容可左右滑动的ListView<br/>
 * <br/>
 * 利用LinearGestureDriver判断是垂直方向拖动还是水平方向拖动
 *
 * Created by S.Violet on 2015/6/17.
 */
public class SlideListView extends ListView {

    private LinearGestureDriver mLinearGestureDriver = new LinearGestureDriver(getContext());

    public SlideListView(Context context) {
        super(context);
        init();
    }

    public SlideListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mLinearGestureDriver.setOrientation(LinearGestureDriver.ORIENTATION_ALL);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean originResult = super.onInterceptTouchEvent(ev);
        mLinearGestureDriver.onInterceptTouchEvent(ev);
        //X轴方向有效拖动时, 阻止ListView拦截事件
        return mLinearGestureDriver.getState() != LinearGestureDriver.STATE_MOVING_X && originResult;
    }

}