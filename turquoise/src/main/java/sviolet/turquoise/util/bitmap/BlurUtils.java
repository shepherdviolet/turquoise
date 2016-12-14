package sviolet.turquoise.util.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.RequiresApi;

/**
 * 高斯模糊工具(JAVA方式, 若需要提升性能请使用JNI方式的库)
 *
 * Created by S.Violet on 2016/12/14.
 */
public class BlurUtils {

    /**
     * @param context context
     * @param bitmap bitmap
     * @param radius radius [0, 25]
     * @param recycle true: recycle origin bitmap
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static Bitmap blurByRenderScript(Context context, Bitmap bitmap, float radius, boolean recycle){
        if (context == null){
            throw new RuntimeException("context is null");
        }
        if (bitmap == null){
            return null;
        }
        //Let's create an empty bitmap with the same size of the bitmap we want to blur
        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        //Instantiate a new Renderscript
        RenderScript renderScript = RenderScript.create(context);
        //Create an Intrinsic Blur Script using the Renderscript
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
        Allocation allocationIn = Allocation.createFromBitmap(renderScript, bitmap);
        Allocation allocationOut = Allocation.createFromBitmap(renderScript, result);
        //Set the radius of the blur
        blurScript.setRadius(radius);
        //Perform the Renderscript
        blurScript.setInput(allocationIn);
        blurScript.forEach(allocationOut);
        //Copy the final bitmap created by the out Allocation to the outBitmap
        allocationOut.copyTo(result);
        //recycle the original bitmap
        if (recycle) {
            bitmap.recycle();
        }
        //After finishing everything, we destroy the Renderscript.
        renderScript.destroy();
        return result;
    }

    /**
     * Stack Blur v1.0 from
     * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
     *
     * Java Author: Mario Klingemann <mario at quasimondo.com>
     * http://incubator.quasimondo.com
     * created Feburary 29, 2004
     * Android port : Yahel Bouaziz <yahel at kayenko.com>
     * http://www.kayenko.com
     * ported april 5th, 2012
     *
     * This is a compromise between Gaussian Blur and Box blur
     * It creates much better looking blurs than Box Blur, but is
     * 7x faster than my Gaussian Blur implementation.
     *
     * I called it Stack Blur because this describes best how this
     * filter works internally: it creates a kind of moving stack
     * of colors whilst scanning through the image. Thereby it
     * just has to add one new block of color to the right side
     * of the stack and remove the leftmost color. The remaining
     * colors on the topmost layer of the stack are either added on
     * or reduced by one, depending on if they are on the right or
     * on the left side of the stack.
     *
     * If you are using this algorithm in your code please add
     * the following line:
     *
     * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
     *
     * @param bitmap bitmap
     * @param radius radius >= 1
     * @param recycle true: recycle origin bitmap
     */
    public static Bitmap blurByJava(Bitmap bitmap, int radius, boolean recycle) {
        if (bitmap == null){
            return null;
        }
        if (radius < 1) {
            throw new RuntimeException("radius must >= 1");
        }

        Bitmap result = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        if (recycle){
            bitmap.recycle();
        }

        int w = result.getWidth();
        int h = result.getHeight();

        int[] pix = new int[w * h];
        result.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rSum, gSum, bSum, x, y, i, p, yp, yi, yw;
        int vMin[] = new int[Math.max(w, h)];

        int divSum = (div + 1) >> 1;
        divSum *= divSum;
        int dv[] = new int[256 * divSum];
        for (i = 0; i < 256 * divSum; i++) {
            dv[i] = (i / divSum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackPointer;
        int stackStart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int rOutSum, gOutSum, bOutSum;
        int rInSum, gInSum, bInSum;

        for (y = 0; y < h; y++) {
            rInSum = gInSum = bInSum = rOutSum = gOutSum = bOutSum = rSum = gSum = bSum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rSum += sir[0] * rbs;
                gSum += sir[1] * rbs;
                bSum += sir[2] * rbs;
                if (i > 0) {
                    rInSum += sir[0];
                    gInSum += sir[1];
                    bInSum += sir[2];
                } else {
                    rOutSum += sir[0];
                    gOutSum += sir[1];
                    bOutSum += sir[2];
                }
            }
            stackPointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rSum];
                g[yi] = dv[gSum];
                b[yi] = dv[bSum];

                rSum -= rOutSum;
                gSum -= gOutSum;
                bSum -= bOutSum;

                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];

                rOutSum -= sir[0];
                gOutSum -= sir[1];
                bOutSum -= sir[2];

                if (y == 0) {
                    vMin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vMin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rInSum += sir[0];
                gInSum += sir[1];
                bInSum += sir[2];

                rSum += rInSum;
                gSum += gInSum;
                bSum += bInSum;

                stackPointer = (stackPointer + 1) % div;
                sir = stack[(stackPointer) % div];

                rOutSum += sir[0];
                gOutSum += sir[1];
                bOutSum += sir[2];

                rInSum -= sir[0];
                gInSum -= sir[1];
                bInSum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rInSum = gInSum = bInSum = rOutSum = gOutSum = bOutSum = rSum = gSum = bSum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rSum += r[yi] * rbs;
                gSum += g[yi] * rbs;
                bSum += b[yi] * rbs;

                if (i > 0) {
                    rInSum += sir[0];
                    gInSum += sir[1];
                    bInSum += sir[2];
                } else {
                    rOutSum += sir[0];
                    gOutSum += sir[1];
                    bOutSum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackPointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rSum] << 16) | (dv[gSum] << 8) | dv[bSum];

                rSum -= rOutSum;
                gSum -= gOutSum;
                bSum -= bOutSum;

                stackStart = stackPointer - radius + div;
                sir = stack[stackStart % div];

                rOutSum -= sir[0];
                gOutSum -= sir[1];
                bOutSum -= sir[2];

                if (x == 0) {
                    vMin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vMin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rInSum += sir[0];
                gInSum += sir[1];
                bInSum += sir[2];

                rSum += rInSum;
                gSum += gInSum;
                bSum += bInSum;

                stackPointer = (stackPointer + 1) % div;
                sir = stack[stackPointer];

                rOutSum += sir[0];
                gOutSum += sir[1];
                bOutSum += sir[2];

                rInSum -= sir[0];
                gInSum -= sir[1];
                bInSum -= sir[2];

                yi += w;
            }
        }

        result.setPixels(pix, 0, w, 0, 0, w, h);

        return result;
    }

}
