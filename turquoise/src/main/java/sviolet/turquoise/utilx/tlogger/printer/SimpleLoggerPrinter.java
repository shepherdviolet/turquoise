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

package sviolet.turquoise.utilx.tlogger.printer;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import sviolet.thistle.util.conversion.StringUtils;
import sviolet.turquoise.util.common.DateTimeUtilsForAndroid;

/**
 * 日志磁盘输出简易实现
 *
 * TLogger.setLoggerPrinter(new SimpleLoggerPrinter(...));
 *
 * 注意:如果使用外部存储器, 需要申请权限
 *
 * <p>WARNING:: Need permission "android.permission.WRITE_EXTERNAL_STORAGE",
 * you should request runtime permission before TLogger.setLoggerPrinter(new SimpleLoggerPrinter(...))</p>
 *
 * Created by S.Violet on 2017/8/16.
 */
public class SimpleLoggerPrinter implements LoggerPrinter {

    private static final int MAX_QUEUE_SIZE = 1000;

    private LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private ThreadLocal<SimpleDateFormat> dateFormats = new ThreadLocal<>();

    private String datePattern;
    private Locale locale;
    private PrintWorker worker;

    private AtomicBoolean queueFull = new AtomicBoolean(false);
    private AtomicBoolean disabled = new AtomicBoolean(false);

    /**
     * @param logDirectory 日志输出路径(如果使用外部储存必须申请权限)
     * @param maxLogSizeMB 日志最大容量
     */
    public SimpleLoggerPrinter(@NonNull File logDirectory, int maxLogSizeMB) {
        this(logDirectory, maxLogSizeMB, false);
    }

