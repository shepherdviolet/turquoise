package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;
import sviolet.thistle.util.judge.CheckUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.handler.ExceptionHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.disk.DiskCacheServer;

/**
 * <p>Multi-thread network loading logic</p>
 *
 * @author S.Violet
 */
public abstract class MultiThreadNetworkLoadHandler implements NetworkLoadHandler {

    private static final int MAXIMUM_REDIRECT_TIMES = 5;
    private static final int READ_BUFF_LENGTH = 8 * 1024;

    private Map<String, OkHttpClient> okHttpClients = new ConcurrentHashMap<>(2);
    private ExecutorService workThreadPool = ThreadPoolExecutorUtils.createCached(0, Integer.MAX_VALUE, 60L, "MultiThreadNetworkLoadHandler-worker-%d");

    private Map<String, String> headers;
    private long minBlockSize = 32L * 1024L;
    private int maxBlockNum = 4;

    protected OkHttpClient getClient(long connectTimeout, long readTimeout){
        OkHttpClient client = okHttpClients.get(connectTimeout + "+" + readTimeout);
        if (client == null) {
            synchronized (this) {
                client = okHttpClients.get(connectTimeout + "+" + readTimeout);
                if (client == null) {
                    client = genClient(connectTimeout, readTimeout);
                    okHttpClients.put(connectTimeout + "+" + readTimeout, client);
                }
            }
        }
        return client;
    }

