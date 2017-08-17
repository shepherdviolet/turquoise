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
 * 注意:如果使用外部存储器, 需要申请权限
 *
 * Created by S.Violet on 2017/8/16.
 */
public class SimpleLoggerPrinter implements LoggerPrinter {

    private static final int MAX_QUEUE_SIZE = 100;

    private LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
    private ThreadLocal<SimpleDateFormat> dateFormats = new ThreadLocal<>();

    private String datePattern;
    private Locale locale;

    private AtomicBoolean queueFull = new AtomicBoolean(false);
    private AtomicBoolean disabled = new AtomicBoolean(false);

    public SimpleLoggerPrinter(@NonNull File logDirectory, int maxLogSizeMB, boolean sensitiveLogEnabled) {
        this(logDirectory, maxLogSizeMB, "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault(), false);
    }

    public SimpleLoggerPrinter(@NonNull File logDirectory, int maxLogSizeMB, @NonNull String datePattern, @NonNull Locale locale, boolean sensitiveLogEnabled) {
        if (maxLogSizeMB <= 0){
            throw new IllegalArgumentException("maxLogSizeMB must > 0");
        }
        this.datePattern = datePattern;
        this.locale = locale;
        new Thread(new PrintWorker(messageQueue, queueFull, disabled, logDirectory, maxLogSizeMB, sensitiveLogEnabled)).start();
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
        }

        private void init(@NonNull AtomicBoolean disabled, @NonNull File logDirectory, boolean sensitiveLogEnabled) {
            if (sensitiveLogEnabled){
                Log.i("Turquoise", "[SimpleLoggerPrinter]directory:" + logDirectory);
            }

            if (!logDirectory.exists() || logDirectory.isFile()){
                if (!logDirectory.mkdirs()){
                    Log.e("Turquoise", "[SimpleLoggerPrinter]init failed, can not create log directory");
                    disabled.set(true);
                    return;
                }
            }

            File[] logFiles = logDirectory.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isFile() && file.getName().endsWith(".tlog");
                }
            });

            if (logFiles.length > 2){
                Arrays.sort(logFiles);
                for (int i = 0 ; i < logFiles.length - 2 ; i++){
                    if (!logFiles[i].delete()){
                        Log.e("Turquoise", "[SimpleLoggerPrinter]trim log files failed, delete failed" + (sensitiveLogEnabled ? " (" + logFiles[i] + ")" : ""));
                    }
                }
            }

            if (logFiles.length >= 2){
                currentFile = logFiles[logFiles.length - 1];
                previousFile = logFiles[logFiles.length - 2];
            } else if (logFiles.length == 1){
                currentFile = logFiles[0];
            }
        }

        @Override
        public void run() {
            while(!disabled.get()){
                try {
                    String message = messageQueue.poll(60, TimeUnit.SECONDS);
                    if (message != null){
                        printMessage(message);
                        if (queueFull.get()){
                            queueFull.set(false);
                            printMessage("[SimpleLoggerPrinter]message queue is full, log can not write to disk");
                        }
                        //keep writer
                        continue;
                    }
                } catch (InterruptedException ignored) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored2) {
                    }
                }
                //no message, close writer
                closeBufferedWriter();
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
            if (currentFile == null || !currentFile.exists() || currentFile.isDirectory()){
                closeBufferedWriter();
                currentFile = new File(logDirectory.getAbsolutePath() + "/" + DateTimeUtilsForAndroid.getCurrentTimeMillis() + ".tlog");
                bufferedWriter = new BufferedWriter(new FileWriter(currentFile));
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer create");
                }
            }
            if (currentFile.length() > maxLogSize){
                closeBufferedWriter();
                if (previousFile != null && previousFile.exists()){
                    if (!previousFile.delete()){
                        Log.e("Turquoise", "[SimpleLoggerPrinter]trim log file failed, delete failed" + (sensitiveLogEnabled ? " (" + previousFile + ")" : ""));
                    }
                }
                previousFile = currentFile;
                currentFile = new File(logDirectory.getAbsolutePath() + "/" + DateTimeUtilsForAndroid.getCurrentTimeMillis() + ".tlog");
                bufferedWriter = new BufferedWriter(new FileWriter(currentFile));
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer switch");
                }
            }
            if (bufferedWriter == null){
                bufferedWriter = new BufferedWriter(new FileWriter(currentFile));
                if (sensitiveLogEnabled) {
                    Log.i("Turquoise", "[SimpleLoggerPrinter]writer recreate");
                }
            }
            return bufferedWriter;
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            closeBufferedWriter();
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
        disabled.set(true);
    }
}
