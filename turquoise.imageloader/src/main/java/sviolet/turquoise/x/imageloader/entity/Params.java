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

package sviolet.turquoise.x.imageloader.entity;

/**
 *
 * Created by S.Violet on 2016/2/16.
 */
public class Params {

    public static final int MATCH_PARENT = -1;

    private Values values;

    private Params(Values values){
        this.values = values;
    }

    public int getReqWidth() {
        return values.reqWidth;
    }

    public int getReqHeight() {
        return values.reqHeight;
    }

    private static class Values{
        private int reqWidth = MATCH_PARENT;
        private int reqHeight = MATCH_PARENT;
    }

    public static class Builder{

        private Values values;

        public Builder(){
            values = new Values();
        }

        public Builder setReqWidth(int reqWidth){
            values.reqWidth = reqWidth;
            return this;
        }

        public Builder setReqHeight(int reqHeight){
            values.reqHeight = reqHeight;
            return this;
        }

        public Params build(){
            return new Params(values);
        }

    }

}
