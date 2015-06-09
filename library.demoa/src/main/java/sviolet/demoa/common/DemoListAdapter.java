package sviolet.demoa.common;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import sviolet.demoa.R;

/**
 * Demo列表适配器
 *
 * Created by S.Violet on 2015/6/2.
 */
public class DemoListAdapter extends BaseAdapter {

    private Context context;
    private int resId;
    private Class<? extends Activity>[] activityList;

    /**
     *
     * @param context context
     * @param activityList 要显示的activity
     */
    public DemoListAdapter(Context context, int resId, Class<? extends Activity>[] activityList) {
        this.context = context;
        this.resId = resId;
        this.activityList = activityList;
    }

    @Override
    public int getCount() {
        if (activityList != null)
            return activityList.length;
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        if (activityList != null && position < activityList.length && position >= 0)
            return activityList[position];
        else
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
            convertView = View.inflate(context, resId, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.guide_main_item_title);
            holder.type = (TextView) convertView.findViewById(R.id.guide_main_item_type);
            holder.info = (TextView) convertView.findViewById(R.id.guide_main_item_info);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        inflateView(position, convertView, holder);
        return convertView;
    }

    /****************************************************
     * private
     */

    /**
     * 渲染View
     * @param position 位置
     * @param view view
     * @param holder holder
     */
    private void inflateView(int position, View view, ViewHolder holder) {
        Class<? extends Activity> activity = (Class) getItem(position);
        if (activity == null)
            return;
        if (activity.isAnnotationPresent(DemoDescription.class)) {
            DemoDescription description = activity.getAnnotation(DemoDescription.class);
            setViewParams(holder, description.title(), description.type(), description.info());
        } else {
            setViewParams(holder, null, null, null);
        }
    }

    /**
     * 设置View的显示值
     */
    private void setViewParams(ViewHolder holder, String title, String type, String info) {
        if (holder == null)
            return;

        if (title != null)
            holder.title.setText(title);
        else
            holder.title.setText("未设置@DemoDescription");

        holder.type.setText(type);
        holder.info.setText(info);
    }

    private class ViewHolder {
        TextView title;
        TextView type;
        TextView info;
    }

}
