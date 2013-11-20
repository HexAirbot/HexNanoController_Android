package com.hexairbot.hexmini.ble;

import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class BleConnection {
	private final static String TAG = BleConnection.class.getSimpleName();
	
	public static final int FAILED_TO_CONNECT = 0;
	public static final int ACTION_CONNCET    = 1;
	
	private Handler handler;
	private Timer timer;
	private BluetoothDevice device;
	
	protected String deviceName;
	protected String deviceAddress;
	protected BleConnectionDelegate delegate;
	protected boolean isConnected;
	protected boolean isConnecting;
	protected boolean isTryingConnect;
	protected boolean isReadyToConnect;
	public boolean isReadyToConnect() {
		return isReadyToConnect;
	}

	protected Context context;
	
	protected BluetoothLeService mBluetoothLeService;
	
	 // Code to manage Service lifecycle.
    private  ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                mBluetoothLeService = null;
            }
            
            isReadyToConnect = true;
            Log.e(TAG, "mBluetoothLeService is okay");
            //sAutomatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	Log.e(TAG, "onServiceDisconnected");
            mBluetoothLeService = null;
            isReadyToConnect = false;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
            	Log.e(TAG, "Only gatt, just wait");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
                isConnected = false;
                if (delegate != null) {
					delegate.didDisconnect(BleConnection.this);
				}
                
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //可以开始干活了
            {
            	//Toast.makeText(BleConnection.this.context, "连接成功，现在可以正常通信！", Toast.LENGTH_SHORT).show();
            	
            	isConnected = true;
            	isConnecting = false;
            	
            	if (delegate != null) {
					delegate.didConnect(BleConnection.this);
				}
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
            	Log.e(TAG, "RECV DATA");
            	String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            	if (delegate != null) {
					delegate.didReceiveData(BleConnection.this, data);
				}
            }
            else{
            	Toast.makeText(BleConnection.this.context, "Unkonwn！", Toast.LENGTH_SHORT).show();	
            }
        }
    };

	public BleConnection(BluetoothDevice device,  Context context) {
		super();
		this.deviceName = device.getName();
		this.deviceAddress = device.getAddress();
		this.context = context;
		this.device = device;
		
		handler = new Handler(){
		public void handleMessage(Message msg) {  
		    switch (msg.what) {  
		            case FAILED_TO_CONNECT:  
		            	if (delegate != null) {
		            		delegate.didFailToConnect(BleConnection.this);
						}
		                break;
		            case ACTION_CONNCET:
		            	mBluetoothLeService.connect(BleConnection.this.deviceAddress);
		            	break;
		            }  
		        };  
		};
		
		mGattUpdateReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	            final String action = intent.getAction();
	            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  //连接成功
	            	Log.e(TAG, "Only gatt, just wait");
	            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { //断开连接
	                isConnected = false;
	                if (BleConnection.this.delegate != null) {
	                	BleConnection.this.delegate.didDisconnect(BleConnection.this);
					}
	                
	            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) //可以开始干活了
	            {
	            	//Toast.makeText(BleConnection.this.context, "连接成功，现在可以正常通信！", Toast.LENGTH_SHORT).show();
	            	
	            	isConnected = true;
	            	if (BleConnection.this.delegate != null) {
	            		BleConnection.this.delegate.didConnect(BleConnection.this);
					}
	            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { //收到数据
	            	Log.e(TAG, "RECV DATA");
	            	String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
	            	if (BleConnection.this.delegate != null) {
	            		BleConnection.this.delegate.didReceiveData(BleConnection.this, data);
					}
	            }
	            else{
	            	Toast.makeText(BleConnection.this.context, "Unkonwn！", Toast.LENGTH_SHORT).show();
	            }
	        }
	    };
		
        Intent gattServiceIntent = new Intent(this.context, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + this.context.bindService(gattServiceIntent, mServiceConnection, android.content.Context.BIND_AUTO_CREATE));
        
        this.context.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}
	
	public String getDeviceName() {
		return deviceName;
	}
	
	public String getDeviceAdress() {
		return deviceAddress;
	}
	
	private int connectionTimeCount = 0;
	
	public void connect(){
		Log.d(TAG, "try connect");
		
		
		
		if (isConnected()) {
			return;
		} 
		else {
			if (isTryingConnect == false) {
				isTryingConnect = true;

				connectionTimeCount = 0;

				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						if (isConnected()) {
							Log.d(TAG, "is Connect");
							timer.cancel();
							timer = null;
							isTryingConnect = false;
							isConnecting = false;
							return;
						}

						if (isReadyToConnect() && (isConnecting == false)) {
							Log.d(TAG, "isReadyToConnect");
							isConnecting = true;
							Message message = new Message();
							message.what = ACTION_CONNCET;
							handler.sendMessage(message);
							
							return;
						} 
						else {
							if (connectionTimeCount > 40) {
								connectionTimeCount = 0;
								isTryingConnect = false;
								isConnecting = false;
								timer.cancel();
								timer = null;

								Log.d(TAG, "connect timeout");

								// (Activity)(BleConnection.this.context).;

								Message message = new Message();
								message.what = FAILED_TO_CONNECT;
								handler.sendMessage(message);

								return;
							}
						}

						connectionTimeCount++;
					}
				}, 0, 250);
			}
			
		}
	}
	
	public void disconnect() {
		Log.d(TAG, "try disconnect");
		
		if (isConnected()) {
			if (mBluetoothLeService != null) {
				mBluetoothLeService.disconnect();
			}
			else {
				Log.d(TAG, "error impossible, mBluetoothLeService == null");
			}
			
			return;
		}
		
		if (isTryingConnect) {
			timer.cancel();
			timer = null;
			connectionTimeCount = 0;
			isTryingConnect = false;
			isConnecting = false;
		}
	}
	
	public void sendData(String data) {
		if (mBluetoothLeService != null && isConnected()) {
			mBluetoothLeService.WriteValue(data);	
		}
	}

	public boolean isConnected() {
		return isConnected;
	}

	public Context getContext() {
		return context;
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		releaseSource();
	}
	
	public void releaseSource(){
		disconnect();
		
		
		if (mGattUpdateReceiver != null) {
			this.context.unregisterReceiver(mGattUpdateReceiver);
			mGattUpdateReceiver = null;
		}
		
		if (mServiceConnection != null) {
			this.context.unbindService(mServiceConnection);
			mServiceConnection = null;
		}
        
        if(mBluetoothLeService != null)
        {
        	mBluetoothLeService.close();
        	mBluetoothLeService = null;
        }
        Log.d(TAG, "releaseSource");
	}
	
    private  IntentFilter makeGattUpdateIntentFilter() {                        //注册接收的事件
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

	public BleConnectionDelegate getDelegate() {
		return delegate;
	}

	public void setDelegate(BleConnectionDelegate delegate) {
		this.delegate = delegate;
	}

	public BluetoothDevice getDevice() {
		return device;
	}
}
