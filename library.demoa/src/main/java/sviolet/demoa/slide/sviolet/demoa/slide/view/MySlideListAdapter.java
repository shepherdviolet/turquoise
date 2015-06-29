package sviolet.demoa.slide.sviolet.demoa.slide.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoa.R;
import sviolet.turquoise.view.slide.view.LinearLayoutDrawer;
import sviolet.turquoise.view.slide.view.SlideListAdapter;

/**
 * 滑动列表适配器
 * <p/>
 * Created by S.Violet on 2015/6/23.
 */
public class MySlideListAdapter extends BaseAdapter implements SlideListAdapter {

    private Context context;
    private int quantity;//列表项数量
    private String title;//标题
    private String type;//类型
    private String info;//信息
    private int titleColor;//标题字体颜色

    private List<LinearLayoutDrawer> drawerList = new ArrayList<LinearLayoutDrawer>();

    /**
     * @param quantity 列表项数量
     * @param title    标题
     * @param type     类型
     * @param info     说明
     */
    public MySlideListAdapter(Context context, int quantity, String title, String type, String info) {
        this(context, quantity, title, type, info, 0xFF303030);
    }

    /**
     * @param quantity   列表项数量
     * @param title      标题
     * @param type       类型
     * @param info       说明
     * @param titleColor 标题颜色
     */
    public MySlideListAdapter(Context context, int quantity, String title, String type, String info, int titleColor) {
        this.context = context;
        this.quantity = quantity;
        this.title = title;
        this.type = type;
        this.info = info;
        this.titleColor = titleColor;
    }

    @Override
    public int getCount() {
        return quantity;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.slide_list_item, null);
            holder = new ViewHolder();
            holder.drawer = (LinearLayoutDrawer) convertView.findViewById(R.id.slide_list_drawer);
            holder.title = (TextView) convertView.findViewById(R.id.common_list_item_title);
            holder.type = (TextView) convertView.findViewById(R.id.common_list_item_type);
            holder.info = (TextView) convertView.findViewById(R.id.common_list_item_info);
            holder.backgroundButtom = (Button) convertView.findViewById(R.id.slide_list_background_button);
            convertView.setTag(holder);
            //初始化抽屉
            drawerList.add(holder.drawer);
            holder.drawer.setSlideScrollDirection(LinearLayoutDrawer.DIRECTION_LEFT)
                    .setSlideDrawerWidth(200)
                    .setSlideScrollDuration(700)
                    .setSlideInitStage(LinearLayoutDrawer.STAGE_PULL_OUT)
                    .applySlideSetting();
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "title click", Toast.LENGTH_SHORT).show();
                }
            });
            holder.backgroundButtom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "button click", Toast.LENGTH_SHORT).show();
                    holder.drawer.pullOut();
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
            //重置抽屉
            holder.drawer.pullOutImmidiatly();//拉出抽屉
        }

        inflateView(position, convertView, holder);
        return convertView;
    }

    /**********************************************
     * private
     */

    /**
     * 渲染View
     *
     * @param position 位置
     * @param view     view
     * @param holder   holder
     */
    private void inflateView(int position, View view, ViewHolder holder) {
        String tail = Integer.toString(position);
        if (title != null)
            holder.title.setText(title + tail);
        else
            holder.title.setText("");

        holder.title.setTextColor(titleColor);

        if (type != null)
            holder.type.setText(type + tail);
        else
            holder.type.setText("");

        if (info != null)
            holder.info.setText(info + tail);
        else
            holder.info.setText("");
    }

    private class ViewHolder {
        LinearLayoutDrawer drawer;
        TextView title;
        TextView type;
        TextView info;
        Button backgroundButtom;
    }

    @Override
    public boolean hasSliddenItem() {
        for (LinearLayoutDrawer linearLayoutDrawer : drawerList) {
            if (linearLayoutDrawer.getCurrentStage() != linearLayoutDrawer.getPullOutStage())
                return true;
        }
        return false;
    }

    @Override
    public void resetSliddenItem() {
        for (LinearLayoutDrawer linearLayoutDrawer : drawerList) {
            linearLayoutDrawer.pullOut();
        }
    }
}
