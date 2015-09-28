package sviolet.liba.io.net.lan;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiIpManager {
	
	private WifiManager wifiManager;
	
	/*
	 * @param wifiManager
	 * 
	 * Activity中通过该方法获得WifiManager wifiManager=(WifiManager)getSystemService(Context.WIFI_SERVICE); 
	 * 
	 * @permission <uses-permission android:name = "android.permission.ACCESS_WIFI_STATE"/>
	 */
	public WifiIpManager(WifiManager wifiManager){
		this.wifiManager = wifiManager;
	}
	
	/*
	 * 获取wifi下本机IP地址
	 * 
	 * @return 本机IP地址(返回null则失败)
	 */
	
	public String getLocalIp(){  	     
	    //检查Wifi状态    
	    if(!wifiManager.isWifiEnabled())  
	    	return null;  
	    WifiInfo wi=wifiManager.getConnectionInfo();  
	    //获取32位整型IP地址    
	    int ipAdd=wi.getIpAddress();  
	    //把整型地址转换成“*.*.*.*”地址    
	    String ip=intToIp(ipAdd);  
	    return ip;  
	}  

	private String intToIp(int i) {  
	    return (i & 0xFF ) + "." +  
	    ((i >> 8 ) & 0xFF) + "." +  
	    ((i >> 16 ) & 0xFF) + "." +  
	    ( i >> 24 & 0xFF) ;  
	} 
}
