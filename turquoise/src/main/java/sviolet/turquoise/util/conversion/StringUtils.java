/*
 * Copyright (C) 2015 S.Violet
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 字符串工具
 * Created by S.Violet on 2015/12/22.
 */
public class StringUtils {

    /**
     * 将字符串指定位置变为大写(字母)
     * @param src 源字符串
     * @param positions 变为大写的位置[0, length)
     * @return 变换后的字符串
     */
    public static String toUpperCase(String src, int... positions){
        if (src == null)
            return null;
        char[] chars = src.toCharArray();
        for (int position : positions){
            if(position < chars.length && position > -1){
                chars[position] -= (chars[position] > 96 && chars[position] < 123) ? 32 : 0;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * 将字符串指定位置变为小写(字母)
     * @param src 源字符串
     * @param positions 变为小写的位置[0, length)
     * @return 变换后的字符串
     */
    public static String toLowerCase(String src, int... positions){
        if (src == null)
            return null;
        char[] chars = src.toCharArray();
        for (int position : positions){
            if(position < chars.length && position > -1){
                chars[position] += (chars[position] > 64 && chars[position] < 91) ? 32 : 0;
            }
        }
        return String.valueOf(chars);
    }

    /**
     * 把异常转为String信息
     */
    public static String throwableToString(Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        printWriter.close();
        return writer.toString();
    }

}
