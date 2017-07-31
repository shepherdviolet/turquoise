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

package sviolet.turquoise.util.conversion;

import android.text.Html;
import android.text.Spanned;

/**
 * Html格式字符串转换工具
 *
 * Created by S.Violet on 2017/7/31.
 */
public class HtmlStringUtils {

    /**
     * 将包含HTML标签的String, 转为Spanned对象, 使得TextView可以显示HTML格式的文字
     * @param stringWithHtmlTags 包含HTML标签的String
     * @return Spanned
     */
    public static Spanned toHtmlSpanned(String stringWithHtmlTags){
        if (stringWithHtmlTags == null){
            return null;
        }
        return Html.fromHtml(stringWithHtmlTags);
    }

    /**
     * 去除String中的HTML标签
     * @param stringWithHtmlTags 包含HTML标签的String
     * @return String
     */
    public static String trimHtmlTags(String stringWithHtmlTags){
        if (stringWithHtmlTags == null){
            return null;
        }
        return Html.fromHtml(stringWithHtmlTags).toString();
    }

}
