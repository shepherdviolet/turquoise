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
public class MultiThreadNetworkLoadHandler implements NetworkLoadHandler {

    private static final int MAXIMUM_REDIRECT_TIMES = 5;
    private static final int READ_BUFF_LENGTH = 8 * 1024;
    private static final long SPLIT_THRESHOLD = 16L * 1024L;

    private Map<String, OkHttpClient> okHttpClients = new ConcurrentHashMap<>(2);
    private ExecutorService workThreadPool = ThreadPoolExecutorUtils.createCached(0, Integer.MAX_VALUE, 60L, "MultiThreadNetworkLoadHandler-worker-%d");

    private Map<String, String> headers;

    private int maxBlockNum;
    private long probeBlockSize;
    private long standardNetworkSpeed;
    private boolean verboseLog;

    public MultiThreadNetworkLoadHandler() {
        this(4, false);
    }

    /**
     * @param maxBlockNum max thread num to load one image, must >= 2
     * @param verboseLog print verbose log if true
     */
    public MultiThreadNetworkLoadHandler(int maxBlockNum, boolean verboseLog) {
        this(maxBlockNum, 100L * 1024L * 1024L, 512L, verboseLog);
    }

    /**
     * @param maxBlockNum max thread num to load one image, must >= 2
     * @param probeBlockSize bytes, ranges of first connection, must >= {@value SPLIT_THRESHOLD}
     * @param standardNetworkSpeed KB/s, This network speed is used to evaluate the number of threads needed, must >= 16
     * @param verboseLog print verbose log if true
     */
    public MultiThreadNetworkLoadHandler(int maxBlockNum, long probeBlockSize, long standardNetworkSpeed, boolean verboseLog) {
        if (maxBlockNum < 2) {
            throw new IllegalArgumentException("maxBlockNum must >= 2");
        }
        if (probeBlockSize < SPLIT_THRESHOLD) {
            throw new IllegalArgumentException("probeBlockSize must >= " + SPLIT_THRESHOLD);
        }
        if (standardNetworkSpeed < 16) {
            throw new IllegalArgumentException("standardNetworkSpeed must >= 16");
        }
        this.maxBlockNum = maxBlockNum;
        this.probeBlockSize = probeBlockSize;
        this.standardNetworkSpeed = standardNetworkSpeed;
        this.verboseLog = verboseLog;
    }

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

