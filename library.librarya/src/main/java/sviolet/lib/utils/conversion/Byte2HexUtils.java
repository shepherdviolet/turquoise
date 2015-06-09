package sviolet.lib.utils.conversion;

/**
 * byte[]转16进制String
 * 
 * @author S.Violet
 *
 */
public class Byte2HexUtils {
	
	public static String shift(byte[] src){  
	    StringBuilder stringBuilder = new StringBuilder("");  
	    
	    if (src == null || src.length <= 0)
	    	return "";
	    
	    for (int i = 0; i < src.length; i++) {  
	    	
	        int v = src[i] & 0xFF;  
	        
	        String hv = Integer.toHexString(v);  
	        if (hv.length() < 2) {  
	            stringBuilder.append(0);  
	        }  
	        
	        stringBuilder.append(hv);  
	        
	    }  
	    
	    return stringBuilder.toString();
	}  
	
}
