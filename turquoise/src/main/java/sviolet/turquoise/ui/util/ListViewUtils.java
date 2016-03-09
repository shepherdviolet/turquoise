package sviolet.turquoise.ui.util;

import android.widget.AbsListView;

/**
 * <p>ListView工具</p>
 *
 * Created by S.Violet on 2016/3/9.
 */
public class ListViewUtils {

    /**
     * 判断列表是否拉到最顶端
     * @param listView 列表控件
     */
    public static boolean reachTop(AbsListView listView){
        //无子元素视为到达顶端
        if (listView.getChildCount() <= 0){
            return true;
        }
        //显示的第一个Item为第一个, 且该Item到达顶端的距离为0
        if(listView.getFirstVisiblePosition() == 0 && listView.getChildAt(0).getTop() == 0){
            return true;
        }
        return false;
    }

    /**
     * 判断列表是否拉到最底端
     * @param listView 列表控件
     */
    public static boolean reachBottom(AbsListView listView){
        //无子元素视为到达底端
        if (listView.getChildCount() <= 0){
            return true;
        }
        //显示的最后一个Item为最后一个, 且该Item到达底部的距离为0
        if(listView.getLastVisiblePosition() == (listView.getCount() - 1) &&
                listView.getChildAt(listView.getChildCount() - 1).getBottom() == listView.getHeight()){
            return true;
        }
        return false;
    }

}
