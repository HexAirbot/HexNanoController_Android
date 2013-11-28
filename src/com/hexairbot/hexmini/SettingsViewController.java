
package com.hexairbot.hexmini;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
//import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.hexairbot.hexmini.R;
//import com.hexairbot.hexmini.drone.DroneConfig;
//import com.hexairbot.hexmini.drone.DroneConfig.EDroneVersion;
import com.hexairbot.hexmini.adapter.SettingsViewAdapter;
import com.hexairbot.hexmini.ble.BleConnectinManager;
import com.hexairbot.hexmini.ble.BleConnectinManagerDelegate;
import com.hexairbot.hexmini.ble.BleConnection;
import com.hexairbot.hexmini.ble.BleConnectionDelegate;
import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.modal.Transmitter;
//import  com.hexairbot.hexmini.ui.adapters.SettingsViewAdapter;
//import  com.hexairbot.hexmini.ui.controls.ViewPagerIndicator;
//import  com.hexairbot.hexmini.ui.filters.NetworkNameFilter;
//import  com.hexairbot.hexmini.ui.listeners.OnSeekChangedListener;
//import  com.hexairbot.hexmini.utils.FontUtils;
import com.hexairbot.hexmini.ui.control.ViewPagerIndicator;


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
    
    private Button defaultSettingsBtn;
    private Button accCalibrateBtn;
    private Button magCalibrateBtn;
    
    private Button scanBtn;
    private Button upTrimBtn;
    private Button downTrimBtn;
    private Button leftTrimBtn;
    private Button rightTrimBtn;
    
    private CheckBox isLeftHandedCheckBox;
    private CheckBox isAccModeCheckBox;
    private CheckBox isHeadfreeModeCheckBox;
    
    private TextView isLeftHandedTitleTextView;
    private TextView isAccModeTextTitleView;
    private TextView isHeadfreeTitleTextView;
    
    private TextView interfaceOpacityValueTextView;
    private TextView takeOffThrottleValueTextView;
    private TextView aileronAndElevatorDeadBandValueTextView;
    private TextView rudderDeadBandValueTextView;
    
    private SeekBar interfaceOpacitySeekBar;
    private SeekBar takeOffThrottleSeekBar;
    private SeekBar aileronAndElevatorDeadBandSeekBar;
    private SeekBar rudderDeadBandSeekBar;
    
    private OnSeekBarChangeListener interfaceOpacitySeekBarListener;
    private OnSeekBarChangeListener takeOffThrottleSeekBarListener;
    private OnSeekBarChangeListener aileronAndElevatorDeadBandSeekBarListener;
    private OnSeekBarChangeListener rudderDeadBandSeekBarListener;
    
    private ListView bleDeviceListView;

    private Resources res;

    private int[] titles;
    
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isScanning;
    
    private BleDeviceListAdapter bleDeviceListAdapter; 
   
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private boolean bleAvailabed;

    
    public SettingsViewController(Context context, LayoutInflater inflater, ViewGroup container, SettingsViewControllerDelegate delegate)
    {	
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
        
        defaultSettingsBtn = (Button)container.findViewById(R.id.defaultSettingsBtn);
        accCalibrateBtn = (Button)container.findViewById(R.id.accCalibrateBtn);
        magCalibrateBtn = (Button)container.findViewById(R.id.magCalibrateBtn);

        titles = new int[] {
                R.string.settings_title_connection,
                R.string.settings_title_personal,
                R.string.settings_title_angel_trim,
                R.string.settings_title_mode,
                R.string.settings_title_about
        };
    	
        backBtn = (Button)container.findViewById(R.id.backBtn);
        
        int[] pageIds = new int[]{
        		R.layout.settings_page_connection,
        		R.layout.settings_page_personal,
        		R.layout.settings_page_angel_trim,
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
        final int interfacePageIdx  = 1;
        final int angelTrimPageIdx  = 2;
        final int modePageIdx       = 3;
        final int aboutPageIdx      = 4;
      
        scanBtn = (Button)settingsViews.get(connectionPageIdx).findViewById(R.id.scanBtn);
        bleDeviceListView = (ListView)settingsViews.get(connectionPageIdx).findViewById(R.id.bleDeviceListView);
        connectionStateTextView = (TextView)settingsViews.get(connectionPageIdx).findViewById(R.id.connectionStateTextView);
        connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
        scanningStateTextView = (TextView)settingsViews.get(connectionPageIdx).findViewById(R.id.scanningStateTextView);
        scanningStateTextView.setText(R.string.settings_item_scanning_hex_mini);
        scanningStateTextView.setVisibility(View.INVISIBLE);
        scanningProgressBar = (ProgressBar)settingsViews.get(connectionPageIdx).findViewById(R.id.scanningProgressBar);
        scanningProgressBar.setVisibility(View.INVISIBLE);

        bleDeviceListAdapter = new BleDeviceListAdapter();
        
        BleConnection connection = Transmitter.sharedTransmitter().getBleConnectionManager().getCurrentConnection();
        
        if (connection != null
        		&& connection.isConnected()) {
        	bleDeviceListAdapter.addDevice(connection.getDevice(), 0);
        	connectionStateTextView.setText(R.string.settings_item_connection_state_conneceted);
		}
        
        bleDeviceListView.setAdapter(bleDeviceListAdapter);
        bleDeviceListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				final int selectedIdx = position;
				final BluetoothDevice bleDevice = bleDeviceListAdapter.getDevice(selectedIdx);
				
				BleConnection bleConnection = Transmitter.sharedTransmitter().getBleConnectionManager().getCurrentConnection();
				
				final String deviceName = bleDevice.getName();
				final String deviceAddress = bleDevice.getAddress();
				
				if (bleConnection != null
						&& bleConnection.getDeviceAdress().equals(deviceAddress)
						&& bleConnection.getDeviceName().equals(deviceName)
						&& bleConnection.isConnected()) {
				      	new AlertDialog.Builder(SettingsViewController.this.context)
						.setIcon(android.R.drawable.ic_dialog_alert).setTitle("提示")
						.setMessage("断开连接?")
						.setPositiveButton("是", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								Transmitter.sharedTransmitter().getBleConnectionManager().disconnect();
								connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
							}
						}).setNegativeButton("否", null).show();
				}
				else{
					new AlertDialog.Builder(SettingsViewController.this.context)
					.setIcon(android.R.drawable.ic_dialog_alert).setTitle("提示")
					.setMessage("连接?")
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (isScanning) {
								if (bleAvailabed) {
									mBluetoothAdapter.stopLeScan(mLeScanCallback);
									
									isScanning = false;
									scanBtn.setText(R.string.btn_title_scan);
									//connectionStateTextView.setText(R.string.s)
									scanningStateTextView.setVisibility(View.INVISIBLE);
									scanningProgressBar.setVisibility(View.INVISIBLE);
									
									Log.d("LeScanCallback", "stop scan");
								}
							}
							
							Transmitter.sharedTransmitter().getBleConnectionManager().disconnect();
							connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
							
							BleConnection bleConnection = new BleConnection(bleDevice, SettingsViewController.this.getContext());
							Transmitter.sharedTransmitter().getBleConnectionManager().connect(bleConnection); 
							 //new ConnectionAsyncTask(deviceName, deviceAddress).execute();
						}
					}).setNegativeButton("否", null).show();
				}
			}
		});
        
        upTrimBtn   = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.upTrimBtn);
        downTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.downTrimBtn);
        leftTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.leftTrimBtn);
        rightTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.rightTrimBtn);
        
        scanBtn.setText(R.string.btn_title_scan);
        upTrimBtn.setText(R.string.btn_title_up_trim);
        downTrimBtn.setText(R.string.btn_title_down_trim);
        leftTrimBtn.setText(R.string.btn_title_left_trim);
        rightTrimBtn.setText(R.string.btn_title_right_trim);
        
        isLeftHandedCheckBox   = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isLeftHandedCheckBox);
        isAccModeCheckBox      = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isAccModeCheckBox);
        isHeadfreeModeCheckBox = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isHeadfreeModeCheckBox);
        
        isLeftHandedTitleTextView = (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.isLeftHandedTitleTextView);
        isAccModeTextTitleView    = (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.isAccModeTitleTextView);
        isHeadfreeTitleTextView   = (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.isHeadfreeModeTitleTextView);
        
        isLeftHandedTitleTextView.setText(R.string.settings_item_left_handed);
        isAccModeTextTitleView.setText(R.string.settings_item_acc_mode);
        isHeadfreeTitleTextView.setText(R.string.settings_item_headfree_mode);
        
        interfaceOpacityValueTextView =  (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.interfaceOpacityValueTextView);
        takeOffThrottleValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.takeOffThrottleValueTextView);
        aileronAndElevatorDeadBandValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.aileronAndElevatorDeadBandValueTextView);
        rudderDeadBandValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.rudderDeadBandValueTextView);
        
        interfaceOpacitySeekBar = (SeekBar)settingsViews.get(interfacePageIdx).findViewById(R.id.interfaceOpacitySeekBar);
        takeOffThrottleSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.takeOffThrottleSeekBar);
        aileronAndElevatorDeadBandSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.aileronAndElevatorDeadBandSeekBar);
        rudderDeadBandSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.rudderDeadBandSeekBar);
        
        interfaceOpacitySeekBar.setMax(100);
        takeOffThrottleSeekBar.setMax(1000);
        aileronAndElevatorDeadBandSeekBar.setMax(100);
        rudderDeadBandSeekBar.setMax(100);
        
        WebView aboutWebView = (WebView)settingsViews.get(aboutPageIdx).findViewById(R.id.aboutWebView);
        aboutWebView.getSettings().setJavaScriptEnabled(true);  
        aboutWebView.loadUrl("file:///android_asset/About.html");
        
        initListeners();
        
        updateSettingsUI();
        
        bleAvailabed = initBle();
        
        Log.d(TAG, "new settings view controller");
    }
    
    private boolean initBle(){
        if (!this.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this.context, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }
        
        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this.context, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            return false;
        }
        
       // sendBleEnableRequest();
        
        // Device scan callback.
        mLeScanCallback =
               new BluetoothAdapter.LeScanCallback() {
           @Override
           public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
               ((Activity)(SettingsViewController.this.context)).runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                   	 Log.d("LeScanCallback", "runOnUiThread");
                   	bleDeviceListAdapter.addDevice(device, rssi);
                   	bleDeviceListAdapter.notifyDataSetChanged();
                   }
               });
           }
       };
       
       return true;
    }
    
    public void setBackBtnOnClickListner(OnClickListener listener) {
		backBtn.setOnClickListener(listener);
	}
    
    private void updateSettingsUI(){
        ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
      
        isLeftHandedCheckBox.setChecked(settings.isLeftHanded());
        isAccModeCheckBox.setChecked(settings.isAccMode());
        isHeadfreeModeCheckBox.setChecked(settings.isHeadFreeMode());
        
        interfaceOpacitySeekBar.setProgress((int)(settings.getInterfaceOpacity() * 100));
        safeSetText(interfaceOpacityValueTextView, interfaceOpacitySeekBar.getProgress() + "%");
        
        takeOffThrottleSeekBar.setProgress((int)(settings.getTakeOffThrottle() - 1000));
        safeSetText(takeOffThrottleValueTextView, (takeOffThrottleSeekBar.getProgress() + 1000) + "us");
        
        aileronAndElevatorDeadBandSeekBar.setProgress((int)(settings.getAileronDeadBand() * 100));
        safeSetText(aileronAndElevatorDeadBandValueTextView, aileronAndElevatorDeadBandSeekBar.getProgress() + "%");
        
        rudderDeadBandSeekBar.setProgress((int)(settings.getRudderDeadBand() * 100));
        safeSetText(rudderDeadBandValueTextView, rudderDeadBandSeekBar.getProgress() + "%");  	
    }

    private void sendBleEnableRequest(){
    	if (mBluetoothAdapter != null) {
    		if (!mBluetoothAdapter.isEnabled()) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    ((Activity)this.context).startActivityForResult(enableBtIntent, HudActivity.REQUEST_ENABLE_BT);
                }
            }
		} 
    }
    
    private List<View> initPages(LayoutInflater inflater, int[] pageIds)
    {
        ArrayList<View> pageList = new ArrayList<View>(pageIds.length);

        for (int i = 0; i < pageIds.length; ++i) {
            View view = inflater.inflate(pageIds[i], null);
            //FontUtils.applyFont(inflater.getContext(), (ViewGroup) view);
            pageList.add(view);
        }

        return pageList;
    }


    private void initListeners()
    {
    	scanBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (isScanning) {
					if (bleAvailabed) {
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						
						isScanning = false;
						scanBtn.setText(R.string.btn_title_scan);
						//connectionStateTextView.setText(R.string.s)
						scanningStateTextView.setVisibility(View.INVISIBLE);
						scanningProgressBar.setVisibility(View.INVISIBLE);
						
						Log.d("LeScanCallback", "stop scan");
					}
				}
				else{
					if (bleAvailabed) {
				        BleConnection connection = Transmitter.sharedTransmitter().getBleConnectionManager().getCurrentConnection();
				        
				        if (connection != null
				        		&& connection.isConnected()) {
				        	Transmitter.sharedTransmitter().getBleConnectionManager().getCurrentConnection().disconnect();
				        	connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
				        }
						
						bleDeviceListAdapter.clear();
						bleDeviceListAdapter.notifyDataSetChanged();
						Log.d("LeScanCallback", "start scan");
						if (mLeScanCallback == null) {
							Log.d("LeScanCallback", "null");
						}
						scanningStateTextView.setVisibility(View.VISIBLE);
						scanningProgressBar.setVisibility(View.VISIBLE);
					 
						isScanning = true;
						scanBtn.setText(R.string.btn_title_stop_scan);
						//mBluetoothAdapter.stopLeScan(mLeScanCallback);
					
						if(mBluetoothAdapter.startLeScan(mLeScanCallback)){
							Log.d(TAG, "ble scan start successful");
						}
						else{
							Log.d(TAG, "ble scan start failed, try again");
							mBluetoothAdapter.startLeScan(mLeScanCallback);
						}
					}
				}
			}
		});
    	
    	upTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	downTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	leftTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	rightTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	magCalibrateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	accCalibrateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
    	
    	defaultSettingsBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();

				if (delegate != null) {
					delegate.leftHandedValueDidChange(settings.isLeftHanded());
					delegate.accModeValueDidChange(settings.isAccMode());
					delegate.headfreeModeValueDidChange(settings.isHeadFreeMode());
				}
				
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
        
        isHeadfreeModeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isHeadfree) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setIsHeadFreeMode(isHeadfree);
				settings.save();
				if (delegate != null) {
					delegate.headfreeModeValueDidChange(isHeadfree);
				}
			}
		});
    	
    	interfaceOpacitySeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setInterfaceOpacity(seekBar.getProgress() / 100.0f);
				settings.save();
				
				if (delegate != null) {
					delegate.interfaceOpacityValueDidChange(settings.getInterfaceOpacity() * 100);
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(interfaceOpacityValueTextView, progress + "%");
			}
		};
		interfaceOpacitySeekBar.setOnSeekBarChangeListener(interfaceOpacitySeekBarListener);
    	
    	takeOffThrottleSeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setTakeOffThrottle(seekBar.getProgress() + 1000);
				settings.save();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(takeOffThrottleValueTextView, (progress + 1000) + "us");
			}
		};
		takeOffThrottleSeekBar.setOnSeekBarChangeListener(takeOffThrottleSeekBarListener);
		
    	aileronAndElevatorDeadBandSeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setAileronDeadBand(seekBar.getProgress() / 100.f);
				settings.setElevatorDeadBand(settings.getAileronDeadBand());
				settings.save();
				
				if (delegate != null) {
					delegate.aileronAndElevatorDeadBandValueDidChange(settings.getAileronDeadBand());
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(aileronAndElevatorDeadBandValueTextView, progress + "%");
			}
		};
		aileronAndElevatorDeadBandSeekBar.setOnSeekBarChangeListener(aileronAndElevatorDeadBandSeekBarListener);
		
    	rudderDeadBandSeekBarListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setRudderDeadBand(seekBar.getProgress() / 100.f);
				settings.save();
				
				if (delegate != null) {
					delegate.rudderDeadBandValueDidChange(settings.getRudderDeadBand());
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
              safeSetText(rudderDeadBandValueTextView, progress + "%");
			}
		};
		rudderDeadBandSeekBar.setOnSeekBarChangeListener(rudderDeadBandSeekBarListener);
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
				TextView bleDeviceAddressTextView = (TextView)row.findViewById(R.id.bleDeviceAddressTextView);
			
				 bleDeviceNameTextView.setTextColor(Color.BLACK);
				 bleDeviceAddressTextView.setTextColor(Color.BLACK);
				 	 
				bleDeviceNameTextView.setText(mLeDevices.get(position).getName());
				bleDeviceAddressTextView.setText(mLeDevices.get(position).getAddress());
				
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
	public void didConnect(BleConnectinManager manager, BleConnection connection) {
		// TODO Auto-generated method stub
		Toast.makeText(SettingsViewController.this.context, "连接成功!", Toast.LENGTH_LONG).show();
		Log.d(TAG, "didConnect"); 
		connectionStateTextView.setText(R.string.settings_item_connection_state_conneceted);
	}

	@Override
	public void didDisconnect(BleConnectinManager manager,
			BleConnection connection) {
		// TODO Auto-generated method stub
		
		Log.d(TAG, "didDisconnect");
		
		if (SettingsViewController.this.context == null) {
			Log.d(TAG, "SettingsViewController context is null");
		}
		
		Toast.makeText(SettingsViewController.this.context, "失去连接!", Toast.LENGTH_LONG).show();	 
		connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
	}

	@Override
	public void didReceiveData(BleConnectinManager manager,
			BleConnection connection, String data) {
		// TODO Auto-generated method stub
		Log.d(TAG, "didReceiveData");
		
		if(data != null){
			Log.d(TAG, data);	
		}
	}

	@Override
	public void didFailToConnect(BleConnectinManager manager,
			BleConnection connection) {
		// TODO Auto-generated method stub
		
		Toast.makeText(SettingsViewController.this.context, "连接失败!", Toast.LENGTH_LONG).show();
		connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
	}


