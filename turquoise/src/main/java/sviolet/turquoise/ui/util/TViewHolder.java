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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sviolet.turquoise.common.statics.SpecialResourceId;

/**
 * ViewHolder工具, 用于Adapter<br/>
 *
 * <pre>{@code
 *     public View getView(int position, View convertView, ViewGroup parent) {
 *          //获得ViewHolder
 *          //第一次create会inflate产生convertView实例, 并产生ViewHolder实例, 用View.setTag绑定在convertView上
 *          //同一个convertView第二次create时, 会用View.getTag直接获取ViewHolder实例
 *          TViewHolder holder = TViewHolder.create(context, convertView, parent, R.layout.xxx);
 *
 *          //获取控件
 *          LinearLayoutDrawer drawer = holder.get(R.id.drawer);
 *          TextView titleView = holder.get(R.id.title);
 *
 *          //可选代码块
 *          //createTimes()方法可以获得同一个convertView create次数
 *          //第一次create时, 可以进行一些控件初始化操作
 *          //二次以上create时, 可以进行一些清理重置操作
 *          if(holder.createTimes() == 1){
 *              //控件初始化操作
 *              //例如: 控件绑定监听器
 *              titleView.setOnClickListener(...);
 *              //例如: 抽屉控件初始化
 *              drawer.setSlideScrollDirection(LinearLayoutDrawer.DIRECTION_LEFT)
 *                  .setSlideDrawerWidth(200)
 *                  .setSlideScrollDuration(700)
 *                  .setSlideInitStage(LinearLayoutDrawer.STAGE_PULL_OUT)
 *                  .applySlideSetting();
 *          }else{
 *              //重置操作
 *              //重置抽屉位置
 *              drawer.pullOutImmediately();
 *          }
 *
 *          //填充数据
 *          Item item = getItem(position);
 *          titleView.setText(item.getTitle());
 *
 *          //必须!!!
 *          //从TViewHolder中获取convertView实例
 *          //切记不能直接返回convertView, 会抛出NullPointException, 必须用holder.getConvertView()从ViewHolder获取
 *          return holder.getConvertView();
 *     }
 * }</pre>
 *
 *
 * 注意:请勿手动持有ViewHolder对象, 这样可能会造成内存泄露. ViewHolder会自动用View.setTag绑定在convertView上.<p/>
 *
 * Created by S.Violet on 2016/1/13.
 */
public class TViewHolder {

    private View convertView;
    private SparseArray<View> views = new SparseArray<>();
    private long createTimes = 0;

    private TViewHolder(View convertView){
        this.convertView = convertView;
    }

    /**
     * 创建(获取)convertView的ViewHolder实例<p/>
     *
     * 1.第一次create会inflate产生convertView实例, 并产生ViewHolder实例, 用View.setTag绑定在convertView上<Br/>
     * 2.同一个convertView第二次create时, 会用View.getTag直接获取ViewHolder实例<br/>
     * 3.每次create都会使得createTimes次数+1<br/>
     *
     * @param context context
     * @param convertView convertView
     * @param parent parent
     * @param layoutResId 资源ID
     * @return TViewHolder
     */
    public static TViewHolder create(Context context, View convertView, ViewGroup parent, int layoutResId){
        if (convertView == null){
            if (context == null){
                throw new RuntimeException("[TViewHolder]create: context must not be null");
            }
            try {
                convertView = LayoutInflater.from(context).inflate(layoutResId, parent, false);
            }catch(Exception e){
                throw new RuntimeException("[TViewHolder]create: error when inflating View", e);
            }
        }else{
            Object holder = convertView.getTag(SpecialResourceId.ViewTag.ViewHolder);
            if (holder instanceof TViewHolder){
                TViewHolder TViewHolder = (TViewHolder) holder;
                TViewHolder.incrementCreateTimes();
                return TViewHolder;
            }
        }
        TViewHolder holder = new TViewHolder(convertView);
        convertView.setTag(SpecialResourceId.ViewTag.ViewHolder, holder);
        holder.incrementCreateTimes();
        return holder;
    }

    /**
     * 获取由ViewHolder.create()方法所创建(inflate)出来的convertView实例
     *
     * @return 获取convertView实例
     */
    public View getConvertView(){
        return convertView;
    }

    /**
     * 创建(获取)convertView的子控件
     *
     * @param resId 子控件资源ID
     * @return 获取convertView的子控件
     */
    public <V extends View> V get(int resId){
        View view = views.get(resId);
        if (view == null){
            view = findView(resId);
            if (view != null){
                views.put(resId, view);
            }
        }
        return (V) view;
    }

    /**
     * create次数 (指ViewHolder.create())<p/>
     *
     * 通常用于判断convertView是否为新实例, 当createTimes() == 1 时可进行一些控件初始化操作, 如:绑定监听器等
     */
    public long createTimes(){
        return createTimes;
    }

    private void incrementCreateTimes(){
        createTimes++;
    }

    private View findView(int resId){
        if (getConvertView() == null){
            throw new RuntimeException("[TViewHolder]get: convertView is null");
        }
        try {
            return getConvertView().findViewById(resId);
        }catch(Exception e){
            throw new RuntimeException("[TViewHolder]get: error when findViewById", e);
        }
    }

}
