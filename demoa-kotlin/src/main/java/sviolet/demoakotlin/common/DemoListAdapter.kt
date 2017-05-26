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

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import sviolet.demoakotlin.R
import sviolet.turquoise.kotlin.extensions.safeGet
import sviolet.turquoise.ui.adapter.TViewHolder
import kotlin.reflect.KClass

/**
 * Demo列表适配器

 * Created by S.Violet on 2015/6/2.
 */
class DemoListAdapter
/**
 * @param context context
 * @param activityList 要显示的activity
 */
constructor (
        private val context: Context,
        private val resId: Int,
        private val activityList: Array<out KClass<out Activity>>?
) : BaseAdapter() {

    override fun getCount(): Int {
        return activityList?.size ?: 0
    }

    override fun getItem(position: Int): Any? {
        return activityList.safeGet(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val holder = TViewHolder.create(context, convertView, parent, resId)
        inflateView(position, holder)
        return holder.convertView
    }

    /****************************************************
     * private
     */

    /**
     * 渲染View
     * @param position 位置
     * *
     * @param holder holder
     */
    private fun inflateView(position: Int, holder: TViewHolder) {
        val activity = getItem(position) as Class<*>? ?: return
        if (activity.isAnnotationPresent(DemoDescription::class.java)) {
            val description = activity.getAnnotation(DemoDescription::class.java)
            setViewParams(holder, description.title, description.type, description.info)
        } else {
            setViewParams(holder, null, null, null)
        }
    }

    /**
     * 设置View的显示值
     */
    private fun setViewParams(holder: TViewHolder?, title: String?, type: String?, info: String?) {
        if (holder == null)
            return

        if (title != null)
            holder.get(R.id.guide_main_item_title, TextView::class.java).text = title
        else
            holder.get(R.id.guide_main_item_title, TextView::class.java).text = "未设置@DemoDescription"

        holder.get(R.id.guide_main_item_type, TextView::class.java).text = type
        holder.get(R.id.guide_main_item_info, TextView::class.java).text = info
    }

}