//	@Override
//	public void didConnect(BleConnection connection) {
//		if (bleConnection != connection) {
//			Log.d(TAG, "old connection, ignore, didConnect"); 
//		}
//		Log.d(TAG, "didConnect"); 
//	}

//	@Override
//	public void didDisconnect(BleConnection connection) {
//		if (bleConnection != connection) {
//			Log.d(TAG, "old connection, ignore, didDisconnect"); 
//		}
//		Log.d(TAG, "didDisconnect"); 
//		
//		connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
//	}

//	@Override
//	public void didReceiveData(BleConnection connection, String data) {
//		if (bleConnection != connection) {
//			Log.d(TAG, "old connection, ignore, didReceiveData"); 
//		}
//		
//		if (data == null) {
//			Log.d(TAG, "didReceiveData:null"); 
//		}
//		else{
//			Log.d(TAG, "didReceiveData:" + data); 
//		}
//	}
	
/*	
	class ConnectionAsyncTask extends AsyncTask<String, Long, Boolean>{
    	private Dialog progressDialog;
    	private String errorInfo;
    	private Map<String, Object> resultMap;
    	private String deviceName;
    	private String deviceAddress;
    	
    	public ConnectionAsyncTask(String deviceName, String deviceAddress){
    		super();
    		this.deviceName = deviceName;
    		this.deviceAddress = deviceAddress;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		
    		//resultTextView.setText(null);
    		progressDialog = new Dialog(SettingsViewController.this.context);
    		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    		progressDialog.setContentView(R.layout.progress_dialog);
    		progressDialog.setCancelable(false);
    		Button cancelBtn = (Button)progressDialog.findViewById(R.id.cancelBtn);
    		cancelBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
    				progressDialog.dismiss();
    				ConnectionAsyncTask.this.cancel(true);
				}
			});
    		
            RotateAnimation rotation = new RotateAnimation(
          	      0f,
          	      360f,
          	      Animation.RELATIVE_TO_SELF,
          	      0.5f,
          	      Animation.RELATIVE_TO_SELF,
          	      0.5f);
			rotation.setDuration(1300);
			rotation.setInterpolator(new LinearInterpolator());
			rotation.setRepeatMode(Animation.RESTART);
			rotation.setRepeatCount(Animation.INFINITE);
          	progressDialog.findViewById(R.id.activityIndicator).startAnimation(rotation);
    		
    		progressDialog.show();
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... params) {
    		int count = 0;
    		boolean isTryingToConnect =  false;
    		
    		while (true) {
    			boolean isReadyToConnect = bleConnection.isReadyToConnect();
    			if (isReadyToConnect) {
    				if (isTryingToConnect == false) {
        				isTryingToConnect = true;
        				bleConnection.connect();
					}
    				else{
    					if (bleConnection.isConnected()) {
							return true;
						}
    				}
				}

    			try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			count++;
    			
    			if (count > 40) { //10s
					return false;
				}
			}
    	}
    	
    	@Override
    	protected void onCancelled() {
    		super.onCancelled();
    	}
    	
    	@Override
    	protected void onPostExecute(Boolean successes){
    		super.onPostExecute(successes);
    		progressDialog.dismiss();
    		
    		if(successes.booleanValue()){
    			Toast.makeText(SettingsViewController.this.context, "连接成功!", Toast.LENGTH_LONG).show();
    			connectionStateTextView.setText(R.string.settings_item_connection_state_conneceted);
    		}
    		else{
    			Toast.makeText(SettingsViewController.this.context, "连接失败!", Toast.LENGTH_LONG).show();
    			connectionStateTextView.setText(R.string.settings_item_connection_state_not_conneceted);
    			return;
    		}
    	}
    }*/
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		
		/*
		if (bleAvailabed) {
			if (isScanning) {

		}
		*/
	}
	
	@Override
	public void viewWillAppear() {
		// TODO Auto-generated method stub
		super.viewWillAppear();
		sendBleEnableRequest();
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
				// connectionStateTextView.setText(R.string.s)
				scanningStateTextView.setVisibility(View.INVISIBLE);
				scanningProgressBar.setVisibility(View.INVISIBLE);

				Log.d("LeScanCallback", "stop scan");
			}
		}
	}
}
