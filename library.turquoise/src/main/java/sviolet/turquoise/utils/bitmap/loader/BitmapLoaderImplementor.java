package sviolet.turquoise.utils.bitmap.loader;

import sviolet.turquoise.utils.bitmap.BitmapUtils;

/**
 * BitmapLoader实现器<br/>
 * <br/>
 * 实现BitmapLoader待实现的方法:<br/>
 * 1.实现cacheKey的生成规则<br/>
 * 2.实现网络加载图片<br/>
 * 3.实现异常处理<br/>
 */
public interface BitmapLoaderImplementor {

    /**
     * [实现提示]:<br/>
     * 通常将url进行摘要计算, 得到摘要值作为cacheKey, 根据实际情况实现.<Br/>
     * AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @return 实现根据URL计算并返回缓存Key
     */
    public String getCacheKey(String url);

    /**
     * 实现根据url参数从网络下载图片数据, 依照需求尺寸reqWidth和reqHeight解析为合适大小的Bitmap,
     * 并调用结果容器BitmapLoaderHolder.setResultSucceed/setResultFailed/setResultCanceled方法返回结果<br/>
     * <br/>
     * *********************************************************************************<br/>
     * * * 注意::<br/>
     * *********************************************************************************<br/>
     * <br/>
     * 1.网络请求必须做超时处理,否则任务会一直阻塞等待.<br/>
     * <br/>
     * 2.数据解析为Bitmap时,请根据需求尺寸reqWidth和reqHeight解析,以节省内存.<br/>
     * @see BitmapUtils <br/>
     *      需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).<br/>
     *      图片解码时根据需求尺寸适当缩,且保持原图长宽比例,解码后Bitmap实际尺寸不等于需求尺寸.设置为0不缩小图片.<Br/>
     * <br/>
     * 3.必须使用BitmapLoaderHolder.setResultSucceed/setResultFailed/setResultCanceled方法返回结果,
     *      若不调用,图片加载任务中的BitmapLoaderHolder.getResult()会一直阻塞等待结果.<br/>
     *      1)setResultSucceed(Bitmap),加载成功,返回Bitmap<br/>
     *      2)setResultFailed(Throwable),加载失败,返回异常<br/>
     *      3)setResultCanceled(),加载取消<br/>
     *      若加载任务已被取消(isCancelling() = true),但仍使用setResultSucceed返回结果,则Bitmap会被存入
     *      磁盘缓存,但BitmapLoader返回任务取消.<br/>
     * <br/>
     * 4.合理地处理加载任务取消的情况<br/>
     *      1)当加载任务取消,终止网络加载,并用BitmapLoaderHolder.setResultCanceled()返回结果<br/>
     *          采用此种方式,已加载的数据废弃,BitmapLoader作为任务取消处理,不会返回Bitmap.<br/>
     *      2)当加载任务取消,继续完成网络加载,并用BitmapLoaderHolder.setResultSucceed(Bitmap)返回结果<br/>
     *          采用此种方式,Bitmap会被存入磁盘缓存,但BitmapLoader仍作为任务取消处理,不会返回Bitmap.<br/>
     * <br/>
     * *********************************************************************************<br/>
     * * * 代码示例::<br/>
     * *********************************************************************************<br/>
     * <br/>
     * 5.无论同步还是异步的情况,均使用resultHolder.set(Bitmap)返回结果.<br/>
     * 同步网络请求:<br/>
     *
     *      //网络加载代码
     *      ......
     *      resultHolder.set(bitmap);
     *
     * <br/>
     * 异步网络请求:<br/>
     *
     *      //异步处理的情况
     *      new Thread(new Runnable(){
     *          public void run() {
     *              //网络加载代码
     *              ......
     *              resultHolder.set(bitmap);
     *          }
     *      }).start();
     *
     *
     * @param url url
     * @param reqWidth 请求宽度
     * @param reqHeight 请求高度
     * @param holder 结果容器
     */
    public void loadFromNet(String url, int reqWidth, int reqHeight, BitmapLoaderHolder holder);

    /**
     * 实现异常处理
     */
    public void onException(Throwable throwable);

    /**
     * 实现写入缓存文件时的异常处理, 通常只需要打印日志或提醒即可
     */
    public void onCacheWriteException(Throwable throwable);

}
