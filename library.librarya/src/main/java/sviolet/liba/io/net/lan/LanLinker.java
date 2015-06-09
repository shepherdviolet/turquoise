package sviolet.liba.io.net.lan;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@SuppressLint("HandlerLeak")
public abstract class LanLinker {
	
	ServerSocket serverSocket;
	
	private boolean looper_startListener;
	
	private static int SERVER_PORT = 17705;
	
	private final static int HANDLER_READ_DATA = 1;
	private final static int HANDLER_ERROR_READ = 2;
	private final static int HANDLER_ERROR_SEND = 3;
	private final static int HANDLER_ERROR_SEND_NO_TARGET = 4;
	private final static int HANDLER_INIT_FAIL = 5;
	
	public LanLinker() throws IOException{
		init();
	}
	
	public LanLinker(int port) throws IOException{
		SERVER_PORT = port;
		init();
	}
	
	private void init() throws IOException{
		serverSocket = new ServerSocket(SERVER_PORT);
		//serverSocket.setSoTimeout(5000);
		startListen();
	}
	
	public void stop(){
		looper_startListener = false;
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startListen(){
		
		looper_startListener = true;
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(looper_startListener){
					try {
						readData(serverSocket.accept());
					}catch (SocketTimeoutException te){
						
					}catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}).start();
		
	}
	
	private void readData(Socket SOCKET){
		
		final Socket socket = SOCKET;
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				ObjectInputStream objectInputStream = null;
				try {
					//socket.setSoTimeout(5000);
					objectInputStream = new ObjectInputStream(socket.getInputStream());
					Object object = objectInputStream.readObject();
					if(object != null){
						Bundle bundle = new Bundle();
						bundle.putString("ip", socket.getInetAddress().getHostAddress());
						
						Message msg = handler.obtainMessage();
						msg.arg1 = HANDLER_READ_DATA;
						msg.obj = object;
						msg.setData(bundle);
						msg.sendToTarget();
					}
				} catch (SocketTimeoutException te){
					te.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sendErrorMessage(HANDLER_ERROR_READ);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sendErrorMessage(HANDLER_ERROR_READ);
				}finally{
					if(objectInputStream != null)
						try {
							objectInputStream.close();
						} catch (IOException e) {
						}
					if(socket != null)
						try {
							socket.close();
						} catch (IOException e) {
						}
				}
			}
			
		}).start();
	}
	
	public void send(Object OBJECT,String IP){
		if(serverSocket != null)
			send(OBJECT,IP,serverSocket.getLocalPort());
		else
			sendErrorMessage(HANDLER_INIT_FAIL);
	}
	
	public void send(Object OBJECT,String IP,int PORT){
		
		final Object object = OBJECT;
		final String ip = IP;
		final int port = PORT;
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Socket socket = null;
				ObjectOutputStream objectOutputStream = null;
				try {
					socket = new Socket(ip,port);
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(object);
					objectOutputStream.flush();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sendErrorMessage(HANDLER_ERROR_SEND_NO_TARGET);
				} catch (ConnectException ce){
					ce.printStackTrace();
					sendErrorMessage(HANDLER_ERROR_SEND_NO_TARGET);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					sendErrorMessage(HANDLER_ERROR_SEND);
				}finally{
					if(objectOutputStream != null)
						try {
							objectOutputStream.close();
						} catch (IOException e) {
						}
					if(socket != null)
						try {
							socket.close();
						} catch (IOException e) {
						}
				}
			}
			
		}).start();
		
	}
	
	private void sendErrorMessage(int info){
		Message msg = handler.obtainMessage();
		msg.arg1 = info;
		msg.sendToTarget();
	}
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.arg1){
			case HANDLER_READ_DATA:
				Bundle bundle = msg.getData();
				onReceive(bundle.getString("ip"),msg.obj);
				break;
			case HANDLER_ERROR_READ:
				onReadError();
				break;
			case HANDLER_ERROR_SEND:
				onSendError();
				break;
			case HANDLER_ERROR_SEND_NO_TARGET:
				onNoTargetError();
				break;
			case HANDLER_INIT_FAIL:
				onInitFailError();
				break;
			default:
				
			}
		}
		
	};
	
	public abstract void onReceive(String targetIp,Object obj);
	
	public abstract void onReadError();
	
	public abstract void onSendError();
	
	public abstract void onNoTargetError();
	
	public abstract void onInitFailError();
}