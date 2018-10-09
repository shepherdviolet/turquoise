package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sviolet.thistle.entity.common.Destroyable;
import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.entity.IndispensableState;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.handler.ExceptionHandler;
import sviolet.turquoise.x.imageloader.handler.NetworkLoadHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.server.disk.DiskCacheServer;

/**
 * <p>Single thread network loading logic</p>
 *
 * <p>You can implement {@link AbstractNetworkLoadHandler#onHandle(Context, Context, Task.Info, NetworkCallback, long, long, long, TLogger)} method</p>
 *
 * <p>Example:{@link sviolet.turquoise.x.imageloader.handler.common.CommonNetworkLoadHandler}</p>
 *
 * @author S.Violet
 */
public abstract class AbstractNetworkLoadHandler implements NetworkLoadHandler {

    private static final int READ_BUFF_SIZE = 8 * 1024;
    private static final int OUTPUT_STREAM_BUFF_SIZE = 64 * 1024;

    /**
     * <p>Loading from net</p>
     * <p>
     * <p>CAUTION:</p>
     * <p>
     * <p>You should call "callback.setResultSucceed()"/"callback.setResultFailed()"/"callback.setResultCanceled()"
     * when process finished, whether loading succeed or failed. if not, NetworkEngine's thread will be block for a long time,
     * until NetworkCallback timeout.Because NetworkEngine will invoke callback.getResult, this method will block thread util you setResult.</p>
     *
     * <pre><@code
     *      public void onHandle(Context applicationContext, Context context, Task.Info taskInfo, NetworkCallback<Result> callback, long connectTimeout, long readTimeout, long imageDataLengthLimit, TLogger logger) {
     *          try{
     *              //load by third party network utils
     *              //don't forget set timeout
     *              XXX.get(url, connectTimeout, readTimeout, new OnFinishListener(){
     *                  public void onSucceed(InputStream inputStream){
     *                      //return result
     *                      callback.setResultSucceed(new WriteResult(inputStream));
     *                  }
     *                  public void onFailed(Exception e){
     *                      //return result
     *                      callback.setResultFailed(e);
     *                  }
     *              });
     *          }catch(Exception e){
     *              //return result
     *              callback.setResultFailed(e);
     *          }
     *      }
     * </pre>
     *
     * @param applicationContext application context
     * @param context            activity context, maybe null
     * @param taskInfo           taskInfo
     * @param callback           callback, you must return result by it.
     * @param connectTimeout     connect timeout of network
     * @param readTimeout        read timeout of network
     * @param imageDataLengthLimit length limit of data
     * @param logger             logger
     */
    protected abstract void onHandle(Context applicationContext, Context context, Task.Info taskInfo, NetworkCallback<Result> callback, long connectTimeout, long readTimeout, long imageDataLengthLimit, TLogger logger);

    /**
     * <p>Network loading result</p>
     * <p>
     * <p>you can return InputStream or bytes</p>
     */
    protected class Result implements Destroyable {

        public static final int UNKNOWN_LENGTH = -1;

        private ResultType type = ResultType.NULL;
        private byte[] bytes;
        private InputStream inputStream;
        private int length = UNKNOWN_LENGTH;

        public Result(InputStream inputStream) {
            if (inputStream == null) {
                this.type = ResultType.NULL;
                return;
            }
            this.type = ResultType.INPUTSTREAM;
            this.inputStream = inputStream;
        }

        /**
         * @deprecated Best to use {@link Result(InputStream)}, bytes result will cost more memory!!!
         */
        @Deprecated
        public Result(byte[] bytes) {
            if (bytes == null) {
                this.type = ResultType.NULL;
                return;
            }
            this.type = ResultType.BYTES;
            this.bytes = bytes;
            this.length = bytes.length;
        }

        public ResultType getType() {
            return type;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public int getLength() {
            return length;
        }

        public Result setLength(int length) {
            if (this.type != ResultType.NULL) {
                this.length = length;
            }
            return this;
        }

        @Override
        public void onDestroy() {
            type = ResultType.NULL;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (bytes != null) {
                bytes = null;
            }
        }
    }