    protected OkHttpClient genClient(long connectTimeout, long readTimeout){
        return new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * add header of http
     * @param key header key
     * @param value header value
     */
    public MultiThreadNetworkLoadHandler addHeader(String key, String value){
        if (headers == null){
            headers = new HashMap<>(0);
        }
        headers.put(key, value);
        return this;
    }

    /******************************************************************************************
     * logic
     */

    @Override
    public final HandleResult onHandle(
            Context applicationContext,
            Context context,
            DiskCacheServer.WriterProvider writerProvider,
            Task.Info taskInfo,
            IndispensableState indispensableState,
            LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig,
            long connectTimeout,
            long readTimeout,
            long imageDataLengthLimit,
            ExceptionHandler exceptionHandler,
            TLogger logger) {

        //reset progress
        taskInfo.getLoadProgress().reset();

        //loading
        Response response = null;
        List<Response> responseList = new ArrayList<>(maxBlockNum);
        List<long[]> offsetList = new ArrayList<>(maxBlockNum);

        try {
            //connect first
            response = connect(null, taskInfo.getUrl(), 0, 0, minBlockSize - 1, connectTimeout, readTimeout, taskInfo, logger);
            //check content range
            ContentRange contentRange = parseContentRange(response, taskInfo, logger);
            if (contentRange.parseError) {
                close(response);
                response = connect(null, taskInfo.getUrl(), 0, -1, -1, connectTimeout, readTimeout, taskInfo, logger);
            }

            //cancel loading if image data out of limit
            if (contentRange.totalSize > imageDataLengthLimit) {
                exceptionHandler.onImageDataLengthOutOfLimitException(applicationContext, context,
                        taskInfo, taskInfo.getLoadProgress().total(), imageDataLengthLimit, logger);
                return NetworkLoadHandler.HandleResult.CANCELED;
            }

            //add to list
            responseList.add(response);
            offsetList.add(new long[]{contentRange.startPosition, contentRange.endPosition});

            //multi-connect if we can
            if (contentRange.acceptRanges && contentRange.endPosition < contentRange.totalSize - 1) {
                //calculate block size
                long remainSize = contentRange.totalSize - contentRange.endPosition - 1;
                long blockSize;
                if (remainSize <= minBlockSize * (maxBlockNum - 1)) {
                    blockSize = minBlockSize;
                } else {
                    blockSize = (int)((double)remainSize / (double)(maxBlockNum - 1) + 1);
                }
                //connect others
                long start = minBlockSize;
                long end;
                ContentRange subContentRange;
                while(start <= contentRange.totalSize - 1) {
                    end = start + blockSize - 1;
                    if (end > contentRange.totalSize - 1){
                        end = contentRange.totalSize - 1;
                    }
                    //connect
                    response = connect(null, taskInfo.getUrl(), 0, start, end, connectTimeout, readTimeout, taskInfo, logger);
                    //ERROR CHECK: check content range again
                    subContentRange = parseContentRange(response, taskInfo, logger);
                    if (!subContentRange.acceptRanges || subContentRange.startPosition != start || subContentRange.endPosition != end) {
                        if (logger.checkEnable(TLogger.ERROR)) {
                            logger.e("[MultiThreadNetworkLoadHandler]CAUTION!!! Http ranges is not accepted, first connection is accepted, but the others did not, bad content range:[" + subContentRange + "], task:" + taskInfo);
                        }
                        exceptionHandler.onHttpContentRangeParseException(applicationContext,context, taskInfo, logger);
                        //close all connections
                        response.close();
                        for (Response responseItem : responseList) {
                            close(responseItem);
                        }
                        //reconnect
                        response = connect(null, taskInfo.getUrl(), 0, -1, -1, connectTimeout, readTimeout, taskInfo, logger);
                        responseList.add(response);
                        break;
                    }
                    //add to list
                    responseList.add(response);
                    offsetList.add(new long[]{start, end});
                    start = start + blockSize;
                }
            }

            //set file size
            RandomAccessFile randomAccessFile = writerProvider.newRandomAccessFileForWrite();
            randomAccessFile.setLength(contentRange.totalSize);
            randomAccessFile.close();

            taskInfo.getLoadProgress().setTotal(contentRange.totalSize);

            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Network loading start, connections:" + responseList.size() + ", task:" + taskInfo);
            }

            //监控线程通知工作线程停止
            AtomicBoolean stopSignal = new AtomicBoolean(false);
            //工作线程完成计数
            AtomicInteger finishSignal = new AtomicInteger(0);
            //工作线程反馈异常
            AtomicReference<Throwable> exceptionSignal = new AtomicReference<>(null);
            for (int i = 0 ; i < responseList.size() ; i++) {
                read(responseList.get(i), offsetList.get(i), stopSignal, finishSignal, exceptionSignal, writerProvider, taskInfo);
            }

            //dead line, fallback
            long startTime = System.currentTimeMillis();
            long deadLine = System.currentTimeMillis() + lowNetworkSpeedConfig.getDeadline() * 2;

            while (true) {
                try {
                    synchronized (stopSignal) {
                        stopSignal.wait(500L);
                    }
                } catch (Exception ignore){
                }
                //exception
                if (exceptionSignal.get() != null){
                    stopSignal.set(true);
                    throw exceptionSignal.get();
                }
                //succeed
                if (finishSignal.get() >= responseList.size()) {
                    stopSignal.set(true);
                    break;
                }
                if (!checkNetworkSpeed(applicationContext, context, startTime, taskInfo, lowNetworkSpeedConfig, exceptionHandler, logger)) {
                    stopSignal.set(true);
                    return HandleResult.CANCELED;
                }
                //deadline
                if (System.currentTimeMillis() > deadLine) {
                    stopSignal.set(true);
                    if (logger.checkEnable(TLogger.ERROR)) {
                        logger.e("[MultiThreadNetworkLoadHandler]Watcher thread reach dead line, force stop all connections");
                    }
                    return HandleResult.CANCELED;
                }
            }

            return HandleResult.SUCCEED;

        } catch (NetworkException e) {
            exceptionHandler.onNetworkLoadException(
                    applicationContext, context, taskInfo, e.getCause(), logger);
        } catch (Throwable e) {
            exceptionHandler.onDiskCacheWriteException(
                    applicationContext, context, taskInfo, e, logger);
        } finally {
            close(response);
            for (Response responseItem : responseList) {
                close(responseItem);
            }
        }

        return HandleResult.FAILED;

    }

