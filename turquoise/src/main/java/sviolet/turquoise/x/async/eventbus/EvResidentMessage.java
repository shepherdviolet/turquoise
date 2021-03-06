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

package sviolet.turquoise.x.async.eventbus;

/**
 * 常驻消息: 消息JavaBean接口, 标识接口.
 * 1.register/post模式中, 两种消息无差别.
 * 2.transmit模式中, 常驻消息在被移除(transmitRemove)前, 可以被重复读取, 且常驻消息会像病毒一样传递给所有
 * 新页面.
 *
 * Created by S.Violet on 2017/1/17.
 */
public interface EvResidentMessage extends EvMessage {


}
