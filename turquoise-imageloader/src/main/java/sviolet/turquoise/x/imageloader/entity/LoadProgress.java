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

package sviolet.turquoise.x.imageloader.entity;

/**
 * <p>Loading progress, progress of network loading</p>
 *
 * Created by S.Violet on 2016/5/5.
 */
public class LoadProgress {

    private Info info = new Info();

    public void setLoaded(long loaded){
        info.loaded = loaded;
    }

    public void increaseLoaded(long increaseCount){
        info.loaded += increaseCount;
    }

    public void setTotal(long total){
        info.total = total;
    }

    public LoadProgress reset(){
        setLoaded(Info.UNKNOWN);
        setTotal(Info.UNKNOWN);
        return this;
    }

    /**
     * @return loaded data count (network load), return {@value Info#UNKNOWN} before network load
     */
    public long loaded(){
        return info.loaded;
    }

    /**
     * @return total count of source data, return {@value Info#UNKNOWN} if can't get from http header
     */
    public long total(){
        return info.total;
    }

    public Info getInfo(){
        return info;
    }

    /*****************************************************************************************
     * inner
     */

    public static class Info {

        public static final long UNKNOWN = -1;

        private long loaded = UNKNOWN;
        private long total = UNKNOWN;

        private Info(){

        }

        /**
         * @return loaded data count (network load), return {@value UNKNOWN} before network load
         */
        public long loaded(){
            return loaded;
        }

        /**
         * @return total count of source data, return {@value UNKNOWN} if can't get from http header
         */
        public long total(){
            return total;
        }

    }

}
