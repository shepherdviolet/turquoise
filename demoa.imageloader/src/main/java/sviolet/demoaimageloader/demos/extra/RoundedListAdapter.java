/*
 * Copyright (C) 2015-2016 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.demoaimageloader.demos.extra;

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

import sviolet.demoaimageloader.R;
import sviolet.turquoise.ui.adapter.TViewHolder;
import sviolet.turquoise.util.bitmap.BitmapUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.stub.Stub;
import sviolet.turquoise.x.imageloader.stub.StubRemoter;

/**
 * 圆角图列表适配器
 *
 * Created by S.Violet on 2016/5/11.
 */
public class RoundedListAdapter extends BaseAdapter {

    private Activity context;
    private List<AsyncImageItem> itemList;

    private Params params;

    public RoundedListAdapter(Activity context, List<AsyncImageItem> itemList){
        this.context = context;
        this.itemList = itemList;

        params = new Params.Builder()
                .setReqSize(600, 400)//提前约定的图片尺寸
                .setBitmapConfig(Bitmap.Config.ARGB_8888)//圆角需要透明度
                .setDecodeScaleStrategy(DecodeHandler.DecodeScaleStrategy.SCALE_FIT_WIDTH)
                .setDecodeInterceptor(new DecodeHandler.Interceptor() {
                    @Override
                    public ImageResource intercept(Context applicationContext, Context context, Task.Info taskInfo, ImageResource imageResource, TLogger logger) {
                        switch (imageResource.getType()){
                            case BITMAP:
                                Bitmap bitmap = BitmapUtils.toRoundedCorner((Bitmap) imageResource.getResource(), MeasureUtils.dp2px(context, 2), BitmapUtils.RoundedCornerType.TopLeft_And_TopRight, true);
                                return new ImageResource(ImageResource.Type.BITMAP, bitmap);
                            default:
                                break;
                        }
                        return imageResource;
                    }
                })
                .build();
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
        TViewHolder holder = TViewHolder.create(context, convertView, parent, R.layout.rounded_list_main_item);

        AsyncImageItem item = itemList.get(position);
        holder.get(R.id.rounded_list_item_topic, TextView.class).setText(item.getTitle());
        holder.get(R.id.rounded_list_item_state, TextView.class).setText(item.getContent());

        final ImageView imageView = holder.get(R.id.rounded_list_item_image);

        if (holder.createTimes() == 1){
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //StubRemoter可以对imageView的图片加载任务进行一些控制, 或获得状态
                    StubRemoter remoter = TILoaderUtils.getStubRemoter(imageView);
                    if (remoter.getLoadState() == Stub.State.LOAD_SUCCEED) {
                        //图片加载成功状态
                        Toast.makeText(context, "click (succeed)", Toast.LENGTH_SHORT).show();
                    } else if (remoter.getLoadState() == Stub.State.LOAD_CANCELED) {
                        //图片加载取消状态, 可以进行重新加载, 其他状态调用relaunch无效
                        if (remoter.relaunch()) {
                            Toast.makeText(context, "relaunch:" + remoter.getUrl(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //图片加载成功状态
                        Toast.makeText(context, "click (unknown)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        TILoader.node(context).load(item.getUrl(0), params, imageView);

        return holder.getConvertView();
    }

}
