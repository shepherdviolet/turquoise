package sviolet.turquoise.utils;

/**
 * 用于检查数据的工具
 * Created by S.Violet on 2015/8/28.
 */
public class CheckUtils {

    /**
     * 检查String是否为空<br/>
     * null / "" <br/>
     * @param input 检查数据
     * @return true 空 false 非空
     */
    public static boolean isEmpty(String input){
        if (input == null){
            return true;
        } else if (input.length() <= 0){
            return true;
        }
        return false;
    }

}
