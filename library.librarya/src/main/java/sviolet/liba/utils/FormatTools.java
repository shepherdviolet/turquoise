package sviolet.liba.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

/**
 * Bitmap与DrawAble与byte[]与InputStream与String之间的转换工具类
 * 
 * @author Administrator
 * 
 */
public class FormatTools {
	private static FormatTools tools = new FormatTools();
	final int BUFFER_SIZE = 4096;

	public static FormatTools getInstance() {
		if (tools == null) {
			tools = new FormatTools();
			return tools;
		}
		return tools;
	}

	// 将byte[]转换成InputStream
	public InputStream Bytes2InputStream(byte[] b) {
		ByteArrayInputStream bais = new ByteArrayInputStream(b);
		return bais;
	}

	/**
	 * 将byte数组转换成String
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public String Bytes2String(byte[] in) {
		InputStream is;
		try {
			is = Bytes2InputStream(in);
			return InputStream2String(is);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * byte[]转换成Bitmap
	 * */
	public Bitmap Bytes2Bitmap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		}
		return null;
	}

	/**
	 * byte[]转换成Drawable
	 * */
	public Drawable Bytes2Drawable(byte[] b) {
		Bitmap bitmap = this.Bytes2Bitmap(b);
		return this.Bitmap2Drawable(bitmap);
	}

	// Drawable转换成InputStream
	public InputStream Drawable2InputStream(Drawable d) {
		Bitmap bitmap = this.Drawable2Bitmap(d);
		return this.Bitmap2InputStream(bitmap);
	}

	/**
	 * Drawable转换成byte[]
	 * */
	public byte[] Drawable2Bytes(Drawable d) {
		Bitmap bitmap = this.Drawable2Bitmap(d);
		return this.Bitmap2Bytes(bitmap);
	}

	/**
	 * Drawable转换成Bitmap
	 * */
	public Bitmap Drawable2Bitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * Bitmap转换成byte[]
	 * */
	public byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * Bitmap转换成Drawable
	 * */
	@SuppressWarnings("deprecation")
	public Drawable Bitmap2Drawable(Bitmap bitmap) {
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		Drawable d = (Drawable) bd;
		return d;
	}

	/**
	 * 将Bitmap转换成InputStream
	 * */
	public InputStream Bitmap2InputStream(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	/**
	 * 将Bitmap转换成InputStream
	 * */
	public InputStream Bitmap2InputStream(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		InputStream is = new ByteArrayInputStream(baos.toByteArray());
		return is;
	}

	/**
	 * 将String转换成byte[]
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public byte[] String2Bytes(String in) {
		try {
			return in.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * 将String转换成InputStream
	 * 
	 * @param in
	 *            ：转化字符串
	 * @return
	 * @throws Exception
	 */
	public InputStream String2InputStream(String in) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(in.getBytes());
		return is;
	}

	/**
	 * 将String转换成InputStream
	 * 
	 * @param in
	 *            ： 转化字符串，encoding：编码格式
	 * @return
	 * @throws Exception
	 */
	public InputStream String2InputStream(String in, String encoding)
			throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(
				in.getBytes(encoding));
		return is;
	}

	/**
	 * 将InputStream转换成byte[]
	 * 
	 * @param is
	 *            ：数据流
	 * */
	public byte[] InputStream2Bytes(InputStream is) {
		String str = "";
		byte[] readByte = new byte[1024];
		try {
			while ((is.read(readByte, 0, 1024)) != -1) {
				str += new String(readByte).trim();
			}
			return str.getBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将InputStream转换成Bitmap
	 * */
	public Bitmap InputStream2Bitmap(InputStream is) {
		return BitmapFactory.decodeStream(is);
	}

	/**
	 * InputStream转换成Drawable
	 * */
	public Drawable InputStream2Drawable(InputStream is) {
		Bitmap bitmap = this.InputStream2Bitmap(is);
		return this.Bitmap2Drawable(bitmap);
	}

	/**
	 * 将InputStream转换成某种字符编码的String
	 * 
	 * @param in
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public String InputStream2String(InputStream in, String encoding)
			throws Exception {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
			outStream.write(data, 0, count);

		data = null;
		return new String(outStream.toByteArray(), encoding);
	}

	/**
	 * 将InputStream转换成String
	 * 
	 * @param in
	 *            InputStream
	 * @return String
	 * @throws Exception
	 * 
	 */
	public String InputStream2String(InputStream in) throws Exception {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] data = new byte[BUFFER_SIZE];
		int count = -1;
		while ((count = in.read(data, 0, BUFFER_SIZE)) != -1)
			outStream.write(data, 0, count);

		data = null;
		return new String(outStream.toByteArray(), "UTF-8");
	}

	/**
	 * 图片进行base64编码
	 * 
	 * @param Drawable
	 *            图片对象
	 * @return String
	 * @throws IOException
	 * */
	public String Drawable2Base64(Drawable drawable) throws IOException {
		InputStream in = Drawable2InputStream(drawable);
		return InputStream2Base64(in);
	}

	/**
	 * 图片进行base64编码
	 * 
	 * @param InputStream
	 *            图片数据流
	 * @return String
	 * @throws IOException
	 * */
	public String InputStream2Base64(InputStream in) throws IOException {
		byte[] bytes = null;
		bytes = new byte[in.available()];
		in.read(bytes);
		in.close();
		return Base64.encodeToString(bytes, Base64.DEFAULT);

		// Base64.encode(input, flags)
		// BASE64Encoder encoder = new BASE64Encoder();
		// return encoder.encode(bytes);
	}
}
