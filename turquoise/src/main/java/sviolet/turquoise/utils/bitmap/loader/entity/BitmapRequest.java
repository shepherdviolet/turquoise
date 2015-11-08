package sviolet.turquoise.utils.bitmap.loader.entity;

import sviolet.turquoise.utils.bitmap.BitmapUtils;

/**
 * BitmapLoader图片加载请求参数<p/>
 *
 * ***********************************************<br/>
 * 参数规范<br/>
 * ***********************************************<p/>
 *
 * url : 图片加载地址 : 必选<p/>
 *
 * 说明:<br/>
 * 图片的加载地址, 也作为图片的唯一标识, 经过sha1运算作为内存缓存和磁盘缓存的键值.<p/>
 *
 * ***********************************************<p/>
 *
 * ReqDimension(reqWidth/reqHeight) : 需求尺寸 : 可选<p/>
 *
 * 说明:<br/>
 * 在界面上显示所需要的尺寸, 通常由显示控件尺寸决定, 目的是在图片解码时根据该尺寸"适当"缩小, 以节省内存开销,
 * 原则上, 不放大图片, 解码后的图片尺寸略大于需求尺寸, 或接近需求尺寸, 且保持原图长宽比. "生效条件"不满足时,
 * 图片保持原图尺寸不变. <p/>
 *
 * 生效条件:<br/>
 * reqWidth > 0 && reqHeight > 0 <p/>
 *
 * 缩小算法可参考{@link BitmapUtils}<p/>
 *
 * ***********************************************<p/>
 *
 * PresetDimension(presetWidth/presetHeight) : 预设尺寸 : 可选<p/>
 *
 * 说明:<br/>
 * 图片加载时, 加载图的尺寸. 用于可提前获得目的图片尺寸的场合, 使得加载图与目的图片尺寸一致, 提前调整控件尺
 * 寸, 提升显示效果. "生效条件"不满足时, 加载图保持原图尺寸不变. <p/>
 *
 * 生效条件:<br/>
 * presetWidth > 0 && presetHeight > 0 <p/>
 *
 * 实现可参考{@link sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoaderTask}<p/>
 *
 * ***********************************************<p/>
 *
 * Created by S.Violet on 2015/11/6.
 */
public class BitmapRequest {

    private String url;

    /**
     * @param url 图片加载地址, 不为空
     */
    public BitmapRequest(String url){
        if (url == null || "".equals(url))
            throw new RuntimeException("[BitmapRequest]url must not be null");
        this.url = url;
    }

    /**
     * @return 图片加载地址, 不为空
     */
    public String getUrl(){
        return url;
    }

    /******************************************************
     * 需求尺寸
     ******************************************************/

    private int reqWidth;//需求宽度
    private int reqHeight;//需求高度

    /**
     * @param reqWidth 需求宽度
     * @param reqHeight 需求高度
     */
    public BitmapRequest setReqDimension(int reqWidth, int reqHeight){
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
        return this;
    }

    /**
     * @return 返回需求宽度
     */
    public int getReqWidth() {
        return reqWidth;
    }

    /**
     * @return 返回需求高度
     */
    public int getReqHeight() {
        return reqHeight;
    }

    /**
     * @return 需求尺寸是否生效
     */
    public boolean hasReqDimension(){
        return reqWidth > 0 && reqHeight > 0;
    }

    /******************************************************
     * 预设尺寸
     ******************************************************/

//    private int presetWidth;//预设宽度
//    private int presetHeight;//预设高度
//
//    /**
//     * @param presetWidth 预设宽度
//     * @param presetHeight 预设高度
//     */
//    public BitmapRequest setPresetDimension(int presetWidth, int presetHeight){
//        this.presetWidth = presetWidth;
//        this.presetHeight = presetHeight;
//        return this;
//    }
//
//    /**
//     * @return 返回预设宽度
//     */
//    public int getPresetWidth() {
//        return presetWidth;
//    }
//
//    /**
//     * @return 返回预设高度
//     */
//    public int getPresetHeight() {
//        return presetHeight;
//    }
//
//    /**
//     * @return 预设尺寸是否生效
//     */
//    public boolean hasPresetDimension(){
//        return presetWidth > 0 && presetHeight > 0;
//    }

}
