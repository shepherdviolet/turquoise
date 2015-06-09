package sviolet.turquoise.utils;

/**
 * 二进制处理工具
 * 
 * @author S.Violet
 *
 */

public class BinaryUtils {

	/**
	 * 获取int中指定的一位bit<br>
	 * <br>
	 * getBit(0x01010001, 0) => 1<br>
	 * 0101000_1_<br>
	 * 截取0x01010001中从右往左数第1位<br>
	 * <br>
	 * getBit(0x01010001, 1) => 0<br>
	 * 010100_0_1<br>
	 * 截取0x01010001中从右往左数第2位<br>
	 * <br>
	 * getBit(0x01010001, 4) = 1<br>
	 * 010_1_0001<br>
	 * 截取0x01010001中从右往左数第5位<br>
	 * <br>
	 * @param number 被截取的数字
	 * @param index 截取bit的位置, 从右往左, 右边第一位位置为0
	 * @return
	 */
    public static int getBit(int number, int index){
        return ( number & ( 0x1 << index ) ) >> index;
    }
	
	/**
	 * 获取int中指定的一位标志位boolean(1:true 0:false)<br>
	 * <br>
	 * getBit(0x01010001, 0) => true<br>
	 * 0101000_1_<br>
	 * 截取0x01010001中从右往左数第1位<br>
	 * <br>
	 * getBit(0x01010001, 1) => false<br>
	 * 010100_0_1<br>
	 * 截取0x01010001中从右往左数第2位<br>
	 * <br>
	 * getBit(0x01010001, 4) = true<br>
	 * 010_1_0001<br>
	 * 截取0x01010001中从右往左数第5位<br>
	 * 
	 * @param number 被截取的数字
	 * @param index 截取标志位的位置, 从右往左, 右边第一位位置为0
	 * @return
	 */
    public static boolean getFlag(int number, int index){
        return (( number & ( 0x1 << index ) ) >> index) == 1;
    }
}
