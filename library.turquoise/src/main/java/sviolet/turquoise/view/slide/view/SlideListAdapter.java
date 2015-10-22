package sviolet.turquoise.view.slide.view;

/**
 * <pre>
 * 可左右滑动的ListView配套适配器(接口)
 * </pre>
 * Created by S.Violet on 2015/6/25.
 */
public interface SlideListAdapter {

    /**
     * <pre>
     * [功能]:上下滑动时复位被滑动过的单元项]
     * 返回false不启用功能
     * [实现提示]:
     * 判断所有的子View是否被滑动, 若有一个被滑动过则返回true, 否则返回false
     * </pre>
     */
    public abstract boolean hasSliddenItem();

    /**
     * <pre>
     * [功能]:上下滑动时复位被滑动过的单元项]
     * [实现提示]:
     * 将所有的子View重置为未滑动过的状态
     * </pre>
     */
    public abstract void resetSliddenItem();

}