    private Response connect(String prevUrl, String url, int redirectTimes, long start, long end, long connectTimeout, long readTimeout, Task.Info taskInfo, TLogger logger) throws NetworkException {
        Response response = null;
        try {
            //skip when redirect too many times
            if (redirectTimes >= MAXIMUM_REDIRECT_TIMES) {
                throw new NetworkException("Redirect times > maximum(" + MAXIMUM_REDIRECT_TIMES + ")");
            } else {
                if (prevUrl != null && prevUrl.equals(url)) {
                    throw new NetworkException("The url redirect to itself, url:" + url);
                }
            }
            //build request
            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .get();
            //add headers
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    requestBuilder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            //set range
            if (start >= 0) {
                requestBuilder.addHeader("Range", "bytes=" + start + "-" + end);
            }
            //connect
            response = getClient(connectTimeout, readTimeout).newCall(requestBuilder.build()).execute();
            //check redirect
            if (response.isRedirect()) {
                String redirectUrl = response.header("Location");
                close(response);
                return connect(url, redirectUrl, redirectTimes + 1, start, end, connectTimeout, readTimeout, taskInfo, logger);
            }
            //check success
            if (!response.isSuccessful()) {
                throw new NetworkException("Http reject, code:" + response.code());
            }
            //check body
            if (response.body() == null) {
                throw new NetworkException("No response body");
            }
            return response;
        } catch (Throwable t) {
            close(response);
            if (t instanceof NetworkException) {
                throw (NetworkException)t;
            }
            throw new NetworkException(t);
        }
    }

