package sviolet.lib.utils.conversion;

public class ByteJointUtils {
	
	/**
	 * 把两个byte[]前后拼接成一个byte[]
	 * 
	 * @param byte1
	 * @param byte2
	 * @return
	 */
	
	public static byte[] joint(byte[] byte1, byte[] byte2){
		byte[] result = new byte[byte1.length + byte2.length];
		System.arraycopy(byte1, 0, result, 0, byte1.length);
		System.arraycopy(byte2, 0, result, byte1.length, byte2.length);
		return result;
	}
}
