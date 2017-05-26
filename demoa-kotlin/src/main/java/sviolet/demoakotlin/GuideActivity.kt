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

package sviolet.demoakotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import sviolet.demoakotlin.common.DemoDefault
import sviolet.demoakotlin.common.DemoList
import sviolet.demoakotlin.common.DemoListAdapter
import sviolet.demoakotlin.logger.LoggerActivity
import sviolet.turquoise.enhance.app.TActivity
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings
import sviolet.turquoise.ui.util.motion.MultiClickFilter
import kotlin.reflect.KClass

/**************************************************************
 * Demo配置
 */
//默认Demo
//@DemoDefault(
//        TempActivity::class
//)

// Demo列表
@DemoList(
    LoggerActivity::class
)
/**************************************************************
 * Activity
 */

@ResourceId(R.layout.guide_main)
@ActivitySettings(
        optionsMenuId = R.menu.menu_guide,
        noTitle = false,
        statusBarColor = 0xFF30C0C0.toInt(),
        navigationBarColor = 0xFF30C0C0.toInt()
)
open class GuideActivity : TActivity() {

    @ResourceId(R.id.guide_main_listview)
    private val demoListView: ListView? = null
    private var demoListAdapter: DemoListAdapter? = null

    private val multiClickFilter = MultiClickFilter()

    override fun onInitViews(savedInstanceState: Bundle?) {
        injectDemoListView()
        injectDefaultDemo()
    }

    override fun onResume() {
        super.onResume()
    }

    /**
     * 菜单
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.guide_menu_settings -> {
                return true
            }
            R.id.guide_menu_about -> {
                //版本显示
                Toast.makeText(this, "Kotlin Demo " + BuildConfig.VERSION_NAME, Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**************************************************************
     * private
     */

    /**
     * 进入指定的Activity

     * @param activity 指定的Acitivity
     */
    private fun go(activity: KClass<out Activity>?) {
        if (activity == null)
            return
        val intent = Intent(this, activity.java)
        startActivity(intent)
    }

    /**
     * 注入并进入默认Demo
     */
    private fun injectDefaultDemo() {
        if (this.javaClass.isAnnotationPresent(DemoDefault::class.java)) {
            val activity = this.javaClass.getAnnotation<DemoDefault>(DemoDefault::class.java).value
            go(activity)
        }
    }

    /**
     * 注入并显示Demo列表
     */
    private fun injectDemoListView() {
        if (this.javaClass.isAnnotationPresent(DemoList::class.java)) {
            val activityList = this.javaClass.getAnnotation<DemoList>(DemoList::class.java).value
            demoListAdapter = DemoListAdapter(this, R.layout.guide_main_item, activityList)
            demoListView?.adapter = demoListAdapter
            demoListView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if (view != null) {
                    if (!multiClickFilter.tryHandle()) {
                        return@OnItemClickListener
                    }
                    go(demoListAdapter?.getItem(position) as KClass<out Activity>)
                }
            }
        }
    }
}