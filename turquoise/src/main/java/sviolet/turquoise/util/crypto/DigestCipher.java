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

package sviolet.turquoise.util.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sviolet.turquoise.util.conversion.ByteUtils;
import sviolet.turquoise.util.droid.DeviceUtils;

/**
 * [国际算法]摘要工具
 */
public class DigestCipher {
	
	public static final String TYPE_MD5 = "MD5";
	public static final String TYPE_SHA1 = "SHA1";
	
	private static final String DEFAULT_ENCODING = "utf-8";
	
	/**
	 * 摘要byte[], 注意抛出异常
	 * 
	 * @param bytes bytes
	 * @param type 摘要算法
	 * @return 摘要bytes
	 */
	public static byte[] digest(byte[] bytes,String type) {
		if (bytes == null){
			throw new NullPointerException("[DigestCipher]digest: bytes is null");
		}
		try {
			MessageDigest cipher = MessageDigest.getInstance(type);
			return cipher.digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type, e);
		}
	}

	/**
	 * 摘要字符串(.getBytes("UTF-8")), 注意抛出异常
	 *
	 * @param str 字符串
	 * @param type 摘要算法
	 * @return 摘要bytes
     */
	public static byte[] digestStr(String str, String type){
		return digestStr(str, type, DEFAULT_ENCODING);
	}

	/**
	 * 摘要字符串(.getBytes(encoding)), 注意抛出异常
	 * 
	 * @param str bytes
	 * @param type 摘要算法
	 * @param encoding 编码方式
	 * @return 摘要bytes
	 */
	public static byte[] digestStr(String str, String type, String encoding){
		if (str == null){
			throw new NullPointerException("[DigestCipher]digestStr: str is null");
		}
		try {
			return digest(str.getBytes(encoding), type);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("[DigestCipher]Unsupported Encoding:" + encoding, e);
		}
	}

    /**
     * 摘要十六进制字符串(ByteUtils.hexToBytes(hexStr)), 注意抛出异常
     *
     * @param hexStr 十六进制字符串
     * @param type 摘要算法
     * @return 摘要bytes
     */
	public static byte[] digestHexStr(String hexStr, String type){
		if (hexStr == null){
			throw new NullPointerException("[DigestCipher]digestHexStr: hexStr is null");
		}
        return digest(ByteUtils.hexToBytes(hexStr), type);
	}

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

    /**
     * 摘要文件(NIO方式, 较快, 安卓API11以上使用, API10以下会很慢)
     *
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFileNio(File file, String type) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            FileChannel channel = inputStream.getChannel();
            MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest cipher = MessageDigest.getInstance(type);
            cipher.update(byteBuffer);
            return cipher.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type, e);
        } catch (IOException e) {
            throw e;
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 摘要文件(普通方式, 阻塞较慢, 安卓API10以下用这个方法)
     *
     * @param file 文件
     * @param type 摘要算法
     * @return 摘要bytes
     */
    public static byte[] digestFileIo(File file, String type) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            MessageDigest cipher = MessageDigest.getInstance(type);
            byte[] buff = new byte[1024];
            int size = -1;
            while((size = inputStream.read(buff)) != -1){
                cipher.update(buff, 0, size);
            }
            return cipher.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[DigestCipher]No Such Algorithm:" + type, e);
        } catch (IOException e) {
            throw e;
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}