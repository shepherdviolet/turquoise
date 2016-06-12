package sviolet.demoa.other.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import sviolet.demoa.R;
import sviolet.turquoise.ui.util.TViewHolder;

/**
 * Created by S.Violet on 2016/3/9.
 */
public class GradualTitleListAdapter extends BaseAdapter {

    private static final int TYPE_TITLE_ITEM = 0;
    private static final int TYPE_CONTENT_ITEM = 1;

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
    public GradualTitleListAdapter(Context context, int quantity, String title, String type, String info){
        this(context, quantity, title, type, info, 0xFF303030);
    }

    /**
     * @param quantity 列表项数量
     * @param title 标题
     * @param type 类型
     * @param info 说明
     * @param titleColor 标题颜色
     */
    public GradualTitleListAdapter(Context context, int quantity, String title, String type, String info, int titleColor) {
        this.context = context;
        this.quantity = quantity;
        this.title = title;
        this.type = type;
        this.info = info;
        this.titleColor = titleColor;
    }

    @Override
    public int getCount() {
        return quantity + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 多类型适配复写方法<br/>
     * 区分类型并返回<br/>
     */
    @Override
    public int getItemViewType(int position) {
        if (position > 0){
            return TYPE_CONTENT_ITEM;
        }else{
            return TYPE_TITLE_ITEM;
        }
    }

    /**
     * 多类型适配复写方法<br/>
     * 返回类型总数<br/>
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TViewHolder holder = null;
        int type = getItemViewType(position);
        switch (type){
            case TYPE_TITLE_ITEM:
                holder = TViewHolder.create(context, convertView, parent, R.layout.other_gradualtitle_main_item_title);
                inflateTitleView(holder);
                break;
            default:
                holder = TViewHolder.create(context, convertView, parent, R.layout.common_list_item);
                inflateContentView(position, holder);
                break;
        }
        return holder.getConvertView();
    }

    /**********************************************
     * private
     */

    private void inflateTitleView(TViewHolder holder) {
        ImageView imageView = holder.get(R.id.other_gradualtitle_main_item_title_imageview);
        imageView.setAlpha(200);
    }

    /**
     * 渲染View
     *
     * @param position 位置
     * @param holder   holder
     */
    private void inflateContentView(int position, TViewHolder holder) {
        String tail = Integer.toString(position);

        TextView titleView = holder.get(R.id.common_list_item_title);
        TextView typeView = holder.get(R.id.common_list_item_type);
        TextView infoView = holder.get(R.id.common_list_item_info);

        if (title != null)
            titleView.setText(title + tail);
        else
            titleView.setText("");

        titleView.setTextColor(titleColor);

        if (type != null)
            typeView.setText(type + tail);
        else
            typeView.setText("");

        if (info != null)
            infoView.setText(info + tail);
        else
            infoView.setText("");

    }
}