    /**
     * Network loading result type
     */
    enum ResultType {
        NULL,
        BYTES,
        INPUTSTREAM
    }

    /*************************************************************************************************
     * Single thread loading logic
     */

    /**
     * Call {@link AbstractNetworkLoadHandler#onHandle(Context, Context, Task.Info, NetworkCallback, long, long, long, TLogger)}
     * to get InputStream or byte[], then write to disk cache.
     */
    @Override
    public final NetworkLoadHandler.HandleResult onHandle(
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

        //network loading, callback's timeout is triple of network timeout
        NetworkCallback<AbstractNetworkLoadHandler.Result> callback = new NetworkCallback<>((connectTimeout + readTimeout) * 3, logger);
        try {
            //call abstract method to get InputStream or byte[]
            onHandle(applicationContext, context, taskInfo, callback, connectTimeout, readTimeout, imageDataLengthLimit, logger);
        } catch (Exception e) {
            exceptionHandler.onNetworkLoadException(applicationContext, context, taskInfo, e, logger);
            return NetworkLoadHandler.HandleResult.FAILED;
        }

        //waiting for result
        int callbackResult = callback.getResult();
        if (logger.checkEnable(TLogger.DEBUG)) {
            logger.d("[NetworkEngine]get result from networkHandler, result:" + callbackResult + ", taskInfo:" + taskInfo);
        }

        switch (callbackResult) {
            //load succeed
            case NetworkCallback.RESULT_SUCCEED:
                AbstractNetworkLoadHandler.Result data = callback.getData();
                if (data.getType() == AbstractNetworkLoadHandler.ResultType.BYTES) {
                    //set progress
                    taskInfo.getLoadProgress().setTotal(data.getBytes().length);
                    taskInfo.getLoadProgress().setLoaded(data.getBytes().length);
                    //handle
                    return onBytesResult(applicationContext, context, writerProvider, data.getBytes(), taskInfo, indispensableState, lowNetworkSpeedConfig, imageDataLengthLimit, exceptionHandler, logger);
                } else if (data.getType() == AbstractNetworkLoadHandler.ResultType.INPUTSTREAM) {
                    //set progress
                    taskInfo.getLoadProgress().setTotal(data.getLength());
                    //handle
                    return onInputStreamResult(applicationContext, context, writerProvider, data.getInputStream(), taskInfo, indispensableState, lowNetworkSpeedConfig, imageDataLengthLimit, exceptionHandler, logger);
                } else {
                    exceptionHandler.onNetworkLoadException(applicationContext, context, taskInfo,
                            new Exception("[NetworkLoadHandler]callback return null result!"), logger);
                    return NetworkLoadHandler.HandleResult.FAILED;
                }
            //load failed
            case NetworkCallback.RESULT_FAILED:
                exceptionHandler.onNetworkLoadException(applicationContext, context, taskInfo, callback.getException(), logger);
                return NetworkLoadHandler.HandleResult.FAILED;
            //load canceled
            case NetworkCallback.RESULT_CANCELED:
            default:
                return NetworkLoadHandler.HandleResult.CANCELED;
        }

    }

    /**
     * Handle byte[] result
     */
    private NetworkLoadHandler.HandleResult onBytesResult(
            Context applicationContext,
            Context context,
            DiskCacheServer.WriterProvider writerProvider,
            byte[] bytes,
            Task.Info taskInfo,
            IndispensableState indispensableState,
            LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig,
            long imageDataLengthLimit,
            ExceptionHandler exceptionHandler,
            TLogger logger) {

        OutputStream outputStream = null;
        try {
            //write to disk cache
            if (bytes == null || bytes.length <= 0) {
                exceptionHandler.onDiskCacheWriteException(
                        applicationContext, context, taskInfo,
                        new Exception("[TILoader]disk cache write failed, bytes is null"), logger);
                return NetworkLoadHandler.HandleResult.FAILED;
            }
            outputStream = writerProvider.newOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            return NetworkLoadHandler.HandleResult.SUCCEED;
        } catch (Exception e) {
            exceptionHandler.onDiskCacheWriteException(
                    applicationContext, context, taskInfo, e, logger);
        } finally {
            closeStream(outputStream);
        }
        return NetworkLoadHandler.HandleResult.FAILED;
    }

