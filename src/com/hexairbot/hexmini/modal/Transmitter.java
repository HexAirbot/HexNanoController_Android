package com.hexairbot.hexmini.modal;

import java.util.Timer;
import java.util.TimerTask;

import com.hexairbot.hexmini.ble.BleConnectinManager;

public class Transmitter {
	private static final int  CHANNEL_COUNT = 8;
	
	private static Transmitter sharedTransmitter; 
	private BleConnectinManager bleConnectionManager;
	private Timer timer;
	private char dataPackage[] = new char[22];
	private float[] channelList = new float[CHANNEL_COUNT];
	
	public BleConnectinManager getBleConnectionManager() {
		return bleConnectionManager;
	}

	private Transmitter(){
		super();
		bleConnectionManager = new BleConnectinManager();
	}
	
	public static Transmitter sharedTransmitter(){
		if (sharedTransmitter == null) {
			sharedTransmitter = new Transmitter();
		}
		
		return sharedTransmitter;
	}
	
	public void start(){
		stop();
		
		initDataPackage();
		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				transmmit();
			}
		}, 0, 40);
	}
	
	public void stop(){
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	public void transmmitData(String data){
		bleConnectionManager.sendData(data);
	}
	
	public boolean transmmitSimpleCommand(String data){
		
		return true;
	}
	
	private void transmmit(){
		updateDataPackage();
	    
	    String data = null;

	    if (data == null) {
	        data = new String(dataPackage, 0, 11);
	    }
	    else{
	    	data += new String(dataPackage, 0, 11);
	    }
	    
	    if (bleConnectionManager.isConnected() && data != null) {
			bleConnectionManager.sendData(data);
		}
	}
	
	private void initDataPackage(){
		dataPackage[0] = '$';
		dataPackage[1] = 'M';
		dataPackage[2] = '<';
		dataPackage[3] = 4;
		dataPackage[4] = (char)(OSDCommon.MSPCommnand.MSP_SET_RAW_RC_TINY.value());
		
		updateDataPackage();
	}
	
	public void setChannel(int channeIdx, float value){
		channelList[channeIdx] = value;
	}

	//传输八个通道的数据，通道数据用5个字节来表示
    private void updateDataPackage(){
		byte checkSum = 0;
	    
	    int dataSizeIdx = 3;
	    int checkSumIdx = 10;
	    
	    dataPackage[dataSizeIdx] = 5;
	    
	    checkSum ^= (dataPackage[dataSizeIdx] & 0xFF);
	    checkSum ^= (dataPackage[dataSizeIdx + 1] & 0xFF);
	    
	    for(int channelIdx = 0; channelIdx < CHANNEL_COUNT - 4; channelIdx++){
	        float scale =  dataPackage[channelIdx];
	        
	        
	        if (scale > 1) {
	            scale = 1;
	        }
	        else if(scale < -1){
	            scale = -1;
	        }
	        
	        byte pulseLen =  (byte) ((int)(Math.abs(500 + 500 * scale)) / 4);
	    
	        dataPackage[5 + channelIdx] = (char) pulseLen;

	//蓝牙延迟测试
//	        if(channelIdx ==0){
//	            static int len = 0;
//	            package[5 + channelIdx] = len++;
//	            if (len > 250) {
//	                len = 0;
//	            }
//	        }
	        
	        checkSum ^= (dataPackage[5 + channelIdx] & 0xFF);
	    }
	    
	    byte auxChannels = 0x00;
	    
	    float aux1Scale = channelList[4];
	    
	    if (aux1Scale < -0.666) {
	        auxChannels |= 0x00;
	    }
	    else if(aux1Scale < 0.3333){
	        auxChannels |= 0x40;
	    }
	    else{
	        auxChannels |= 0x80;
	    }
	    
	    float aux2Scale = channelList[5];
	    
	    if (aux2Scale < -0.666) {
	        auxChannels |= 0x00;
	    }
	    else if(aux2Scale < 0.3333){
	        auxChannels |= 0x10;
	    }
	    else{
	        auxChannels |= 0x20;
	    }
	    
	    float aux3Scale = channelList[6];
	    
	    if (aux3Scale < -0.666) {
	        auxChannels |= 0x00;
	    }
	    else if(aux3Scale < 0.3333){
	        auxChannels |= 0x04;
	    }
	    else{
	        auxChannels |= 0x08;
	    }
	    
	    float aux4Scale = channelList[7];
	    
	    if (aux4Scale < -0.666) {
	        auxChannels |= 0x00;
	    }
	    else if(aux4Scale < 0.3333){
	        auxChannels |= 0x01;
	    }
	    else{
	        auxChannels |= 0x02;
	    }
	    
	    dataPackage[5 + 4] = (char) auxChannels;
	    checkSum ^= (dataPackage[5 + 4] & 0xFF);
	       
	    dataPackage[checkSumIdx] = (char) checkSum;
	}
}
