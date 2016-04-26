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

package sviolet.turquoise.ui.util;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * <p>[RecyclerView专用] RecyclerView.ViewHolder增强</p>
 *
 * <p>建议直接使用{@link TRecyclerViewAdapter}.</p>
 *
 * <pre>{@code
 *  public class XXXAdapter extends RecyclerView.Adapter<TRecyclerViewHolder> {
 *
 *      public TRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
 *          //实例化TRecyclerViewHolder
 *          return new TRecyclerViewHolder(context, parent, R.layout.xxx);
 *      }
 *
 *      public void onBindViewHolder(TRecyclerViewHolder holder, int position) {
 *          //装载数据
 *          ((TextView)holder.get(R.id.xxx)).setText(datas.get(position));
 *      }
 *
 *      public int getItemCount() {
 *          return datas.size();
 *      }
 *
 *  }
 *
 * }</pre>
 *
 * Created by S.Violet on 2016/4/26.
 */
public class TRecyclerViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> subViews = new SparseArray<>();

    /**
     * <p>[RecyclerView专用] RecyclerView.ViewHolder增强</p>
     *
     * @param context context
     * @param parent ViewGroup
     * @param layoutResId item's layout resource id
     */
    public TRecyclerViewHolder(Context context, ViewGroup parent, int layoutResId){
        this(LayoutInflater.from(context).inflate(layoutResId, parent, false));
    }

    /**
     * <p>[RecyclerView专用] RecyclerView.ViewHolder增强</p>
     *
     * @param itemView itemView
     */
    public TRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * 创建(获取)itemView的子控件
     *
     * @param resId 子控件资源ID
     * @return 子控件
     */
    public View get(int resId){
        View view = subViews.get(resId);
        if (view == null){
            view = findView(resId);
            if (view != null){
                subViews.put(resId, view);
            }
        }
        return view;
    }

    private View findView(int resId){
        if (itemView == null){
            throw new RuntimeException("[TViewHolder]get: convertView is null");
        }
        try {
            return itemView.findViewById(resId);
        }catch(Exception e){
            throw new RuntimeException("[TViewHolder]get: error when findViewById", e);
        }
    }

    TRecyclerViewHolder bindClickListener(final OnItemClickListener onClickListener){
        if (onClickListener != null){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(v, getLayoutPosition());
                }
            });
        }
        return this;
    }

    TRecyclerViewHolder bindLongClickListener(final OnItemLongClickListener onLongClickListener){
        if (onLongClickListener != null){
            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    return onLongClickListener.onLongClick(v, getLayoutPosition());
                }
            });
        }
        return this;
    }

    /**
     * [RecyclerView专用]点击事件监听器
     */
    public interface OnItemClickListener{

        void onClick(View v, int position);

    }

    /**
     * [RecyclerView专用]长按事件监听器
     */
    public interface OnItemLongClickListener{

        boolean onLongClick(View v, int position);

    }

}
