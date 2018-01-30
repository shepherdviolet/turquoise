/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * <p>[RecyclerView专用] RecyclerView.Adapter增强</p>
 *
 * <p>Dependency:com.android.support:recyclerview-v7</p>
 *
 * <pre>{@code
 *
 *  protected void onCreate(Bundle savedInstanceState) {
 *      super.onCreate(savedInstanceState);
 *      //实例化
 *      adapter = new MyAdapter(this, deviceList);
 *      //设置监听器(必须在RecyclerView绘制之前调用, 通常在onCreate中调用)
 *      adapter.setOnItemClickListener(new TRecyclerViewHolder.OnItemClickListener() {
 *          public void onClick(View v, int position) {
 *              //点击事件
 *          }
 *      });
 *      adapter.setOnItemLongClickListener(new TRecyclerViewHolder.OnItemLongClickListener() {
 *          public boolean onLongClick(View v, int position) {
 *              //长按事件
 *              return true;
 *          }
 *      });
 *      recyclerView.setLayoutManager(new LinearLayoutManager(this));
 *      recyclerView.setAdapter(adapter);
 *  }
 *
 *  public class MyAdapter extends TRecyclerViewAdapter {
 *
 *      private Context context;
 *      private List<String> dataList;
 *
 *      public MyAdapter(Context context, List<String> dataList) {
 *          this.context = context;
 *          this.dataList = dataList;
 *      }
 *
 *      public int chooseLayoutId(int viewType) {
 *          //选择viewType对应的layoutResId
 *          return R.layout.item;
 *      }
 *
 *      public Context getContext() {
 *          //返回context
 *          return context;
 *      }
 *
 *      public void onBindViewHolder(TRecyclerViewHolder holder, int position) {
 *          //装载数据
 *          holder.<TextView>get(R.id.item_text).setText(dataList.get(position));
 *      }
 *
 *      public int getItemCount() {
 *          return dataList.size();
 *      }
 *
 *  }
 * }</pre>
 *
 * Created by S.Violet on 2016/4/26.
 */
public abstract class TRecyclerViewAdapter extends RecyclerView.Adapter<TRecyclerViewHolder> {

    private TRecyclerViewHolder.OnItemClickListener onItemClickListener;
    private TRecyclerViewHolder.OnItemLongClickListener onItemLongClickListener;

    @Override
    public final TRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create holder
        TRecyclerViewHolder holder = new TRecyclerViewHolder(getContext(), parent, chooseLayoutId(viewType));
        //bind listener
        if (onItemClickListener != null){
            holder.bindClickListener(onItemClickListener);
        }
        if (onItemLongClickListener != null){
            holder.bindLongClickListener(onItemLongClickListener);
        }
        return holder;
    }

    /**
     * Select the layout resource Id depending on the viewType
     * @param viewType view type
     * @return layout resource Id, can't be null
     */
    public abstract int chooseLayoutId(int viewType);

    /**
     * get Context
     * @return can't be null
     */
    public abstract Context getContext();

    /**
     * <p>set item click listener, effective before RecyclerView drawing, invoke in onCreate please</p>
     */
    public TRecyclerViewAdapter setOnItemClickListener(TRecyclerViewHolder.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    /**
     * <p>set item long click listener, effective before RecyclerView drawing, invoke in onCreate please</p>
     */
    public TRecyclerViewAdapter setOnItemLongClickListener(TRecyclerViewHolder.OnItemLongClickListener onItemLongClickListener){
        this.onItemLongClickListener = onItemLongClickListener;
        return this;
    }

}
