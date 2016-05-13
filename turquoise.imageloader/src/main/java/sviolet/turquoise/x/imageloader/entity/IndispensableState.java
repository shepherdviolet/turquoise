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
 * TODO
 * <p>Indispensable task will keep loading as far as possible, even if the speed is very slow,
 * it has double connection-timeout & read-timeout, and ignore AbortOnLowNetworkSpeed check.</p>
 *
 * <p>False by default.</p>
 *
 * Created by S.Violet on 2016/5/13.
 */
public class IndispensableState {

    private boolean indispensable = false;

    public boolean isIndispensable(){
        return indispensable;
    }

    public void setIndispensable(){
        indispensable = true;
    }

}
