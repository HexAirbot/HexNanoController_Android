package com.hexairbot.hexmini.modal;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;
import android.util.Log;
import com.hexairbot.hexmini.HexMiniApplication;
import com.hexairbot.hexmini.ble.BleConnectinManager;
import com.hexairbot.hexmini.modal.OSDCommon.MSPCommnand;
import com.hexairbot.hexmini.ui.Text;

public class Transmitter implements OSDDataDelegate{
	private static final int  CHANNEL_COUNT = 8;
	private static final int  FPS = 14; //max 17
	
	private static Transmitter sharedTransmitter; 
	private BleConnectinManager bleConnectionManager;
	private Timer timer;
	private byte dataPackage[] = new byte[11];
	private byte refinedDataPackage[] = new byte[6];
	private float[] channelList = new float[CHANNEL_COUNT];
	
	private OSDData osdData;
	
	public OSDData getOsdData() {
		return osdData;
	}

	public BleConnectinManager getBleConnectionManager() {
		return bleConnectionManager;
	}

	public void setBleConnectionManager(BleConnectinManager bleConnectionManager) {
		this.bleConnectionManager = bleConnectionManager;
	}

	private Transmitter(){
		super();
		//bleConnectionManager = new BleConnectinManager();
	}
	
	public static Transmitter sharedTransmitter(){
		if (sharedTransmitter == null) {
			sharedTransmitter = new Transmitter();
		}
		
		return sharedTransmitter;
	}
	
	Handler handler = new Handler();
	
	public void start(){
		stop();
		
		initDataPackage();
		
		
	    if (osdData == null) {
	        osdData = new OSDData();
	        osdData.setDelegate(this);
	    }

		
		timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						transmmit();						
					}
				});
				
			}
		}, 0, 1000 / FPS);
	}
	
	public void stop(){
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	public void transmmitData(byte[] data){
		if (bleConnectionManager != null && bleConnectionManager.isConnected() && data != null){
			if (HexMiniApplication.sharedApplicaion().isFullDuplex()) {
				bleConnectionManager.sendRequstData(data);
			}
			else{
				bleConnectionManager.sendControlData(data);
			}
		}
	}
	
	public boolean transmmitSimpleCommand(OSDCommon.MSPCommnand commnand){
		transmmitData(OSDCommon.getSimpleCommand(commnand));
		return true;
	}
	
	private int sendCnt;
	
	private void transmmit(){
		updateDataPackage();
	    if (bleConnectionManager != null && bleConnectionManager.isConnected() && dataPackage != null) {
	    	if (HexMiniApplication.sharedApplicaion().isFullDuplex()) {
		    	refinedDataPackage[0] = dataPackage[5];
		    	refinedDataPackage[1] = dataPackage[6];
		    	refinedDataPackage[2] = dataPackage[7];
		    	refinedDataPackage[3] = dataPackage[8];
		    	refinedDataPackage[4] = dataPackage[9];
		    	refinedDataPackage[5] = dataPackage[10];
		    	bleConnectionManager.sendControlData(refinedDataPackage);
			    
		    	sendCnt++;
			    if (sendCnt % 2 == 1){		    
			    	bleConnectionManager.sendRequstData(OSDCommon.getSimpleCommand(MSPCommnand.MSP_HEX_NANO));
			    }  
	    	}
	    	else{
	    		bleConnectionManager.sendControlData(dataPackage);	
	    	}
	}
		/*
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				Log.e("TEST", "handler.post");
				
				updateDataPackage();
			    if (bleConnectionManager != null && bleConnectionManager.isConnected() && dataPackage != null) {
					bleConnectionManager.sendData(dataPackage);
				}
			}
		});*/
	}
	
	private void initDataPackage(){
		dataPackage[0] = '$';
		dataPackage[1] = 'M';
		dataPackage[2] = '<';
		dataPackage[3] = 4;
		dataPackage[4] = (byte)(OSDCommon.MSPCommnand.MSP_SET_RAW_RC_TINY.value());
		
		updateDataPackage();
	}
	
	public void setChannel(int channeIdx, float value){
		channelList[channeIdx] = value;
	}
	
	int check = 0;

	//传输八个通道的数据，通道数据用5个字节来表示
    private void updateDataPackage(){
		byte checkSum = 0;
	    
	    int dataSizeIdx = 3;
	    int checkSumIdx = 10;
	    
	    dataPackage[dataSizeIdx] = 5;
	    
	    checkSum ^= (dataPackage[dataSizeIdx] & 0xFF);
	    checkSum ^= (dataPackage[dataSizeIdx + 1] & 0xFF);
	    
	    /*
	    if (check == 0) {
	    	channelList[0] = -0.5f; 
	    	check = 1;
		}
	    else{
	    	channelList[0] = 0.5f; 
	    	check = 0;
	    }*/
	    
	    
	    
	    for(int channelIdx = 0; channelIdx < CHANNEL_COUNT - 4; channelIdx++){
	        float scale =  channelList[channelIdx];
	        
	        
	        if (scale > 1) {
	            scale = 1;
	        }
	        else if(scale < -1){
	            scale = -1;
	        }
	        
	        byte pulseLen =  (byte) ((int)(Math.abs(500 + 500 * scale)) / 4);
	    
	        dataPackage[5 + channelIdx] = (byte) pulseLen;
	        
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
	    
	    dataPackage[5 + 4] = (byte) auxChannels;
	    checkSum ^= (dataPackage[5 + 4] & 0xFF);
	       
	    dataPackage[checkSumIdx] = (byte) checkSum;
	}

	@Override
	public void osdDataDidUpdateOneFrame() {
		Log.d("Test", "osdDataDidUpdateOneFrame");
		
		Text debugTextView = HexMiniApplication.sharedApplicaion().getDebugTextView();
		
		String debugString = "" + osdData.getAngleX() + " " + osdData.getAngleY();
		
		debugTextView.setText(debugString);	
	}
}
