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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Byte处理工具
 * 
 * @author S.Violet
 */

public class ByteUtils {

	private static final String HEX_STRING_MAPPING = "0123456789ABCDEF";

	/**
	 * 把两个byte[]前后拼接成一个byte[]
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static byte[] joint(byte[] left, byte[] right){
		byte[] result = new byte[left.length + right.length];
		System.arraycopy(left, 0, result, 0, left.length);
		System.arraycopy(right, 0, result, left.length, right.length);
		return result;
	}

	/**
	 * bytes转为hexString
	 * @param bytes bytes
	 * @return hex string
	 */
	public static String bytesToHex(byte[] bytes){
		if (bytes == null || bytes.length <= 0)
			return "";
		StringBuilder stringBuilder = new StringBuilder("");
        for (byte unit : bytes) {
            int unitInt = unit & 0xFF;
            String unitHex = Integer.toHexString(unitInt);
            if (unitHex.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(unitHex);
        }
		return stringBuilder.toString();
	}

	/**
	 * hexString转为bytes
	 * @param hexString hexString
	 * @return bytes
	 */
	public static byte[] hexToBytes(String hexString) {
		if (hexString == null || hexString.length() <= 0) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			int step = i * 2;
			result[i] = (byte) (charToByte(hexChars[step]) << 4 | charToByte(hexChars[step + 1]));
		}
		return result;
	}

	private static byte charToByte(char c) {
		return (byte) HEX_STRING_MAPPING.indexOf(c);
	}

	/**
	 * 对象转数组
	 * @param obj
	 * @return
	 */
	public static byte[] objectToByte (Object obj) {
		byte[] bytes = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			bytes = bos.toByteArray ();
			oos.close();
			bos.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return bytes;
	}

	/**
	 * 数组转对象
	 * @param bytes
	 * @return
	 */
	public static Object byteToObject (byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream (bytes);
			ObjectInputStream ois = new ObjectInputStream (bis);
			obj = ois.readObject();
			ois.close();
			bis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return obj;
	}

}
