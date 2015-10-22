package sviolet.turquoise.view.slide.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import sviolet.turquoise.view.slide.logic.LinearGestureDriver;

/**
 * <pre>
 * 内容可左右滑动的ListView
 * 
 * 利用LinearGestureDriver判断是垂直方向拖动还是水平方向拖动
 * </pre>
 * Created by S.Violet on 2015/6/17.
 */
public class SlideListView extends ListView {

    private LinearGestureDriver mLinearGestureDriver = new LinearGestureDriver(getContext());
    private boolean isXMoving = false;//X轴方向拖动中
    private boolean abortTouchEvent = false;//阻断OnTouchEvent处理事件
    private int lastX;//上一次X轴坐标位置

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
        //重置标志位
        if (ev.getAction() != MotionEvent.ACTION_MOVE) {
            isXMoving = false;
            abortTouchEvent = false;
        }

        boolean originResult = super.onInterceptTouchEvent(ev);
        mLinearGestureDriver.onInterceptTouchEvent(ev);

        //[功能]:上下滑动时复位被滑动过的单元项]
        if (getAdapter() instanceof SlideListAdapter && !isXMoving && ((SlideListAdapter)getAdapter()).hasSliddenItem()){
            //当Adapter实现了SlideListAdapter方法, 且X轴尚未发生有效滑动, 且存在被滑动过的子View
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                //按下时重置所有子View到未滑动状态
                ((SlideListAdapter) getAdapter()).resetSliddenItem();
                lastX = (int) ev.getX();
                return false;
            }else if (ev.getAction() == MotionEvent.ACTION_MOVE){
                //手势拖动时判断方向
                int _lastX = lastX;
                lastX = (int) ev.getX();
                //向左滑动时拦截事件, 并阻断OnTouchEvent事件处理
                if (ev.getX() < _lastX) {
                    abortTouchEvent = true;
                    return true;
                }
            }
            return false;
        } else if (mLinearGestureDriver.getState() == LinearGestureDriver.STATE_MOVING_X){
            //X轴方向有效拖动时, 阻止ListView拦截事件
            isXMoving = true;
            return false;
        }
        return originResult;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //若阻断OnTouchEvent, 则直接返回true不执行父类方法
        if (abortTouchEvent) {
            return true;
        }
        return super.onTouchEvent(ev);
    }

}