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
 */

package sviolet.turquoise.exception;

/**
 * [异常]弃用异常<Br/>
 * 当调用一个被弃用的方法或类时,抛出该异常
 *
 * Created by S.Violet on 2015/6/30.
 */
public class DeprecatedException extends RuntimeException {

    public DeprecatedException(String msg){
        super(msg);
    }

    public DeprecatedException(String msg, Throwable throwable){
        super(msg, throwable);
    }

}
