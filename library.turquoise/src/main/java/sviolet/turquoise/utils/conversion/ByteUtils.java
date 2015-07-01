package sviolet.turquoise.utils.conversion;

/**
 * Byte处理工具
 * 
 * @author S.Violet
 */

public class ByteUtils {

	/**
	 * 把两个byte[]前后拼接成一个byte[]
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static byte[] joint(byte[] left, byte[] right){
		byte[] result = new byte[left.length + right.length];
		System.arraycopy(left, 0, result, 0, left.length);
		System.arraycopy(right, 0, result, left.length, right.length);
		return result;
	}
	
}
