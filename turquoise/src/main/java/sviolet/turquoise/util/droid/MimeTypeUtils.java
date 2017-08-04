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

package sviolet.turquoise.util.droid;

import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

/**
 * MimeType工具, 用于WebView操作
 *
 * 例如:
 * return new WebResourceResponse(MimeTypeUtils.getFromUrl(url), "UTF-8", inputStream)；
 *
 * Created by S.Violet on 2017/8/4.
 */
public class MimeTypeUtils {

    public static String getFromFile(@NonNull String filePath) {
        int offset = filePath.lastIndexOf(".");
        if(offset > -1) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(filePath.substring(offset + 1));
        } else {
            return "application/octet-stream";
        }
    }

    public static String getFromUrl(@NonNull String url) {
        return getFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
    }

    public static String getFromExtension(@NonNull String extension) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null ? mimeType : (
                extension.equalsIgnoreCase("htm") ? "text/html" : (
                extension.equalsIgnoreCase("html") ? "text/html" : (
                extension.equalsIgnoreCase("js") ? "application/x-javascript" : (
                extension.equalsIgnoreCase("css") ? "text/css" : (
                extension.equalsIgnoreCase("ttf") ? "application/x-font-ttf" : (
                extension.equalsIgnoreCase("eot") ? "application/vnd.ms-fontobject" : (
                extension.equalsIgnoreCase("svg") ? "image/svg+xml" : (
                extension.equalsIgnoreCase("woff") ? "application/font-woff" : (
                extension.equalsIgnoreCase("otf") ? "application/x-font-opentype" :
                null)))))))));
    }
}