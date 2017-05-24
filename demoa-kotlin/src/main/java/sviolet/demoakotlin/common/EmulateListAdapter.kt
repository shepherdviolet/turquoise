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

package sviolet.demoakotlin.common

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import sviolet.demoakotlin.R
import sviolet.turquoise.ui.adapter.TViewHolder

/**
 * 模拟List适配器
 *
 *
 * Created by S.Violet on 2015/6/3.
 */
class EmulateListAdapter
/**
 * @param quantity 列表项数量
 * *
 * @param title 标题
 * *
 * @param type 类型
 * *
 * @param info 说明
 * *
 * @param titleColor 标题颜色
 */
@JvmOverloads constructor(private val context: Context, private val quantity: Int//列表项数量
                          , private val title: String?//标题
                          , private val type: String?//类型
                          , private val info: String?//信息
                          , private val titleColor: Int = 0xFF303030.toInt()//标题字体颜色
) : BaseAdapter() {

    override fun getCount(): Int {
        return quantity
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val holder = TViewHolder.create(context, convertView, parent, R.layout.common_list_item)
        inflateView(position, holder)
        return holder.convertView
    }

    /**********************************************
     * private
     */

    /**
     * 渲染View

     * @param position 位置
     * *
     * @param holder   holder
     */
    private fun inflateView(position: Int, holder: TViewHolder) {
        val tail = Integer.toString(position)

        val titleView = holder.get<TextView>(R.id.common_list_item_title)
        val typeView = holder.get<TextView>(R.id.common_list_item_type)
        val infoView = holder.get<TextView>(R.id.common_list_item_info)

        if (title != null)
            titleView.text = title + tail
        else
            titleView.text = ""

        titleView.setTextColor(titleColor)

        if (type != null)
            typeView.text = type + tail
        else
            typeView.text = ""

        if (info != null)
            infoView.text = info + tail
        else
            infoView.text = ""

    }

}
/**
 * @param quantity 列表项数量
 * *
 * @param title 标题
 * *
 * @param type 类型
 * *
 * @param info 说明
 */