    private ContentRange parseContentRange(Response response, Task.Info taskInfo, TLogger logger) {
        String contentRangeOrigin = response.header("Content-Range");
        if (CheckUtils.isEmptyOrBlank(contentRangeOrigin)){
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, task:" + taskInfo);
            }
            return new ContentRange(false, false, response.body().contentLength() - 1, 0, 0);
        }
        if (contentRangeOrigin.length() < 10
                || !contentRangeOrigin.startsWith("bytes ")) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
        }

        String contentRangeString = contentRangeOrigin.substring(6);
        long startPosition;
        long endPosition;
        long totalSize;

        if (contentRangeString.startsWith("*/")){
            startPosition = 0;
            endPosition = response.body().contentLength() - 1;
            try {
                totalSize = Long.parseLong(contentRangeString.substring(2));
            } catch (Exception e) {
                if (logger.checkEnable(TLogger.DEBUG)) {
                    logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
                }
                return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
            }
            return new ContentRange(true, true, totalSize, startPosition, endPosition);
        }

        int index1 = contentRangeString.indexOf("-");
        if (index1 < 0) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
        } else if (index1 == 0) {
            startPosition = 0;
        } else {
            try {
                startPosition = Long.parseLong(contentRangeString.substring(0, index1));
            } catch (Exception e) {
                if (logger.checkEnable(TLogger.DEBUG)) {
                    logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
                }
                return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
            }
        }

        int index2 = contentRangeString.indexOf("/");
        if (index2 <= index1 || index2 == contentRangeString.length() - 1) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
        } else if (index2 == index1 + 1) {
            endPosition = response.body().contentLength() - 1;
        } else {
            try {
                endPosition = Long.parseLong(contentRangeString.substring(index1 + 1, index2));
            } catch (Exception e) {
                if (logger.checkEnable(TLogger.DEBUG)) {
                    logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
                }
                return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
            }
        }

        if (startPosition > endPosition) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
        }

        try {
            totalSize = Long.parseLong(contentRangeString.substring(index2 + 1));
        } catch (Exception e) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
        }

        if (endPosition > totalSize - 1){
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength() - 1, 0, 0);
        }

        return new ContentRange(false, true, totalSize, startPosition, endPosition);
    }

    private void read(final Response response, final long[] offset, final AtomicBoolean stopSignal, final AtomicInteger finishSignal, final AtomicReference<Throwable> exceptionSignal, final DiskCacheServer.WriterProvider writerProvider, final Task.Info taskInfo){
        workThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                RandomAccessFile randomAccessFile = null;
                try {
                    InputStream inputStream = response.body().byteStream();
                    randomAccessFile = writerProvider.newRandomAccessFileForWrite();

                    long start = offset[0];
                    long end = offset[1];
                    int readLength;
                    byte[] buff = new byte[READ_BUFF_LENGTH];

                    while (!stopSignal.get() && start <= end) {

                        //read from inputStream
                        try {
                            readLength = inputStream.read(buff);
                        } catch (Exception e) {
                            throw new NetworkException(e);
                        }
                        if (readLength < 0) {
                            break;
                        }
                        if (readLength == 0){
                            continue;
                        }

                        //calculate write length
                        int writeLength = readLength;
                        if (writeLength > end - start + 1){
                            writeLength = (int) (end - start + 1);
                        }

                        //record progress
                        taskInfo.getLoadProgress().increaseLoaded(writeLength);

                        //write to disk
                        randomAccessFile.seek(start);
                        randomAccessFile.write(buff, 0, writeLength);
                        start += readLength;
                    }

                    //finish
                    finishSignal.incrementAndGet();
                    synchronized (stopSignal) {
                        stopSignal.notifyAll();
                    }

                } catch (Throwable t) {
                    exceptionSignal.set(t);
                    synchronized (stopSignal) {
                        stopSignal.notifyAll();
                    }
                } finally {
                    close(response);
                    close(randomAccessFile);
                }
            }
        });
    }

    /**
     * Check network speed by LowNetworkSpeedStrategy
     */
    private boolean checkNetworkSpeed(
            Context applicationContext,
            Context context,
            long startTime,
            Task.Info taskInfo,
            LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig,
            ExceptionHandler exceptionHandler,
            TLogger logger){

        final long elapseTime = System.currentTimeMillis() - startTime + 1;
        final long loadedData = taskInfo.getLoadProgress().loaded();
        final long totalData = taskInfo.getLoadProgress().total();

        final long deadline = lowNetworkSpeedConfig.getDeadline();
        final long windowPeriod = lowNetworkSpeedConfig.getWindowPeriod();
        final int thresholdSpeed = lowNetworkSpeedConfig.getThresholdSpeed();

        //dead line
        if (elapseTime > deadline){
            int speed = (int) ((float)loadedData / (elapseTime >> 10));
            exceptionHandler.handleLowNetworkSpeedEvent(applicationContext, context,
                    taskInfo, elapseTime, speed, logger);
            return false;
        }

        //if in window period, skip speed check
        if (elapseTime < windowPeriod){
            return true;
        }

        //check speed
        int speed = (int) ((float)loadedData / (elapseTime >> 10));
        //check by thresholdSpeed
        if (speed > thresholdSpeed) {
            //check by progress
            if (totalData > 0) {
                //calculate min speed by total data length
                int minSpeed = (int)((float)totalData / (deadline >> 10));
                //80% off
                if (speed > minSpeed * 0.8f){
                    return true;
                }
            } else {
                return true;
            }
        }

        exceptionHandler.handleLowNetworkSpeedEvent(applicationContext, context,
                taskInfo, elapseTime, speed, logger);
        return false;
    }

    private void close(Response response) {
        if (response == null){
            return;
        }
        try {
            response.close();
        } catch (Throwable ignore){
        }
    }

    private void close(RandomAccessFile randomAccessFile) {
        if (randomAccessFile != null){
            try {
                randomAccessFile.close();
            } catch (Throwable ignore) {
            }
        }
    }

    private static class ContentRange {
        private boolean parseError = false;
        private boolean acceptRanges = false;
        private long totalSize;
        private long startPosition;
        private long endPosition;

        public ContentRange(boolean parseError, boolean acceptRanges, long totalSize, long startPosition, long endPosition) {
            this.parseError = parseError;
            this.acceptRanges = acceptRanges;
            this.totalSize = totalSize;
            this.startPosition = startPosition;
            this.endPosition = endPosition;
        }

        @Override
        public String toString() {
            return "parseError:" + parseError + ", acceptRanges:" + acceptRanges + ", totalSize:" + totalSize + ", startPosition:" + startPosition + ", endPosition:" + endPosition;
        }
    }

    public static class NetworkException extends Exception{

        public NetworkException(String message) {
            super(message);
        }

        public NetworkException(String message, Throwable cause) {
            super(message, cause);
        }

        public NetworkException(Throwable cause) {
            super(cause);
        }
    }

}
