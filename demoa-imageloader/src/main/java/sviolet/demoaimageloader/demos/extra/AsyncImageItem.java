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

package sviolet.demoaimageloader.demos.extra;

/**
 *
 * Created by S.Violet on 2015/7/7.
 */
public class AsyncImageItem {

    private String title;
    private String content;
    private String[] url = new String[5];

    public String getTitle() {
        return title;
    }

    public String getUrl(int index) {
        return url[index];
    }

    public String[] getUrls(){
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(int index, String url) {
        this.url[index] = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
