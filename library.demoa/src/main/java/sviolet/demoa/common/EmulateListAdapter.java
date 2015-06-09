package sviolet.demoa.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import sviolet.demoa.R;

/**
 * 模拟List适配器
 * <p/>
 * Created by S.Violet on 2015/6/3.
 */
public class EmulateListAdapter extends BaseAdapter {

    private Context context;
    private int quantity;//列表项数量
    private String title;//标题
    private String type;//类型
    private String info;//信息
    private int titleColor;//标题字体颜色

    /**
     * @param quantity 列表项数量
     * @param title 标题
     * @param type 类型
     * @param info 说明
     */
    public EmulateListAdapter(Context context, int quantity, String title, String type, String info){
        this(context, quantity, title, type, info, 0xFF303030);
    }

    /**
     * @param quantity 列表项数量
     * @param title 标题
     * @param type 类型
     * @param info 说明
     * @param titleColor 标题颜色
     */
    public EmulateListAdapter(Context context, int quantity, String title, String type, String info, int titleColor) {
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
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.common_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.common_list_item_title);
            holder.type = (TextView) convertView.findViewById(R.id.common_list_item_type);
            holder.info = (TextView) convertView.findViewById(R.id.common_list_item_info);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
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
        TextView title;
        TextView type;
        TextView info;
    }

}
