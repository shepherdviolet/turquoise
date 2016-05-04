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

package sviolet.turquoise.util.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sviolet.turquoise.util.conversion.ByteUtils;

/**
 * 数据类型判断工具
 *
 * Created by S.Violet on 2016/5/4.
 */
public class DataTypeUtils {

    private static final int MAX_HEADER_LENGTH = 20;//最大截取文件头长度
    private static final Map<String, Type> typesMapping = new HashMap<>();//映射表

    /**
     * 数据类型
     */
    public enum Type {
        JPG,
        PNG,
        GIF,
        BMP,
        TIF,
        DWG,
        PSD,
        RTF,
        XML,
        HTML,
        EMAIL,
        DOC,
        MDB,
        PS,
        PDF,
        ZIP,
        RAR,
        WAV,
        AVI,
        RM,
        MPG,
        MOV,
        ASF,
        MID,
        GZ,
        UNKNOWN,//未知类型
        NULL//文件或字节流为空
    }

    /**
     * 填充文件头-文件类型映射表
     */
    static {
        typesMapping.put("FFD8FF", Type.JPG);
        typesMapping.put("89504E47", Type.PNG);
        typesMapping.put("47494638", Type.GIF);
        typesMapping.put("49492A00", Type.TIF);
        typesMapping.put("424D", Type.BMP);
        typesMapping.put("41433130", Type.DWG);
        typesMapping.put("38425053", Type.PSD);
        typesMapping.put("7B5C727466", Type.RTF);
        typesMapping.put("3C3F786D6C", Type.XML);
        typesMapping.put("68746D6C3E", Type.HTML);
        typesMapping.put("44656C69766572792D646174653A", Type.EMAIL);
        typesMapping.put("D0CF11E0", Type.DOC);
        typesMapping.put("5374616E64617264204A", Type.MDB);
        typesMapping.put("252150532D41646F6265", Type.PS);
        typesMapping.put("255044462D312E", Type.PDF);
        typesMapping.put("504B0304", Type.ZIP);
        typesMapping.put("52617221", Type.RAR);
        typesMapping.put("57415645", Type.WAV);
        typesMapping.put("41564920", Type.AVI);
        typesMapping.put("2E524D46", Type.RM);
        typesMapping.put("000001BA", Type.MPG);
        typesMapping.put("000001B3", Type.MPG);
        typesMapping.put("6D6F6F76", Type.MOV);
        typesMapping.put("3026B2758E66CF11", Type.ASF);
        typesMapping.put("4D546864", Type.MID);
        typesMapping.put("1F8B08", Type.GZ);
    }

    /**
     * 判断文件数据类型
     * @param file 文件
     * @return 类型
     */
    public static Type getFileType(File file){
        if (file == null || !file.exists()){
            return Type.NULL;
        }
        String fileHeader = getFileHeader(file);
        if (fileHeader == null){
            return Type.NULL;
        }
        for (Map.Entry<String, Type> entry : typesMapping.entrySet()){
            if (fileHeader.startsWith(entry.getKey())){
                return entry.getValue();
            }
        }
        return Type.UNKNOWN;
    }

    private static String getFileHeader(File file){
        FileInputStream inputStream = null;
        try{
            long fileLength = file.length();
            if (fileLength <= 0){
                return null;
            }
            int headerLength;
            if (fileLength > MAX_HEADER_LENGTH){
                headerLength = MAX_HEADER_LENGTH;
            }else{
                headerLength = (int) fileLength;
            }
            byte[] buffer = new byte[headerLength];
            inputStream = new FileInputStream(file);
            inputStream.read(buffer);
            return ByteUtils.bytesToHex(buffer, true);
        }catch(Exception ignored){
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * 判断bytes数据类型
     * @param bytes bytes
     * @return 类型
     */
    public static Type getBytesType(byte[] bytes){
        String header = getBytesHeader(bytes);
        if (header == null){
            return Type.NULL;
        }
        for (Map.Entry<String, Type> entry : typesMapping.entrySet()){
            if (header.startsWith(entry.getKey())){
                return entry.getValue();
            }
        }
        return Type.UNKNOWN;
    }

    private static String getBytesHeader(byte[] bytes){
        if (bytes == null || bytes.length <= 0){
            return null;
        }
        int headerLength;
        if (bytes.length > MAX_HEADER_LENGTH){
            headerLength = MAX_HEADER_LENGTH;
        }else{
            headerLength = bytes.length;
        }
        byte[] buffer = new byte[headerLength];
        System.arraycopy(bytes, 0, buffer, 0, headerLength);
        return ByteUtils.bytesToHex(buffer, true);
    }

}
