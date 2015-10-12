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
     * 通常将url进行摘要计算, 得到摘要值作为cacheKey, 根据实际情况实现.  <Br/>
     * AsyncBitmapLoader中每个位图资源都由url唯一标识, url在AsyncBitmapLoader内部
     * 将由getCacheKey()方法计算为一个cacheKey, 内存缓存/磁盘缓存/队列key都将使用
     * 这个cacheKey标识唯一的资源<br/>
     *
     * @return 实现根据URL计算并返回缓存Key
     */
    public String getCacheKey(String url);

    /**
     * 实现根据url参数从网络下载图片数据, 依照需求尺寸reqWidth和reqHeight解析为合适大小的Bitmap,
     * 并调用结果容器resultHolder.set(Bitmap)方法将Bitmap返回, 若加载失败则set(null).<br/>
     * <br/>
     * * * 注意::<br/>
     * <br/>
     * 1.网络请求注意做超时处理,否则任务可能会一直等待.<br/>
     * <br/>
     * 2.数据解析为Bitmap时,请根据需求尺寸reqWidth和reqHeight解析,以节省内存.<br/>
     * @see BitmapUtils <br/>
     * * * 需求尺寸定义::<br/>
     * * * 需求尺寸(reqWidth/reqHeight)参数用于节省内存消耗,请根据界面展示所需尺寸设置(像素px).<br/>
     * * * 图片解码时会根据需求尺寸整数倍缩小,且长宽保持原图比例,解码后的Bitmap尺寸通常不等于需求尺寸.设置为0不缩小图片.<Br/>
     * <br/>
     * 4.线程会阻塞等待,直到resultHolder.set(Bitmap)方法执行.若任务被cancel,阻塞也会被中断,且即使后续
     * 网络请求返回了Bitmap,也会被Bitmap.recycle().<br/>
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
     * @param resultHolder 结果容器
     */
    public void loadFromNet(String url, int reqWidth, int reqHeight, BitmapLoaderHolder resultHolder);

    /**
     * 实现异常处理
     */
    public void onException(Throwable throwable);

    /**
     * 实现写入缓存文件时的异常处理, 通常只需要打印日志或提醒即可
     */
    public void onCacheWriteException(Throwable throwable);

}