    /**
     * @param logDirectory 日志输出路径(如果使用外部储存必须申请权限)
     * @param maxLogSizeMB 日志最大容量
     * @param sensitiveLogEnabled 是否输出敏感日志(默认false)
     */
    public SimpleLoggerPrinter(@NonNull File logDirectory, int maxLogSizeMB, boolean sensitiveLogEnabled) {
        this(logDirectory, maxLogSizeMB, "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault(), sensitiveLogEnabled);
    }

    /**
     * @param logDirectory 日志输出路径(如果使用外部储存必须申请权限)
     * @param maxLogSizeMB 日志最大容量
     * @param datePattern 日期格式
     * @param locale 地区
     * @param sensitiveLogEnabled 是否输出敏感日志(默认false)
     */
    public SimpleLoggerPrinter(@NonNull File logDirectory, int maxLogSizeMB, @NonNull String datePattern, @NonNull Locale locale, boolean sensitiveLogEnabled) {
        if (maxLogSizeMB <= 0){
            throw new IllegalArgumentException("maxLogSizeMB must > 0");
        }
        this.datePattern = datePattern;
        this.locale = locale;
        this.messageQueue.offer(getDateFormat().format(new Date()) + " --------------------------SimpleLoggerPrinter--------------------------\n");
        this.worker = new PrintWorker(messageQueue, queueFull, disabled, logDirectory, maxLogSizeMB, sensitiveLogEnabled);
        new Thread(this.worker).start();
    }

    @Override
    public void e(Object msg, Throwable throwable) {
        if (disabled.get()){
            return;
        }
        if (!messageQueue.offer(
                getDateFormat().format(new Date()) + " ERROR " + msg + (throwable != null ? "\n" + StringUtils.throwableToString(throwable) : "\n")
        )){
            Log.e("Turquoise", "[SimpleLoggerPrinter]message queue is full, log can not write to disk");
            queueFull.set(true);
        }
    }

    @Override
    public void w(Object msg, Throwable throwable) {
        if (disabled.get()){
            return;
        }
        if (!messageQueue.offer(
                getDateFormat().format(new Date()) + " WARNING " + msg + (throwable != null ? "\n" + StringUtils.throwableToString(throwable) : "\n")
        )){
            Log.e("Turquoise", "[SimpleLoggerPrinter]message queue is full, log can not write to disk");
            queueFull.set(true);
        }
    }

    @Override
    public void i(Object msg) {
        if (disabled.get()){
            return;
        }
        if (!messageQueue.offer(
                getDateFormat().format(new Date()) + " INFO " + msg + "\n"
        )){
            Log.e("Turquoise", "[SimpleLoggerPrinter]message queue is full, log can not write to disk");
            queueFull.set(true);
        }
    }

    @Override
    public void d(Object msg) {
        if (disabled.get()){
            return;
        }
        if (!messageQueue.offer(
                getDateFormat().format(new Date()) + " DEBUG " + msg + "\n"
        )){
            Log.e("Turquoise", "[SimpleLoggerPrinter]message queue is full, log can not write to disk");
            queueFull.set(true);
        }
    }

    @Override
    public void flush() {
        worker.flushBufferedWriter();
        Log.i("Turquoise", "[SimpleLoggerPrinter]flush by manual");
    }

    @Override
    public void close() {
        disabled.set(true);
        Log.i("Turquoise", "[SimpleLoggerPrinter]close");
    }

    @NonNull
    private SimpleDateFormat getDateFormat() {
        SimpleDateFormat dateFormat = dateFormats.get();
        if (dateFormat == null){
            dateFormat = new SimpleDateFormat(datePattern, locale);
            dateFormats.set(dateFormat);
        }
        return dateFormat;
    }

    private static class PrintWorker implements Runnable{

        private LinkedBlockingQueue<String> messageQueue;
        private AtomicBoolean queueFull;
        private AtomicBoolean disabled;
        private File logDirectory;
        private long maxLogSize;
        private boolean sensitiveLogEnabled;

        private File currentFile;
        private File previousFile;
        private BufferedWriter bufferedWriter;

        public PrintWorker(@NonNull LinkedBlockingQueue<String> messageQueue, @NonNull AtomicBoolean queueFull, @NonNull AtomicBoolean disabled, @NonNull File logDirectory, int maxLogSizeMB, boolean sensitiveLogEnabled) {
            this.messageQueue = messageQueue;
            this.queueFull = queueFull;
            this.disabled = disabled;
            this.logDirectory = logDirectory;
            this.maxLogSize = maxLogSizeMB * 1024 * 1024 / 2;
            this.sensitiveLogEnabled = sensitiveLogEnabled;
        }

        private void init(@NonNull AtomicBoolean disabled, @NonNull File logDirectory, boolean sensitiveLogEnabled) {
            if (sensitiveLogEnabled){
                Log.i("Turquoise", "[SimpleLoggerPrinter]init directory:" + logDirectory);
            }

            //创建路径
            if (!logDirectory.exists() || logDirectory.isFile()){
                if (!logDirectory.mkdirs()){
                    Log.e("Turquoise", "[SimpleLoggerPrinter]init failed, can not create log directory");
                    disabled.set(true);
                    return;
                }
            }

            //查找日志
            File[] logFiles = logDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".tlog");
                }
            });

            //清理多余日志
            if (logFiles.length > 2){
                Arrays.sort(logFiles);
                for (int i = 0 ; i < logFiles.length - 2 ; i++){
                    if (!logFiles[i].delete()){
                        Log.e("Turquoise", "[SimpleLoggerPrinter]trim log files failed, delete failed" + (sensitiveLogEnabled ? " (" + logFiles[i] + ")" : ""));
                    }
                }
            }

            //加载日志文件
            if (logFiles.length >= 2){
                currentFile = logFiles[logFiles.length - 1];
                previousFile = logFiles[logFiles.length - 2];
            } else if (logFiles.length == 1){
                currentFile = logFiles[0];
            }
        }

        private static final int FLUSH_TIMEOUT = 100;
        private static final int CLOSE_TIMEOUT = 20000;
        private static final int MAX_TIMEOUT = 300000;

        @Override
        public void run() {
            //初始化
            try {
                init(disabled, logDirectory, sensitiveLogEnabled);
            } catch (Throwable t) {
                disabled.set(true);
                if (sensitiveLogEnabled){
                    Log.e("Turquoise", "[SimpleLoggerPrinter]error while init, SimpleLoggerPrinter disabled", t);
                } else {
                    Log.e("Turquoise", "[SimpleLoggerPrinter]error while init, SimpleLoggerPrinter disabled");
                }
            }
            //循环
            int interruptTimes = 0;
            int timeout = FLUSH_TIMEOUT;
            while(!disabled.get()){
                try {
                    //从队列中获取消息
                    String message = messageQueue.poll(timeout, TimeUnit.MILLISECONDS);
                    if (message != null){
                        //打印消息
                        printMessage(message);
                        //打印队列满的错误日志
                        if (queueFull.get()){
                            queueFull.set(false);
                            printMessage("[SimpleLoggerPrinter]message queue is full, log can not write to disk");
                        }
                        //有新日志后, 将超时调整为极小的值. 当短暂地无日志后, 会及时将日志写入磁盘(flush)
                        timeout = FLUSH_TIMEOUT;
                    } else if (timeout == FLUSH_TIMEOUT){
                        //当短暂地无日志后, 会及时将日志写入磁盘(flush)
                        flushBufferedWriter();
                        //当写入磁盘后, 将超时调整为较大的值, 若仍然无日志, 会关闭Writer
                        timeout = CLOSE_TIMEOUT;
                    } else {
                        //长时间无日志时, 关闭writer
                        closeBufferedWriter();
                        //将超时调整为最大的值
                        timeout = MAX_TIMEOUT;
                    }
                    interruptTimes = 0;
                } catch (InterruptedException ignored) {
                    interruptTimes++;
                    if (interruptTimes == 10){
                        closeBufferedWriter();
                    }
                    try { Thread.sleep(100); } catch (InterruptedException ignored2) { }
                }
            }
            if (sensitiveLogEnabled) {
                Log.i("Turquoise", "[SimpleLoggerPrinter]disabled");
            }
        }

        private void printMessage(String message){
            try {
                BufferedWriter writer = getCurrentFileWriter();
                writer.write(message);
            } catch (Throwable t) {
                disabled.set(true);
                if (sensitiveLogEnabled){
                    Log.e("Turquoise", "[SimpleLoggerPrinter]error while writing log file, SimpleLoggerPrinter disabled", t);
                } else {
                    Log.e("Turquoise", "[SimpleLoggerPrinter]error while writing log file, SimpleLoggerPrinter disabled");
                }
            }
        }

        private BufferedWriter getCurrentFileWriter() throws IOException {
            //判断的当前文件是否存在
            if (currentFile == null || !currentFile.exists() || currentFile.isDirectory()){
                //新建文件
                closeBufferedWriter();
                currentFile = new File(logDirectory.getAbsolutePath() + "/" + DateTimeUtilsForAndroid.getCurrentTimeMillis() + ".tlog");
                bufferedWriter = new BufferedWriter(new FileWriter(currentFile, true));
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer create");
                }
            }
            //判断文件是否过大
            if (currentFile.length() > maxLogSize){
                //切换文件
                closeBufferedWriter();
                if (previousFile != null && previousFile.exists()){
                    if (!previousFile.delete()){
                        Log.e("Turquoise", "[SimpleLoggerPrinter]trim log file failed, delete failed" + (sensitiveLogEnabled ? " (" + previousFile + ")" : ""));
                    }
                }
                previousFile = currentFile;
                currentFile = new File(logDirectory.getAbsolutePath() + "/" + DateTimeUtilsForAndroid.getCurrentTimeMillis() + ".tlog");
                bufferedWriter = new BufferedWriter(new FileWriter(currentFile, true));
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer switch");
                }
            }
            //判断writer是否存在
            if (bufferedWriter == null){
                //创建writer
                bufferedWriter = new BufferedWriter(new FileWriter(currentFile, true));
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer recreate");
                }
            }
            return bufferedWriter;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            Log.i("Turquoise", "[SimpleLoggerPrinter]worker finalize");
            closeBufferedWriter();
        }

        private void flushBufferedWriter() {
            if (bufferedWriter != null){
                try { bufferedWriter.flush(); } catch (Throwable ignore){}
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer flush");
                }
            }
        }

        private void closeBufferedWriter() {
            if (bufferedWriter != null){
                try { bufferedWriter.flush(); bufferedWriter.close(); } catch (Throwable ignore){}
                bufferedWriter = null;
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer closed");
                }
            }
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
        Log.i("Turquoise", "[SimpleLoggerPrinter]finalize");
    }
}
