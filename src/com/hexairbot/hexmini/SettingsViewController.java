
package com.hexairbot.hexmini;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
//import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ProgressBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hexairbot.hexmini.adapter.SettingsViewAdapter;
import com.hexairbot.hexmini.ble.BleConnectinManager;
import com.hexairbot.hexmini.ble.BleConnectinManagerDelegate;
//import com.hexairbot.hexmini.ipc.view.VideoSettingView;
import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.modal.OSDCommon;
import com.hexairbot.hexmini.modal.Transmitter;
import com.hexairbot.hexmini.ui.control.ViewPagerIndicator;
//import com.vmc.ipc.proxy.IpcProxy;
import com.hexairbot.hexmini.R;

public class SettingsViewController extends ViewController
        implements OnPageChangeListener,
        OnClickListener, BleConnectinManagerDelegate
{

    private static final String TAG = SettingsViewController.class.getSimpleName();
    
    private SettingsViewControllerDelegate delegate;

    private ProgressBar scanningProgressBar;
    
    private List<View> settingsViews;
    
    private TextView titleTextView;
    private TextView connectionStateTextView;
    private TextView scanningStateTextView;
    
    private ViewPager viewPager;
    private ImageButton preBtn;
    private ImageButton nextBtn;
    
    private Button backBtn;
    

    private Button magCalibrateBtn;
    
    private Button scanBtn;

    
    private CheckBox isLeftHandedCheckBox;
    private CheckBox isAccModeCheckBox;

    private CheckBox isBeginnerModeCheckBox;

    

    
    private ListView bleDeviceListView;

    private Resources res;

    private int[] titles;
    
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isScanning;
    
    private BleDeviceListAdapter bleDeviceListAdapter; 
   
    private BluetoothAdapter.LeScanCallback  mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            ((Activity)(SettingsViewController.this.context)).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	bleDeviceListAdapter.addDevice(device, rssi);
                	bleDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    private boolean bleAvailabed;
    
    
    LocalBroadcastManager mLocalBroadcastManager;


    
    public SettingsViewController(Context context, LayoutInflater inflater, ViewGroup container, SettingsViewControllerDelegate delegate)
    {	
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
    	
    	Transmitter.sharedTransmitter().getBleConnectionManager().setDelegate(this);
    	
    	isScanning = false;
    	
    	res = context.getResources();
    	this.context = context;
    	
    	this.delegate = delegate;
    	
    	titleTextView = (TextView) container.findViewById(R.id.titleTextView);
    	    	
        preBtn = (ImageButton)container.findViewById(R.id.preBtn);
        preBtn.setOnClickListener(this);
        
        nextBtn = (ImageButton)container.findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(this);
        

        
        


        
        titles = new int[] {
                R.string.settings_title_connection,
                R.string.settings_title_mode,
                R.string.settings_title_about
        };
    	
        backBtn = (Button)container.findViewById(R.id.backBtn);
        
        int[] pageIds = new int[]{
        		R.layout.settings_page_connection,
        		R.layout.settings_page_mode,
        		R.layout.settings_page_about
        };
        
        settingsViews = initPages(inflater, pageIds);
        
        viewPager = (ViewPager) container.findViewById(R.id.viewPager);
        viewPager.setAdapter(new SettingsViewAdapter(settingsViews));
        
        ViewPagerIndicator viewPagerIndicator = (ViewPagerIndicator) container.findViewById(R.id.pageIndicator);
        viewPagerIndicator.setViewPager(viewPager);
        viewPagerIndicator.setOnPageChangeListener(this);
        
        final int connectionPageIdx = 0;
        final int modePageIdx       = 1;
        final int aboutPageIdx      = 2;
        								
        scanBtn = (Button)settingsViews.get(connectionPageIdx).findViewById(R.id.scanBtn);
        bleDeviceListView = (ListView)settingsViews.get(connectionPageIdx).findViewById(R.id.bleDeviceListView);
        connectionStateTextView = (TextView)settingsViews.get(connectionPageIdx).findViewById(R.id.connectionStateTextView);
        connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
        scanningStateTextView = (TextView)settingsViews.get(connectionPageIdx).findViewById(R.id.scanningStateTextView);
        scanningStateTextView.setText(R.string.settings_item_scanning_anyflite);
        scanningStateTextView.setVisibility(View.INVISIBLE);
        scanningProgressBar = (ProgressBar)settingsViews.get(connectionPageIdx).findViewById(R.id.scanningProgressBar);
        scanningProgressBar.setVisibility(View.INVISIBLE);

        bleDeviceListAdapter = new BleDeviceListAdapter();
        
        BluetoothDevice currentDevice = Transmitter.sharedTransmitter().getBleConnectionManager().getCurrentDevice();
        
        if (currentDevice != null
        		&& Transmitter.sharedTransmitter().getBleConnectionManager().isConnected()) {
        	bleDeviceListAdapter.addDevice(currentDevice, 0);
        	connectionStateTextView.setText(R.string.settings_item_connection_state_conneceted);
		}
        
        bleDeviceListView.setAdapter(bleDeviceListAdapter);
        bleDeviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				final int selectedIdx = position;
				final BluetoothDevice targetDevice = bleDeviceListAdapter.getDevice(selectedIdx);
				
				BluetoothDevice currentDevice = Transmitter.sharedTransmitter().getBleConnectionManager().getCurrentDevice();
				
				if (currentDevice == targetDevice 
						&& Transmitter.sharedTransmitter().getBleConnectionManager().isConnected()) {
				     
					
					new AlertDialog.Builder(SettingsViewController.this.context)
						.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.dialog_title_info)
						.setMessage(R.string.dialog_disconnect)
						.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_DISARM);
								
								Handler handler = new Handler();
								
								handler.postDelayed(new Runnable() {
									
									@Override
									public void run() {
										Transmitter.sharedTransmitter().stop();
										
										Transmitter.sharedTransmitter().getBleConnectionManager().disconnect();
									}
								}, 10);
								
								connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
							}
						}).setNegativeButton(R.string.dialog_btn_no, null).show();
				}
				else{
					new AlertDialog.Builder(SettingsViewController.this.context)
					.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.dialog_title_info)
					.setMessage(R.string.dialog_connect)
					.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (isScanning) {
								if (bleAvailabed) {
									mBluetoothAdapter.stopLeScan(mLeScanCallback);
									
									isScanning = false;
									scanBtn.setText(R.string.btn_title_scan);
									scanningStateTextView.setVisibility(View.INVISIBLE);
									scanningProgressBar.setVisibility(View.INVISIBLE);
									
									Log.d(TAG, "stop scan");
								}
							}
							
							if(SettingsViewController.this.delegate != null){
								SettingsViewController.this.delegate.tryingToConnect(targetDevice.getName());
							}
							
							connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
							Transmitter.sharedTransmitter().getBleConnectionManager().connect(targetDevice); 
						}
					}).setNegativeButton(R.string.dialog_btn_no, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}).show();
				}
			}
		});
        
        
        scanBtn.setText(R.string.btn_title_scan);
        
     
        
            isLeftHandedCheckBox   = (CheckBox)settingsViews.get(modePageIdx).findViewById(R.id.isLeftHandedCheckBox);
            isAccModeCheckBox      = (CheckBox)settingsViews.get(modePageIdx).findViewById(R.id.isAccModeCheckBox);
            magCalibrateBtn = (Button)settingsViews.get(modePageIdx).findViewById(R.id.magCalibrateBtn);
        

        isBeginnerModeCheckBox = (CheckBox)settingsViews.get(modePageIdx).findViewById(R.id.isBeginnerModeCheckBox);


        WebView aboutWebView = (WebView)settingsViews.get(aboutPageIdx).findViewById(R.id.aboutWebView);
       
        
        String language = Locale.getDefault().getLanguage(); 
        
        if ("zh".equals(language)) {  
        	aboutWebView.loadUrl("file:///android_asset/About-zh.html");
        }
        else{
        	aboutWebView.loadUrl("file:///android_asset/About.html");
        }
        
        initListeners();
        
        updateSettingsUI();
        
        bleAvailabed = initBle();
        
        Log.d(TAG, "new settings view controller");
    }
    
	private boolean initBle() {
		if (mBluetoothAdapter == null) {
			if (!this.context.getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_BLUETOOTH_LE)) {
				Toast.makeText(this.context, R.string.ble_not_supported,
						Toast.LENGTH_SHORT).show();
				return false;
			}

			final BluetoothManager bluetoothManager = (BluetoothManager) this.context
					.getSystemService(Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = bluetoothManager.getAdapter();

			// Checks if Bluetooth is supported on the device.
			if (mBluetoothAdapter == null) {
				Toast.makeText(this.context, R.string.bluetooth_not_supported,
						Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		return true;
	}
    
    public void setBackBtnOnClickListner(OnClickListener listener) {
		backBtn.setOnClickListener(listener);
	}
    
    private void updateSettingsUI(){
        ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
      
        isLeftHandedCheckBox.setChecked(settings.isLeftHanded());
        isAccModeCheckBox.setChecked(settings.isAccMode());

        isBeginnerModeCheckBox.setChecked(settings.isBeginnerMode());

        

    }

    private void sendBleEnableRequest(){
    	if (mBluetoothAdapter != null) {
    		if (!mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    ((Activity)this.context).startActivityForResult(enableBtIntent, 1);
                }
            }
		} 
    }
    
    private List<View> initPages(LayoutInflater inflater, int[] pageIds)
    {
    	
        ArrayList<View> pageList = new ArrayList<View>(pageIds.length);
       
        for (int i = 0; i < pageIds.length; i++) {
        	

        		View view = inflater.inflate(pageIds[i], null);
        		Log.i("########","*****i:"+i);
        	
        		pageList.add(view);
        	}


        return pageList;
    }

    private void initListeners()
    {
    	scanBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				bleAvailabed = initBle();
				
				if (bleAvailabed) {
					if (isScanning) {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);

						isScanning = false;
						scanBtn.setText(R.string.btn_title_scan);
						scanningStateTextView.setVisibility(View.INVISIBLE);
						scanningProgressBar.setVisibility(View.INVISIBLE);

						Log.d("LeScanCallback", "stop scan");
					} 
					else {
						Log.d(TAG, "start scan");
						isScanning = true;

						Transmitter.sharedTransmitter().stop();
						
						BluetoothDevice currentDevice = Transmitter.sharedTransmitter().getBleConnectionManager().getCurrentDevice();
						if (currentDevice != null) {
							Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_DISARM);
							
							Handler handler = new Handler();
							
							handler.postDelayed(new Runnable() {
								
								@Override
								public void run() {
									Transmitter.sharedTransmitter().getBleConnectionManager().closeCurrentGatt();
								}
							}, 10);
						}

						connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);

						bleDeviceListAdapter.clear();
						bleDeviceListAdapter.notifyDataSetChanged();

						scanningStateTextView.setVisibility(View.VISIBLE);
						scanningProgressBar.setVisibility(View.VISIBLE);
						scanBtn.setText(R.string.btn_title_stop_scan);

						if (mBluetoothAdapter.startLeScan(mLeScanCallback)) {
							Log.d(TAG, "ble scan start successful");
						} 
						else {
							Log.d(TAG, "ble scan start");
						}
					}
				}
			}
		});
    	


    	
    	
   
    	magCalibrateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
		      	new AlertDialog.Builder(SettingsViewController.this.context)
				.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.dialog_title_info)
				.setMessage(R.string.dialog_calibrate_mag)
				.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_MAG_CALIBRATION);
					}
				}).setNegativeButton(R.string.dialog_btn_no, null).show();				
			}
		});
    	

    	
	
    	
        isLeftHandedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener(
        		) {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isLeftHanded) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setLeftHanded(isLeftHanded);
				settings.save();
				if (delegate != null) {
					delegate.leftHandedValueDidChange(isLeftHanded);
				}
				
			}
		});
        
        isAccModeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isAccMode) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setIsAccMode(isAccMode);
				settings.save();
				if (delegate != null) {
					delegate.accModeValueDidChange(isAccMode);
				}
			}
		});
        

        isBeginnerModeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isBeginnerMode) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setIsBeginnerMode(isBeginnerMode);
				settings.save();
				if (delegate != null) {
					delegate.beginnerModeValueDidChange(isBeginnerMode);
				}
			}
		});       
    }
    

    private void safeSetText(final TextView view, final String text)
    {
        if (view != null) {
            view.setText(text);
        }
    }


    public void onPageScrollStateChanged(int state)
    {
        // Left unimplemented
    }


    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {
        // Left unimplemented
    }


    public void onPageSelected(int position)
    {
        if (position == 0 && preBtn.getVisibility() != View.INVISIBLE) {
            preBtn.setVisibility(View.INVISIBLE);
        } else if (preBtn.getVisibility() != View.VISIBLE) {
            preBtn.setVisibility(View.VISIBLE);
        }

        if (nextBtn.getVisibility() != View.INVISIBLE && position == (viewPager.getAdapter().getCount() - 1)) {
            nextBtn.setVisibility(View.INVISIBLE);
        } else if (nextBtn.getVisibility() != View.VISIBLE) {
            nextBtn.setVisibility(View.VISIBLE);
        }

        if(titleTextView == null){
        	Log.d("Debug", "titleTextView is null");
        }
        
        titleTextView.setText(res.getString(titles[position]));
    }


    public void onClick(View v)
    {	
        switch (v.getId()) {
        case R.id.preBtn:
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
            break;
        case R.id.nextBtn:
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
            break;
        }
    }
    
    
	class BleDeviceListAdapter extends BaseAdapter{
        private ArrayList<BluetoothDevice> mLeDevices;
        private ArrayList<Integer> rssis;
        private ArrayList<byte[]> bRecord;
		
	    private LayoutInflater inflater;
		
		BleDeviceListAdapter() {
			super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            rssis = new ArrayList<Integer>();
            bRecord = new ArrayList<byte[]>();
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
        public void addDevice(BluetoothDevice device, int rs) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                rssis.add(rs);
              //  bRecord.add(record);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        
        public void clear() {
            mLeDevices.clear();
            rssis.clear();
            bRecord.clear();
        }
		
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			
				if (convertView == null) {
					row = inflater.inflate(R.layout.ble_device_list_row, null);
				}
				else {
					row = convertView;
				}	
		
				TextView bleDeviceNameTextView = (TextView)row.findViewById(R.id.bleDeviceNameTextView);
				
				 bleDeviceNameTextView.setTextColor(Color.WHITE);
				 bleDeviceNameTextView.setBackgroundColor(Color.BLUE);
				
				if ("Any Flite".equals(mLeDevices.get(position).getName())
						|| "Flexbot".equals(mLeDevices.get(position).getName())
						|| "FlexBLE".equals(mLeDevices.get(position).getName())) {
					bleDeviceNameTextView.setText("Flexbot");
				}
				else{
					bleDeviceNameTextView.setText(R.string.unknown);
				}
				 
							
				
				return row;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mLeDevices.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
	}


	@Override
	public void didConnect(BleConnectinManager manager) {
		// TODO Auto-generated method stub
		Toast.makeText(SettingsViewController.this.context, R.string.connection_successful, Toast.LENGTH_SHORT).show();
		Log.d(TAG, "didConnect"); 
		connectionStateTextView.setText(R.string.settings_item_connection_state_conneceted);
		Transmitter.sharedTransmitter().start();
		
		bleDeviceListView.setEnabled(false);
		scanBtn.setEnabled(false);
		
		Handler handler = new Handler();
		
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	bleDeviceListView.setEnabled(true);
        		scanBtn.setEnabled(true);
            }
        }, 3000);
		
		if(delegate != null){
			delegate.didConnect();
		}
	}

	@Override
	public void didDisconnect(BleConnectinManager manager) {
		Log.d(TAG, "didDisconnect");
		
		Transmitter.sharedTransmitter().stop();
		
		Toast.makeText(SettingsViewController.this.context, R.string.connection_lost, Toast.LENGTH_SHORT).show();	 
		connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
	
		bleDeviceListView.setEnabled(false);
		scanBtn.setEnabled(false);
		
		Handler handler = new Handler();
		
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	bleDeviceListView.setEnabled(true);
        		scanBtn.setEnabled(true);
            }
        }, 3000);
		
		if(delegate != null){
			delegate.didDisconnect();
		}
	}

	@Override
	 public void didReceiveData(BleConnectinManager manager, byte[] data) {
         // TODO Auto-generated method stub
         Log.d(TAG, "didReceiveData");

         Transmitter.sharedTransmitter().getOsdData().parseRawData(data);
	 }
	
	

	@Override
	public void didFailToConnect(BleConnectinManager manager) {
		// TODO Auto-generated method stub
		
		Toast.makeText(SettingsViewController.this.context, R.string.connection_failed, Toast.LENGTH_SHORT).show();
		connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
	
		if(delegate != null){
			delegate.didFailToConnect();
		}
	}
	
	

	
	@Override
	public void viewWillDisappear() {
		// TODO Auto-generated method stub
		super.viewWillDisappear();
		
		Log.d(TAG, "viewWillAppear()");

		if (isScanning) {
			if (bleAvailabed) {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);

				isScanning = false;
				scanBtn.setText(R.string.btn_title_scan);
				scanningStateTextView.setVisibility(View.INVISIBLE);
				scanningProgressBar.setVisibility(View.INVISIBLE);

				Log.d("LeScanCallback", "stop scan");
			}
		}
		

	}
	

}