    /**
     * handle InputStream result
     */
    private NetworkLoadHandler.HandleResult onInputStreamResult(
            Context applicationContext,
            Context context,
            DiskCacheServer.WriterProvider writerProvider,
            InputStream inputStream,
            Task.Info taskInfo,
            IndispensableState indispensableState,
            LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig,
            long imageDataLengthLimit,
            ExceptionHandler exceptionHandler,
            TLogger logger) {

        OutputStream outputStream = null;

        try {
            //cancel loading if image data out of limit
            if (taskInfo.getLoadProgress().total() > imageDataLengthLimit) {
                exceptionHandler.onImageDataLengthOutOfLimitException(applicationContext, context,
                        taskInfo, taskInfo.getLoadProgress().total(), imageDataLengthLimit, logger);
                return NetworkLoadHandler.HandleResult.CANCELED;
            }

            //get outputStream
            outputStream = writerProvider.newOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream, OUTPUT_STREAM_BUFF_SIZE);
            long startTime = System.currentTimeMillis();
            byte[] buffer = new byte[READ_BUFF_SIZE];
            int readLength;
            int loopCount = 0;

            //read from network and write to disk
            while (true) {
                try {
                    //read from inputStream
                    readLength = inputStream.read(buffer);
                } catch (Exception e) {
                    throw new NetworkException(e);
                }
                if (readLength < 0) {
                    break;
                }
                //record progress
                taskInfo.getLoadProgress().increaseLoaded(readLength);
                //check if data out of limit
                if (taskInfo.getLoadProgress().loaded() > imageDataLengthLimit) {
                    exceptionHandler.onImageDataLengthOutOfLimitException(applicationContext, context,
                            taskInfo, taskInfo.getLoadProgress().loaded(), imageDataLengthLimit, logger);
                    return NetworkLoadHandler.HandleResult.CANCELED;
                }
                //check if speed is low
                if (!checkNetworkSpeed(applicationContext, context, startTime, loopCount, taskInfo, lowNetworkSpeedConfig, exceptionHandler, logger)) {
                    return NetworkLoadHandler.HandleResult.CANCELED;
                }
                //write disk
                bufferedOutputStream.write(buffer, 0, readLength);
                loopCount++;
            }

            if (loopCount == 0) {
                throw new NetworkException(new Exception("[TILoader]network load failed, null content received (1)"));
            }

            //succeed
            bufferedOutputStream.flush();
            return NetworkLoadHandler.HandleResult.SUCCEED;

        } catch (NetworkException e) {
            exceptionHandler.onNetworkLoadException(
                    applicationContext, context, taskInfo, e, logger);
        } catch (Exception e) {
            exceptionHandler.onDiskCacheWriteException(
                    applicationContext, context, taskInfo, e, logger);
        } finally {
            closeStream(inputStream);
            closeStream(outputStream);
        }

        return NetworkLoadHandler.HandleResult.FAILED;

    }

    /**
     * Check network speed by LowNetworkSpeedStrategy
     */
    private boolean checkNetworkSpeed(
            Context applicationContext,
            Context context,
            long startTime,
            int loopCount,
            Task.Info taskInfo,
            LowNetworkSpeedStrategy.Configure lowNetworkSpeedConfig,
            ExceptionHandler exceptionHandler,
            TLogger logger){

        //decrease check frequency
        if (loopCount << 30 != 0){
            return true;
        }

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

    private void closeStream(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeStream(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static class NetworkException extends Exception{

        public NetworkException(Throwable throwable) {
            super(throwable);
        }

    }

}
