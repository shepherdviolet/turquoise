package sviolet.turquoise.util.droid;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * FrameBuffer(帧缓冲)读取工具(需要ROOT权限)
 * 
 * @author S.Violet ()
 *
 */
public class FrameBufferUtils {

	/**
	 * 获得FrameBuffer的输入流(需要ROOT权限)
	 * 
	 * @return
	 * @throws FileNotFoundException 
	 */
	private static InputStream getInputStream() throws FileNotFoundException{
		return new FileInputStream(new File("/dev/graphics/fb0"));
	}
	
	/**
	 * 从FrameBuffer帧缓存中获取一帧数据
	 * 
	 * @param WHD
	 * @return byte[] 帧数据
	 * @throws IOException
	 */
	public static byte[] getFrameBuffer(int[] WHD) throws IOException{
		//WHD[0]宽度 * WHD[1]高度 * WHD[2]深度
		byte[] pixels =new byte[WHD[0] * WHD[1] * WHD[2]];
		DataInputStream dataInputStream =new DataInputStream(getInputStream());
		dataInputStream.readFully(pixels);
		dataInputStream.close();
		return pixels;
	}
	
	/**
	 * 获得屏幕截图(Bitmap)
	 * 
	 * @param context
	 * @return Bitmap
	 * @throws IOException
	 */
	public static Bitmap getScreenShot(Context context) throws IOException{
		//WHD[0]宽度 * WHD[1]高度 * WHD[2]深度
        int[] WHD = getScreenWHD(context);
        int[] colors = convertToARGB8888(getFrameBuffer(WHD), PixelFormat.RGBA_8888);
		return Bitmap.createBitmap(colors,WHD[0],WHD[1],Bitmap.Config.ARGB_8888);
	}
	
	/**
	 * 得到屏幕的宽度/高度/色深度(像素)
	 * 
	 * @param context
	 * @return {width,height,deepth}
	 */
	@SuppressWarnings("deprecation")
	public static int[] getScreenWHD(Context context){
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		
		//获得屏幕宽高(像素)
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;

		// 获取显示方式
		int localPixelFormat = display.getPixelFormat();
		PixelFormat pixelFormat = new PixelFormat();
		PixelFormat.getPixelFormatInfo(localPixelFormat, pixelFormat);
		int deepth = pixelFormat.bytesPerPixel;// 位深
		
		return new int[]{width,height,deepth};
	}
	
	/**
	 * 获得当前屏幕中一个像素点的颜色
	 * [0]Alpha [1]R [2]G [3]B 
	 * 
	 * @param context
	 * @param x X轴坐标
	 * @param y Y轴坐标
	 * @return 
	 * @throws IOException 
	 */
	public static int[] getPixelColor(Context context,int x, int y) throws IOException{
		int[] WHD = getScreenWHD(context);
		byte[] frameBuffer = getFrameBuffer(WHD);
		return getPixelColor(frameBuffer,WHD,x,y);
	}
	
	/**
	 * 获得frameBuffer中一个像素点的颜色
	 * [0]Alpha [1]R [2]G [3]B 
	 * 
	 * @param frameBuffer 帧缓存数据 getFrameBuffer()方法获得
	 * @param WHD 屏幕宽高深 getScreenWHD()方法获得
	 * @param x X轴坐标
	 * @param y Y轴坐标
	 * @return
	 */
	public static int[] getPixelColor(byte[] frameBuffer,int[] WHD, int x, int y){
		int i = y * WHD[0] + x;
		int blue = toUnsigned(frameBuffer[4 * i]);
		int green = toUnsigned(frameBuffer[4 * i + 1]);
		int red = toUnsigned(frameBuffer[4 * i + 2]);
		int alpha = toUnsigned(frameBuffer[4 * i + 3]);
		return new int[]{alpha,red,green,blue};
	}
	
	/**
	 * [算法]
	 * 将frameBuffer的一帧数据(byte[])转换为颜色数据(int[])
	 * 
	 * @param frameBuffer 一帧的数据
	 * @param pixelFormat 格式
	 * @return
	 * 
	 * @author S.Violet()
	 */
	private static int[] convertToARGB8888(byte[] frameBuffer, int pixelFormat) {
		int[] colors = null;
		switch (pixelFormat) {
		//自制算法
		case PixelFormat.RGBA_8888:
		case PixelFormat.RGBX_8888:
			colors = new int[frameBuffer.length / 4];
			for (int x = 0; x < colors.length; x++) {
				int blue = toUnsigned(frameBuffer[4 * x]);
				int green = toUnsigned(frameBuffer[4 * x + 1]);
				int red = toUnsigned(frameBuffer[4 * x + 2]);
				int alpha = toUnsigned(frameBuffer[4 * x + 3]);
				colors[x] = blue | green << 8 | red << 16 | alpha << 24;
			}
			break;
			
		//565算法可能会有问题,未测试
		case PixelFormat.RGB_565:
			colors = new int[frameBuffer.length / 2];
			for (int x = 0; x < colors.length; x++) {
				int rgb = frameBuffer[2 * x] + frameBuffer[2 * x + 1] * 256;
				int red = rgb >> 11;
				red = (red << 3) | (red >> 2);
				int green = (rgb >> 5) & 63;
				green = (green << 2) | (green >> 4);
				int blue = rgb & 31;
				blue = (blue << 3) | (blue >> 2);
				colors[x] = 0xff000000 | (red << 16) | (green << 8) | blue;
			}
			break;
		}
		return colors;
	}
	
	/**
	 * 将byte转换为无符号数
	 * 
	 * @param i
	 * @return
	 */
	private static int toUnsigned(byte i){
		if(i < 0) {
            return i + 256;
        } else {
            return i;
        }
	}
}
