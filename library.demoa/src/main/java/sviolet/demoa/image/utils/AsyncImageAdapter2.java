package sviolet.demoa.image.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sviolet.demoa.R;
import sviolet.turquoise.utils.bitmap.loader.AsyncBitmapDrawable;
import sviolet.turquoise.utils.bitmap.loader.AsyncBitmapDrawableLoader;
import sviolet.turquoise.utils.sys.MeasureUtils;
import sviolet.turquoise.view.GradualImageView;

/**
 * ListView适配器
 * Created by S.Violet on 2015/7/7.
 */
public class AsyncImageAdapter2 extends BaseAdapter {

    private Context context;
    private List<AsyncImageItem> itemList;
    private AsyncBitmapDrawableLoader asyncBitmapDrawableLoader;
    private int widthHeightLarge, widthHeightSmall;

    /**
     * @param context context
     * @param itemList 数据
     * @param asyncBitmapDrawableLoader 用于图片动态加载缓存
     */
    public AsyncImageAdapter2(Context context, List<AsyncImageItem> itemList, AsyncBitmapDrawableLoader asyncBitmapDrawableLoader){
        this.context = context;
        this.itemList = itemList;
        this.asyncBitmapDrawableLoader = asyncBitmapDrawableLoader;

        //图片大小尺寸的长宽值
        widthHeightLarge = MeasureUtils.dp2px(context, 160);//160dp*160dp
        widthHeightSmall = MeasureUtils.dp2px(context, 80);//80dp*80dp
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        if (view == null){
            view = View.inflate(context, R.layout.image_async_item, null);
            holder = new ViewHolder();
            holder.imageView[0] = (GradualImageView) view.findViewById(R.id.image_async_item_imageview0);
            holder.imageView[1] = (GradualImageView) view.findViewById(R.id.image_async_item_imageview1);
            holder.imageView[2] = (GradualImageView) view.findViewById(R.id.image_async_item_imageview2);
            holder.imageView[3] = (GradualImageView) view.findViewById(R.id.image_async_item_imageview3);
            holder.imageView[4] = (GradualImageView) view.findViewById(R.id.image_async_item_imageview4);
            holder.titleTextView = (TextView) view.findViewById(R.id.image_async_item_title);
            holder.contentTextView = (TextView) view.findViewById(R.id.image_async_item_content);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        AsyncImageItem item = itemList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());

        for (int i = 0 ; i < 5 ; i++) {
            /**
             * [重要]必须在设置新图前, 将原来的AsyncBitmapDrawable置为不再使用, 这样能取消原图的加载任务,
             * 保证需要显示的图能尽快加载出来, 使得滑动过程中的加载任务不占用等待队列, 防止滚动停止
             * 后界面中有图片未加载<br/>
             */
            AsyncBitmapDrawable drawable = (AsyncBitmapDrawable) holder.imageView[i].getDrawable();
            if (drawable != null)
                drawable.unused();

            /**
             * asyncBitmapDrawableLoader.load()方法返回的AsyncBitmapDrawable直接赋给ImageView
             */
            if (i == 0) {
                //第一张图为160*160dp, 其余80*80dp
                //加载成功逐渐显示动画效果
                holder.imageView[i].setImageDrawableImmediate(asyncBitmapDrawableLoader.load(item.getUrl(i), widthHeightLarge, widthHeightLarge));
            } else {
                holder.imageView[i].setImageDrawableImmediate(asyncBitmapDrawableLoader.load(item.getUrl(i), widthHeightSmall, widthHeightSmall));
            }

        }
        return view;
    }

    private class ViewHolder{
        TextView titleTextView;
        TextView contentTextView;
        GradualImageView[] imageView = new GradualImageView[5];
    }

}
