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

package sviolet.turquoise.util.droid;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>CPU信息工具</p>
 *
 * Created by S.Violet on 2016/6/23.
 */
public class ProcessorUtils {

    /**
     * 获得CPU基本信息
     */
    public static CpuInfo getCpuInfo(){
        CpuInfo cpuInfo = new CpuInfo();
        List<String> info = readCpuInfo();
        for (String line : info){
            //计算逻辑核心数
            if (line.startsWith("processor")){
                cpuInfo.logicProcessors++;
            }
        }
        return cpuInfo;
    }

    /**
     * 获得当前CPU核心频率
     * @return 若核心离线则返回0
     */
    public static  List<String> getCurrentFrequency(){
        return getCurrentFrequency(getCpuInfo());
    }

    /**
     * 获得当前CPU核心频率
     * @param cpuInfo CPU基本信息
     * @return 若核心离线则返回0
     */
    public static  List<String> getCurrentFrequency(CpuInfo cpuInfo){
        return getCurrentFrequency(cpuInfo.logicProcessors);
    }

    /**
     * 获得当前CPU核心频率
     * @param processorNum CPU核心数
     * @return 若核心离线则返回0
     */
    public static  List<String> getCurrentFrequency(int processorNum){
        List<String> frequencyList = new ArrayList<>();
        for (int id = 0 ; id < processorNum ; id++){
            String frequency = readCurrentFrequency(id);
            if (frequency == null){
                frequency = "0";
            }
            frequencyList.add(frequency);
        }
        return frequencyList;
    }

    /**
     * 获得CPU核心最小频率
     * @return 若核心离线则返回0
     */
    public static  List<String> getMinFrequency(){
        return getMinFrequency(getCpuInfo());
    }

    /**
     * 获得CPU核心最小频率
     * @param cpuInfo CPU基本信息
     * @return 若核心离线则返回0
     */
    public static  List<String> getMinFrequency(CpuInfo cpuInfo){
        return getMinFrequency(cpuInfo.logicProcessors);
    }

    /**
     * 获得CPU核心最小频率
     * @param processorNum CPU核心数
     * @return 若核心离线则返回0
     */
    public static  List<String> getMinFrequency(int processorNum){
        List<String> frequencyList = new ArrayList<>();
        for (int id = 0 ; id < processorNum ; id++){
            String frequency = readMinFrequency(id);
            if (frequency == null){
                frequency = "0";
            }
            frequencyList.add(frequency);
        }
        return frequencyList;
    }

    /**
     * 获得CPU核心最大频率
     * @return 若核心离线则返回0
     */
    public static  List<String> getMaxFrequency(){
        return getMaxFrequency(getCpuInfo());
    }

    /**
     * 获得CPU核心最大频率
     * @param cpuInfo CPU基本信息
     * @return 若核心离线则返回0
     */
    public static  List<String> getMaxFrequency(CpuInfo cpuInfo){
        return getMaxFrequency(cpuInfo.logicProcessors);
    }

    /**
     * 获得CPU核心最大频率
     * @param processorNum CPU核心数
     * @return 若核心离线则返回0
     */
    public static  List<String> getMaxFrequency(int processorNum){
        List<String> frequencyList = new ArrayList<>();
        for (int id = 0 ; id < processorNum ; id++){
            String frequency = readMaxFrequency(id);
            if (frequency == null){
                frequency = "0";
            }
            frequencyList.add(frequency);
        }
        return frequencyList;
    }

    /**
     * 计算CPU使用率, 需要两个时间点, 计算两个时间点之间的平均使用率, 要求时间点1早于时间点2
     * @param cpuInfo CPU基本信息
     * @param usageInfo1 时间点1 (ProcessUtils.readUsageInfo())
     * @param usageInfo2 时间点2 (ProcessUtils.readUsageInfo())
     * @return CPU使用率
     */
    public static UsageRate getCpuUsageRate(CpuInfo cpuInfo, List<String> usageInfo1, List<String> usageInfo2){
        return getCpuUsageRate(cpuInfo, convertUsageInfo(usageInfo1), convertUsageInfo(usageInfo2));
    }

