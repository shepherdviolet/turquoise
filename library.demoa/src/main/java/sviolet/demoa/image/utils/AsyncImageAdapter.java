package sviolet.demoa.image.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import sviolet.demoa.R;
import sviolet.turquoise.app.TActivity;
import sviolet.turquoise.io.BitmapLoader;
import sviolet.turquoise.utils.BitmapUtils;
import sviolet.turquoise.utils.CachedBitmapUtils;
import sviolet.turquoise.utils.MeasureUtils;
import sviolet.turquoise.view.GradualImageView;

/**
 * ListView适配器
 * Created by S.Violet on 2015/7/7.
 */
public class AsyncImageAdapter extends BaseAdapter {

    private static final String DEFAULT_BITMAP_KEY = "default_bitmap";

    private Context context;
    private List<AsyncImageItem> itemList;
    private BitmapLoader bitmapLoader;
    private CachedBitmapUtils cachedBitmapUtils;

    /**
     * @param context context
     * @param itemList 数据
     * @param bitmapLoader 用于图片动态加载缓存
     * @param cachedBitmapUtils 用于解码默认图(TActivity.getCachedBitmapUtils())
     */
    public AsyncImageAdapter(Context context, List<AsyncImageItem> itemList, BitmapLoader bitmapLoader, CachedBitmapUtils cachedBitmapUtils){
        this.context = context;
        this.itemList = itemList;
        this.bitmapLoader = bitmapLoader;
        this.cachedBitmapUtils = cachedBitmapUtils;
        //用CachedBitmapUtils解码的默认图, 会缓存在其内建BtimapCache中, 在TActivity.onDestroy()时会回收资源
        cachedBitmapUtils.decodeFromResource(DEFAULT_BITMAP_KEY, context.getResources(), R.mipmap.async_image_null);
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
            for (int i = 0 ; i < 5 ; i++){
                //设置背景为默认图片, 从cachedBitmapUtils缓存中取
                holder.imageView[i].setBackgroundDrawable(BitmapUtils.bitmapToDrawable(cachedBitmapUtils.getBitmap(DEFAULT_BITMAP_KEY)));
            }
        }else{
            holder = (ViewHolder) view.getTag();
            for (int i = 0 ; i < 5 ; i++){
                //去除ImageView中原有图片
                holder.imageView[i].setImageBitmapImmediate(null);
                //将之前的位图资源置为unused状态以便回收资源 [重要]
                bitmapLoader.unused(holder.url[i], holder.key[i]);
            }
        }
        AsyncImageItem item = itemList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());
        //将url和key记录在holder中, 用于后续执行unused
        holder.url = item.getUrls();
        holder.key = item.getKeys();

        for (int i = 0 ; i < 5 ; i++) {
            //从内存缓存中取位图
            Bitmap bitmap = bitmapLoader.get(item.getUrl(i), item.getKey(i));
            if (bitmap != null && !bitmap.isRecycled()) {
                //若内存缓存中存在, 则直接设置图片
                holder.imageView[i].setImageBitmapImmediate(bitmap);
            }else {
                //若内存缓存中不存在, 交由BitmapLoader.load异步加载
                //图片需要显示的尺寸, 大图160dp, 小图80dp
                int widthHeight = i == 0 ? MeasureUtils.dp2px(context, 160) : MeasureUtils.dp2px(context, 80);
                //异步加载, BitmapLoader会根据需求尺寸加载合适大小的位图, 以节省内存
                //将ImageView作为参数传入, 便于在回调函数中设置图片
                bitmapLoader.load(item.getUrl(i), item.getKey(i), widthHeight, widthHeight, holder.imageView[i], mOnLoadCompleteListener);
            }
        }
        return view;
    }

    /**
     * 图片异步加载回调
     */
    private BitmapLoader.OnLoadCompleteListener mOnLoadCompleteListener = new BitmapLoader.OnLoadCompleteListener() {
        @Override
        public void onLoadSucceed(String url, String key, Object params, Bitmap bitmap) {
            //参数为load传入的ImageView
            GradualImageView imageView = ((GradualImageView) params);
            if (bitmap != null && !bitmap.isRecycled()) {
                //若图片存在且未被回收, 设置图片(渐渐显示)
                imageView.setImageBitmapGradual(bitmap);
            } else {
                //若图片不存在, 可以考虑重新发起加载请求
                //loader.load(url, key, widthHeight, widthHeight, params, mOnLoadCompleteListener);
                //此Demo不做重发
                if (context instanceof TActivity) {
                    ((TActivity) context).getLogger().e("[AsyncImageAdapter]加载成功后找不到位图url:" + url + " key:" + key);
                }
                Toast.makeText(context, "[AsyncImageAdapter]加载成功后找不到位图url:" + url + " key:" + key, Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onLoadFailed(String url, String key, Object params) {
            //加载失败处理
        }
        @Override
        public void onLoadCanceled(String url, String key, Object params) {
            //加载取消处理
        }
    };

    private class ViewHolder{
        TextView titleTextView;
        TextView contentTextView;
        GradualImageView[] imageView = new GradualImageView[5];
        String[] url = new String[5];
        String[] key = new String[5];
    }

}
