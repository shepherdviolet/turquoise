package sviolet.liba.io.net.lan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class LanBroadcast {
	
	private static final int PACKET_SIZE = 128;
	
	private static String BROADCAST_IP = "230.0.0.7";
    private static int BROADCAST_PORT = 17706;
    
    private String localIp;
    private String Id = "unknow";
    private HashMap<String,String> ipMap = new HashMap<String, String>();
    private HashMap<String,String> ipMapNew = new HashMap<String, String>();
    private boolean looper_startListen;
    private boolean looper_startOnlineCast;
    private boolean looper_startMapFresh;
    
    MulticastSocket multicastSocket;
    InetAddress broadcastAddress;
    DatagramSocket sendSocket;
    
    WifiIpManager wifiIpManager;
    
    public LanBroadcast(WifiIpManager wifiIpManager) throws Exception{
    	init(wifiIpManager);
    }
    
    public LanBroadcast(WifiIpManager wifiIpManager,String broadcastIp,int broadcastPort) throws Exception{
    	BROADCAST_IP = broadcastIp;
    	BROADCAST_PORT = broadcastPort;
    	init(wifiIpManager);
    }
    
    private void init(WifiIpManager wifiIpManager) throws Exception{
    	this.wifiIpManager = wifiIpManager;
    	if(this.wifiIpManager == null)
    		throw new Exception("Exception:wifiIpManager is null");
    	
    	multicastSocket=new MulticastSocket(BROADCAST_PORT); 
    	broadcastAddress = InetAddress.getByName(BROADCAST_IP);
    	multicastSocket.joinGroup(broadcastAddress);//必须joinGroup才能收到广播
    	sendSocket = new DatagramSocket();
    	multicastSocket.setSoTimeout(5000);
    	
    	freshLocalIp();
    }
    
    private void startListen(){
    	
    	looper_startListen = true;
    	
    	new Thread(new Runnable(){

			@Override
			public void run() {
				DatagramPacket packet;
				String[] msg;
				while(looper_startListen){
					try {
						packet = new DatagramPacket(new byte[PACKET_SIZE],PACKET_SIZE);
						multicastSocket.receive(packet);
						msg = new String(packet.getData(),0,packet.getLength()).split("@");
						if(!msg[1].equals(localIp)){
							if(msg[0].equals("online")){
								ipMap.put(msg[1],msg[2]);
								ipMapNew.put(msg[1],msg[2]);
								replyCast(msg[1]);
							}else if(msg[0].equals("reply")){
								ipMap.put(msg[1],msg[2]);
								ipMapNew.put(msg[1],msg[2]);
							}
						}
					} catch (SocketTimeoutException te){
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
    		
    	}).start();
    	
    }
    
    private void startOnlineCast(){
    	
    	looper_startOnlineCast = true;
    	
    	new Thread(new Runnable(){

			@Override
			public void run() {
				while(looper_startOnlineCast){
					freshLocalIp();
					onlineCast();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
    		
    	}).start();
    	
    }
    
    private void startMapFresh(){
    	looper_startMapFresh = true;
    	
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				HashMap<String,String> map;
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				while(looper_startMapFresh){
					map = ipMap;
					ipMap = ipMapNew;
					map.clear();
					ipMapNew = map;
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
    }
    
    private void cast(String title,InetAddress addr){
    	
    	final String TITLE = title;
    	final InetAddress ADDR = addr;
    	
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				byte[] bytes = new byte[PACKET_SIZE];
				bytes = (TITLE+"@"+localIp+"@"+Id).getBytes();
				DatagramPacket packet = new DatagramPacket(bytes,bytes.length,ADDR,BROADCAST_PORT);
				try {
					sendSocket.send(packet);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}).start();
    }
    
    private void onlineCast(){
    	
		cast("online",broadcastAddress);

    }
    
    private void replyCast(String ip){
    	
    	InetAddress addr;
    	
		try {
			addr = InetAddress.getByName(ip);
			cast("reply",addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	
    }
    
    public void start(){
    	startListen();
    	startOnlineCast();
    	startMapFresh();
    }
    
    public void stop(){
    	
    	looper_startListen = false;
    	looper_startOnlineCast = false;
    	looper_startMapFresh = false;
    	
    	if(multicastSocket != null){
    		try {
				multicastSocket.leaveGroup(broadcastAddress);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		multicastSocket.close();
    	}
    	if(sendSocket != null)
    		sendSocket.close();
    }
    
    private void freshLocalIp(){
    	localIp = wifiIpManager.getLocalIp();
    }
    
    public ArrayList<String> getIpList(){
    	@SuppressWarnings("unchecked")
		Map<String,String> map =(Map<String, String>) ipMap.clone();
    	ArrayList<String> ipList = new ArrayList<String>();
    	Iterator<Entry<String, String>> iter = map.entrySet().iterator();
    	while (iter.hasNext()) {
    		Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
    		ipList.add(entry.getKey());
    	}
		return ipList;
    }
    
    public ArrayList<String> getIdList(){
    	@SuppressWarnings("unchecked")
		Map<String,String> map =(Map<String, String>) ipMap.clone();
    	ArrayList<String> idList = new ArrayList<String>();
    	Iterator<Entry<String, String>> iter = map.entrySet().iterator();
    	while (iter.hasNext()) {
    		Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
    		idList.add(entry.getValue());
    	}
		return idList;
    }

    public ArrayList<HashMap<String,String>> getUserList(){
    	@SuppressWarnings("unchecked")
		Map<String,String> map =(Map<String, String>) ipMap.clone();
    	ArrayList<HashMap<String,String>> userList = new ArrayList<HashMap<String,String>>();
    	Iterator<Entry<String, String>> iter = map.entrySet().iterator();
    	while (iter.hasNext()) {
    		Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
    		HashMap<String,String> MapTemp = new HashMap<String, String>();
    		MapTemp.put("ip", entry.getKey());
    		MapTemp.put("id", entry.getValue());
    		userList.add(MapTemp);
    	}
		return userList;
    }
    
	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}
    
}
