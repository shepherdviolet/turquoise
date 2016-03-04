package sviolet.turquoise.common.statics;

/**
 * 特殊资源ID
 *
 * Created by S.Violet on 2016/1/13.
 */
public class SpecialResourceId {

    public static class ViewTag{

        /**
         * 用于SimpleBitmapLoader<br/>
         * 将SimpleBitmapLoaderTask作为View的Tag绑定在View上<br/>
         */
        public static final int SimpleBitmapLoaderTask = 0xfff77f00;

        /**
         * 用于ViewHolder<br/>
         * 将ViewHolder作为View的Tag绑定在View上<br/>
         */
        public static final int ViewHolder = 0xfff77f01;

        /**
         * for TILoader's Task : sviolet.turquoise.x.imageloader.task.Task <br/>
         * used to bind Task on View as a Tag<br/>
         */
        public static final int TILoaderTask = 0xfff77f02;

    }

}
