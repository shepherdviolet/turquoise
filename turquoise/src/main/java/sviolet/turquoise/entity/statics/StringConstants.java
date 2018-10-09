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

package sviolet.turquoise.entity.statics;

/**
 * String常量
 *
 * Created by S.Violet on 2016/3/4.
 */
public class StringConstants {

    /*******************************************************************************
     * Logger TAG
     */

    public static final String LIBRARY_TAG = "Turquoise";//库名称

    /*******************************************************************************
     * LifeCycle
     */

    public static final String LIFECYCLE_FRAGMENT_TAG = "TURQUOISE_LIFE_CYCLE_FRAGMENT";//生命周期Fragment的TAG名
    public static final String LIFECYCLE_MANAGER_TAG = "TURQUOISE_LIFE_CYCLE_MANAGER";//生命周期管理器在ParasiticVars中的key

    /*******************************************************************************
     * Shared Preferences
     */

    //MultiDex
    public static final String MULTI_DEX_PREF_NAME = "turquoise_multi_dex";
    public static final String MULTI_DEX_PREF_SHA1_KEY = "second_dex_sha1";
    public static final String MULTI_DEX_PREF_VERSION_KEY = "version";

}
