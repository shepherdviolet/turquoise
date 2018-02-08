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

import java.util.List;

import sviolet.demoaimageloader.R;
import sviolet.turquoise.ui.adapter.TRecyclerViewAdapter;
import sviolet.turquoise.ui.adapter.TRecyclerViewHolder;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;

/**
 * Created by S.Violet on 2016/5/18.
 */
public class RecyclerViewAdapter extends TRecyclerViewAdapter {

    private Activity context;
    private List<AsyncImageItem> dataList;

    public RecyclerViewAdapter(Activity context, List<AsyncImageItem> dataList){
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int chooseLayoutId(int viewType) {
        return R.layout.recycler_view_main_item;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public void onBindViewHolder(TRecyclerViewHolder holder, int position) {
        TILoader.node(context).load(dataList.get(position).getUrl(0), holder.get(R.id.recycler_view_main_item_image), new Params.Builder()
                .setSourceType(Params.SourceType.URL_TO_QR_CODE)
                .setBitmapConfig(Bitmap.Config.ALPHA_8)//save memory
                .addExtra(DecodeHandler.EXTRA_REQ_DIMENSION_ZOOM, 0.5f)//save memory
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_CHARSET, "utf-8")
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_MARGIN, 1)
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_CORRECTION_LEVEL, ZxingUtils.CorrectionLevel.M)
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_FORCE_SQUARE, false)
                .build());
    }

}
