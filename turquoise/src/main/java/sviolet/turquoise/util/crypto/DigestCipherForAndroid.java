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

package sviolet.turquoise.util.crypto;

import java.io.File;
import java.io.IOException;

import sviolet.thistle.util.crypto.DigestCipher;
import sviolet.turquoise.util.droid.DeviceUtils;

/**
 * [国际算法]摘要工具
 *
 * Created by S.Violet on 2017/7/31.
 */
public class DigestCipherForAndroid extends DigestCipher {

    /**
     * 摘要文件(根据安卓API版本选择NIO或IO方式)
     *
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFile(File file, String type) throws IOException {
        if(DeviceUtils.getVersionSDK() < 11)
            return digestFileIo(file, type);//API10使用普通IO(NIO很慢)
        else
            return digestFileNio(file, type);//API11以上使用NIO,效率高
    }

}