    /**
     * Multi-thread load logic
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

            long connectStartTime = System.currentTimeMillis();

            //connect first
            response = connect(null, taskInfo.getUrl(), 0, 0, probeBlockSize - 1, connectTimeout, readTimeout, taskInfo, logger);
            //check content range
            ContentRange contentRange = parseContentRange(response, taskInfo, logger);
            if (contentRange.parseError) {
                //parse error, reconnect without ranges
                close(response);
                response = connect(null, taskInfo.getUrl(), 0, -1, -1, connectTimeout, readTimeout, taskInfo, logger);
                //reset content range
                contentRange.acceptRanges = false;
                contentRange.totalSize = response.body().contentLength();
                contentRange.startPosition = 0;
                contentRange.endPosition = contentRange.totalSize - 1;
            }

            //cancel loading if image data out of limit
            if (contentRange.totalSize > imageDataLengthLimit || contentRange.totalSize <= 0) {
                exceptionHandler.onImageDataLengthOutOfLimitException(applicationContext, context,
                        taskInfo, taskInfo.getLoadProgress().total(), imageDataLengthLimit, logger);
                return NetworkLoadHandler.HandleResult.CANCELED;
            }

            //multi-connect if we can
            if (!contentRange.acceptRanges || contentRange.totalSize <= SPLIT_THRESHOLD) {
                //one connection
                //add first connect to list
                responseList.add(response);
                offsetList.add(new long[]{0, contentRange.totalSize - 1});
            } else {
                //multi connection
                long firstConnectElapse = System.currentTimeMillis() - connectStartTime;
                //calculate block num
                int optimalBlockNum = maxBlockNum;
                int minBlockNum = contentRange.endPosition == contentRange.totalSize - 1 ? 1 : 2;
                if (contentRange.totalSize < (contentRange.endPosition + 1) * maxBlockNum) {
                    if (verboseLog && logger.checkEnable(TLogger.DEBUG)) {
                        logger.d("[MultiThreadNetworkLoadHandler:verbose]Calculate block num, firstConnectElapse:" + firstConnectElapse + ", total size:" + contentRange.totalSize + ", task:" + taskInfo);
                    }
                    long optimalElapse = Long.MAX_VALUE;
                    long elapse;
                    for (int blockNum = maxBlockNum; blockNum >= minBlockNum; blockNum--) {
                        elapse = firstConnectElapse * blockNum + (long) ((double) contentRange.totalSize / (double) (standardNetworkSpeed * blockNum));
                        if (elapse < optimalElapse) {
                            optimalElapse = elapse;
                            optimalBlockNum = blockNum;
                        }
                        if (verboseLog && logger.checkEnable(TLogger.DEBUG)) {
                            logger.d("[MultiThreadNetworkLoadHandler:verbose]Calculate block num, if blockNum = " + blockNum + ", elapse = " + elapse + ", task:" + taskInfo);
                        }
                    }
                }
                //calculate block size
                long firstBlockSize;
                long nextBlockSize;
                if (optimalBlockNum == 1){
                    //one block
                    firstBlockSize = contentRange.endPosition + 1;
                    nextBlockSize = firstBlockSize;
                } else if (contentRange.totalSize < (contentRange.endPosition + 1) * optimalBlockNum) {
                    //average distribution
                    firstBlockSize = (long) ((double) contentRange.totalSize / (double) optimalBlockNum);
                    nextBlockSize = firstBlockSize;
                } else {
                    //fill first block, the others average distribution
                    firstBlockSize = contentRange.endPosition + 1;
                    nextBlockSize = (long) ((double) (contentRange.totalSize - (contentRange.endPosition + 1)) / (double) (optimalBlockNum - 1));
                }
                //add first connect to list
                responseList.add(response);
                offsetList.add(new long[]{0, firstBlockSize - 1});
                if (verboseLog && logger.checkEnable(TLogger.DEBUG)) {
                    logger.d("[MultiThreadNetworkLoadHandler:verbose]Block 1, start:0, end:" + (firstBlockSize - 1) + ", task:" + taskInfo);
                }
                //connect others
                long start = firstBlockSize;
                long end;
                ContentRange subContentRange;
                int connectCount = 2;
                while (connectCount <= optimalBlockNum && start <= contentRange.totalSize - 1) {
                    end = start + nextBlockSize - 1;
                    if (end > contentRange.totalSize - 1) {
                        end = contentRange.totalSize - 1;
                    }
                    if (connectCount >= optimalBlockNum) {
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
                        exceptionHandler.onHttpContentRangeParseException(applicationContext, context, taskInfo, logger);
                        //close all connections
                        response.close();
                        for (Response responseItem : responseList) {
                            close(responseItem);
                        }
                        //clean list
                        responseList.removeAll(null);
                        offsetList.removeAll(null);
                        //reconnect
                        response = connect(null, taskInfo.getUrl(), 0, -1, -1, connectTimeout, readTimeout, taskInfo, logger);
                        responseList.add(response);
                        offsetList.add(new long[]{0, response.body().contentLength() - 1});
                        break;
                    }
                    //add to list
                    responseList.add(response);
                    offsetList.add(new long[]{start, end});
                    if (verboseLog && logger.checkEnable(TLogger.DEBUG)) {
                        logger.d("[MultiThreadNetworkLoadHandler:verbose]Block " + connectCount + ", start:" + start + ", end:" + end + ", task:" + taskInfo);
                    }
                    //next
                    start = start + nextBlockSize;
                    connectCount++;
                }
            }

            //set file size
            RandomAccessFile randomAccessFile = writerProvider.newRandomAccessFileForWrite();
            randomAccessFile.setLength(contentRange.totalSize);
            randomAccessFile.close();

            //refresh ui
            taskInfo.getLoadProgress().setTotal(contentRange.totalSize);

            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Network loading start, connections:" + responseList.size() + ", connect elapse:" + (System.currentTimeMillis() - connectStartTime) + ", task:" + taskInfo);
            }

            AtomicBoolean stopSignal = new AtomicBoolean(false);
            AtomicInteger finishSignal = new AtomicInteger(0);
            AtomicReference<Throwable> exceptionSignal = new AtomicReference<>(null);

            //dead line, fallback
            long deadLine = System.currentTimeMillis() + lowNetworkSpeedConfig.getDeadline() * 2;
            long readStartTime = System.currentTimeMillis();

            //start reading
            for (int i = 0 ; i < responseList.size() ; i++) {
                read(responseList.get(i), offsetList.get(i), stopSignal, finishSignal, exceptionSignal, writerProvider, taskInfo);
            }

            //watcher thread
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
                //check network speed
                if (!checkNetworkSpeed(applicationContext, context, readStartTime, taskInfo, lowNetworkSpeedConfig, exceptionHandler, logger)) {
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

            if (logger.checkEnable(TLogger.DEBUG)) {
                long readElapse = System.currentTimeMillis() - readStartTime;
                String readSpeed = readElapse <= 0 ? "0KB/s" : String.valueOf((int)(((double)contentRange.totalSize / 1024d) / ((double)readElapse / 1000d))) + "KB/s";
                logger.d("[MultiThreadNetworkLoadHandler]Network loading finish, connections:" + responseList.size() + ", read elapse:" + readElapse + ", speed:" + readSpeed + ", task:" + taskInfo);
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
            return new ContentRange(false, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
        }
        if (contentRangeOrigin.length() < 10
                || !contentRangeOrigin.startsWith("bytes ")) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
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
                return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
            }
            return new ContentRange(false, true, totalSize, startPosition, endPosition);
        }

        int index1 = contentRangeString.indexOf("-");
        if (index1 < 0) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
        } else if (index1 == 0) {
            startPosition = 0;
        } else {
            try {
                startPosition = Long.parseLong(contentRangeString.substring(0, index1));
            } catch (Exception e) {
                if (logger.checkEnable(TLogger.DEBUG)) {
                    logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
                }
                return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
            }
        }

        int index2 = contentRangeString.indexOf("/");
        if (index2 <= index1 || index2 == contentRangeString.length() - 1) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
        } else if (index2 == index1 + 1) {
            endPosition = response.body().contentLength() - 1;
        } else {
            try {
                endPosition = Long.parseLong(contentRangeString.substring(index1 + 1, index2));
            } catch (Exception e) {
                if (logger.checkEnable(TLogger.DEBUG)) {
                    logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
                }
                return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
            }
        }

        if (startPosition > endPosition) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
        }

        try {
            totalSize = Long.parseLong(contentRangeString.substring(index2 + 1));
        } catch (Exception e) {
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
        }

        if (endPosition > totalSize - 1){
            if (logger.checkEnable(TLogger.DEBUG)) {
                logger.d("[MultiThreadNetworkLoadHandler]Http ranges is not accepted, invalid Content-range:[" + contentRangeOrigin + "], task:" + taskInfo);
            }
            return new ContentRange(true, false, response.body().contentLength(), 0, response.body().contentLength() - 1);
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
                        start += writeLength;
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

        private ContentRange(boolean parseError, boolean acceptRanges, long totalSize, long startPosition, long endPosition) {
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
