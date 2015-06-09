package sviolet.lib.utils.conversion;

public class Object2Base64Utils{

	public static byte[] toByteArray(Object obj) {
		return Base64Utils.encode(Byte2ObjectUtils.toByteArray(obj));
	}

	public static Object toObject(byte[] bytes) {
		return Byte2ObjectUtils.toObject(Base64Utils.decode(bytes));
	}
	
}
