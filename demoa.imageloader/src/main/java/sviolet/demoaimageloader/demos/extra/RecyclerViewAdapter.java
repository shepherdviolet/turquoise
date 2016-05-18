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

import java.util.List;

import sviolet.demoaimageloader.R;
import sviolet.turquoise.ui.util.TRecyclerViewAdapter;
import sviolet.turquoise.ui.util.TRecyclerViewHolder;
import sviolet.turquoise.x.imageloader.TILoader;

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
        TILoader.node(context).load(dataList.get(position).getUrl(0), holder.get(R.id.recycler_view_main_item_image));
    }

}
