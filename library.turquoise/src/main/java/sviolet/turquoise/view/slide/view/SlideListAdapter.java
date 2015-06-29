package sviolet.turquoise.view.slide.view;

/**
 * 可左右滑动的ListView配套适配器(接口)
 *
 * Created by S.Violet on 2015/6/25.
 */
public interface SlideListAdapter {

    /**
     * [功能]:上下滑动时复位被滑动过的单元项]<br/>
     * 返回false不启用功能<br/>
     * [实现提示]:<br/>
     * 判断所有的子View是否被滑动, 若有一个被滑动过则返回true, 否则返回false
     */
    public abstract boolean hasSliddenItem();

    /**
     * [功能]:上下滑动时复位被滑动过的单元项]<br/>
     * [实现提示]:<Br/>
     * 将所有的子View重置为未滑动过的状态
     */
    public abstract void resetSliddenItem();

}