    /**
     * 计算CPU使用率, 需要两个时间点, 计算两个时间点之间的平均使用率, 要求时间点1早于时间点2
     * @param cpuInfo CPU基本信息
     * @param usageInfoMap1 时间点1 (ProcessUtils.getUsageInfo())
     * @param usageInfoMap2 时间点2 (ProcessUtils.getUsageInfo())
     * @return CPU使用率
     */
    public static UsageRate getCpuUsageRate(CpuInfo cpuInfo, Map<String, long[]> usageInfoMap1, Map<String, long[]> usageInfoMap2){
        UsageRate usageRate = new UsageRate();
        usageRate.processorUsages = new int[cpuInfo.logicProcessors];
        try {
            usageRate.totalUsage = calculateCpuUsage(usageInfoMap1.get("cpu"), usageInfoMap2.get("cpu"));
            for (int i = 0; i < cpuInfo.logicProcessors; i++) {
                usageRate.processorUsages[i] = calculateCpuUsage(usageInfoMap1.get("cpu" + i), usageInfoMap2.get("cpu" + i));
            }
        } catch (Exception ignore) {
        }
        return usageRate;
    }

    private static int calculateCpuUsage(long[] time1, long[] time2){
        if (time1 == null || time2 == null){
            return 0;
        }
        long totalTime1 = 0;
        for (long time : time1){
            totalTime1 += time;
        }
        long totalTime2 = 0;
        for (long time : time2){
            totalTime2 += time;
        }
        //防止分母为0
        if (totalTime2 - totalTime1 == 0){
            return 0;
        }
        return (int) (100 * (totalTime2 - totalTime1 - time2[3] + time1[3]) / (totalTime2 - totalTime1));
    }

    /**
     * 获得CPU使用时间信息(从开机以来的累计数据)
     */
    public static Map<String, long[]> getUsageInfo(){
        return convertUsageInfo(readUsageInfo());
    }

    private static Map<String, long[]> convertUsageInfo(List<String> usageInfo){
        Map<String, long[]> info = new HashMap<>();
        for (String line : usageInfo){
            String[] units = line.split("\\s+");
            if (units.length > 0){
                long[] time = new long[units.length - 1];
                for (int i = 0 ; i < units.length - 1 ; i++){
                    time[i] = Long.valueOf(units[i + 1]);
                }
                info.put(units[0], time);
            }
        }
        return info;
    }

    /************************************************************************************8
     * raw
     */

    public static List<String> readCpuInfo(){
        List<String> info = new ArrayList<>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader("/proc/cpuinfo"), 1024);
            String lineStr;
            while((lineStr = reader.readLine()) != null){
                info.add(lineStr);
            }
        }catch(IOException ignore){
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return info;
    }

    public static String readCurrentFrequency(int processorId){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + processorId + "/cpufreq/scaling_cur_freq"), 1024);
            return reader.readLine();
        }catch(IOException ignore){
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    public static String readMinFrequency(int processorId){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + processorId + "/cpufreq/cpuinfo_min_freq"), 1024);
            return reader.readLine();
        }catch(IOException ignore){
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    public static String readMaxFrequency(int processorId){
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + processorId + "/cpufreq/cpuinfo_max_freq"), 1024);
            return reader.readLine();
        }catch(IOException ignore){
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    public static List<String> readUsageInfo(){
        List<String> info = new ArrayList<>();
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader("/proc/stat"), 1024);
            String lineStr;
            while((lineStr = reader.readLine()) != null){
                info.add(lineStr);
            }
        }catch(IOException ignore){
        }finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return info;
    }

    /************************************************************************************8
     * classes
     */

    public static class CpuInfo{

        public int logicProcessors = 0;//逻辑核心数

    }

    public static class UsageRate {

        public int totalUsage = 0;
        public int[] processorUsages = {};

    }

}
