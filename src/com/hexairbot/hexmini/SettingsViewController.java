
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
import android.content.IntentFilter;
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
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hexairbot.hexmini.adapter.SettingsViewAdapter;
import com.hexairbot.hexmini.ble.BleConnectinManager;
import com.hexairbot.hexmini.ble.BleConnectinManagerDelegate;
import com.hexairbot.hexmini.ipc.view.VideoSettingView;
import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.modal.OSDCommon;
import com.hexairbot.hexmini.modal.Transmitter;
import com.hexairbot.hexmini.ui.control.ViewPagerIndicator;
import com.vmc.ipc.proxy.IpcProxy;
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
    
    private Button feedbackBtn;
    
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
    private CheckBox isBeginnerModeCheckBox;
    private CheckBox isAutoAltHoldModeCheckBox;
    
    private TextView interfaceOpacityValueTextView;
    private TextView aileronAndElevatorDeadBandValueTextView;
    private TextView rudderDeadBandValueTextView;
    
    private SeekBar interfaceOpacitySeekBar;
    private SeekBar aileronAndElevatorDeadBandSeekBar;
    private SeekBar rudderDeadBandSeekBar;
    
    private OnSeekBarChangeListener interfaceOpacitySeekBarListener;
    private OnSeekBarChangeListener aileronAndElevatorDeadBandSeekBarListener;
    private OnSeekBarChangeListener rudderDeadBandSeekBarListener;
    
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
    VideoSettingView videoSetting;

    
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
        
        feedbackBtn = (Button)container.findViewById(R.id.feedbackBtn);
        feedbackBtn.setVisibility(View.GONE);
        
        
        defaultSettingsBtn = (Button)container.findViewById(R.id.defaultSettingsBtn);
        accCalibrateBtn = (Button)container.findViewById(R.id.accCalibrateBtn);
        magCalibrateBtn = (Button)container.findViewById(R.id.magCalibrateBtn);

        
        titles = new int[] {
                R.string.settings_title_connection,
                R.string.settings_title_personal,
                R.string.settings_title_video,
                R.string.settings_title_angel_trim,
                R.string.settings_title_mode,
                R.string.settings_title_about
        };
    	
        backBtn = (Button)container.findViewById(R.id.backBtn);
        
        int[] pageIds = new int[]{
        		R.layout.settings_page_connection,
        		R.layout.settings_page_personal,
        		R.layout.settings_page_video,
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
//        final int videoPageIdx      = 2;
        final int angelTrimPageIdx  = 3;
        final int modePageIdx       = 4;
        final int aboutPageIdx      = 5;
        								
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
								SettingsViewController.this.delegate.tringToConnect(targetDevice.getName());
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
        
        upTrimBtn   = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.upTrimBtn);
        downTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.downTrimBtn);
        leftTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.leftTrimBtn);
        rightTrimBtn = (Button)settingsViews.get(angelTrimPageIdx).findViewById(R.id.rightTrimBtn);
        
        scanBtn.setText(R.string.btn_title_scan);
        
        isLeftHandedCheckBox   = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isLeftHandedCheckBox);
        isAccModeCheckBox      = (CheckBox)settingsViews.get(interfacePageIdx).findViewById(R.id.isAccModeCheckBox);
        isHeadfreeModeCheckBox = (CheckBox)settingsViews.get(modePageIdx).findViewById(R.id.isHeadfreeModeCheckBox);
        isBeginnerModeCheckBox = (CheckBox)settingsViews.get(modePageIdx).findViewById(R.id.isBeginnerModeCheckBox);
        isAutoAltHoldModeCheckBox = (CheckBox)settingsViews.get(modePageIdx).findViewById(R.id.autoAltHoldCheckBox);
        
        interfaceOpacityValueTextView =  (TextView)settingsViews.get(interfacePageIdx).findViewById(R.id.interfaceOpacityValueTextView);
        aileronAndElevatorDeadBandValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.aileronAndElevatorDeadBandValueTextView);
        rudderDeadBandValueTextView = (TextView)settingsViews.get(modePageIdx).findViewById(R.id.rudderDeadBandValueTextView);
        
        interfaceOpacitySeekBar = (SeekBar)settingsViews.get(interfacePageIdx).findViewById(R.id.interfaceOpacitySeekBar);
        aileronAndElevatorDeadBandSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.aileronAndElevatorDeadBandSeekBar);
        rudderDeadBandSeekBar = (SeekBar)settingsViews.get(modePageIdx).findViewById(R.id.rudderDeadBandSeekBar);
        
        interfaceOpacitySeekBar.setMax(100);
        aileronAndElevatorDeadBandSeekBar.setMax(20);
        rudderDeadBandSeekBar.setMax(20);
        
        WebView aboutWebView = (WebView)settingsViews.get(aboutPageIdx).findViewById(R.id.aboutWebView);
        //aboutWebView.getSettings().setJavaScriptEnabled(true);  
        
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
        isHeadfreeModeCheckBox.setChecked(settings.isHeadFreeMode());
        isBeginnerModeCheckBox.setChecked(settings.isBeginnerMode());
        isAutoAltHoldModeCheckBox.setChecked(settings.isAutoAltHoldMode());
        
        interfaceOpacitySeekBar.setProgress((int)(settings.getInterfaceOpacity() * 100));
        safeSetText(interfaceOpacityValueTextView, interfaceOpacitySeekBar.getProgress() + "%");
        
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
                    ((Activity)this.context).startActivityForResult(enableBtIntent, 1);
                }
            }
		} 
    }
    
    private List<View> initPages(LayoutInflater inflater, int[] pageIds)
    {
        ArrayList<View> pageList = new ArrayList<View>(pageIds.length);

        for (int i = 0; i < pageIds.length; ++i) {
        	if(pageIds[i] == R.layout.settings_page_video){
        		videoSetting = new VideoSettingView(context,inflater);
        		//videoSetting.setTitle(this.getActivity().getResources().getString(R.string.set_video_setting));
        		pageList.add(videoSetting.getContent());
        		
        	}
        	else{
        		View view = inflater.inflate(pageIds[i], null);
        		//FontUtils.applyFont(inflater.getContext(), (ViewGroup) view);
        		pageList.add(view);
        	}
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
    	
    	upTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "MSP_TRIM_UP");
				Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_TRIM_UP);
			}
		});
    	
    	downTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "MSP_TRIM_DOWN");
				Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_TRIM_DOWN);
			}
		});
    	
    	leftTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "MSP_TRIM_LEFT");
				Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_TRIM_LEFT);
			}
		});
    	
    	rightTrimBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "MSP_TRIM_RIGHT");
				Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_TRIM_RIGHT);
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
    	
    	accCalibrateBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
		      	new AlertDialog.Builder(SettingsViewController.this.context)
				.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.dialog_title_info)
				.setMessage(R.string.dialog_calibrate_acc)
				.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_ACC_CALIBRATION);
					}
				}).setNegativeButton(R.string.dialog_btn_no, null).show();				
			}
		});
    	
    	defaultSettingsBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(SettingsViewController.this.context)
				.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.dialog_title_info)
				.setMessage(R.string.dialog_reset)
				.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();

						settings.resetToDefault();
						
						settings.save();
						
						SettingsViewController.this.updateSettingsUI();
						
						if (delegate != null) {
							delegate.interfaceOpacityValueDidChange(settings.getInterfaceOpacity() * 100);
						
							delegate.leftHandedValueDidChange(settings.isLeftHanded());
						
							delegate.accModeValueDidChange(settings.isAccMode());
							delegate.headfreeModeValueDidChange(settings.isHeadFreeMode());
						
							delegate.aileronAndElevatorDeadBandValueDidChange(settings.getAileronDeadBand());
							delegate.rudderDeadBandValueDidChange(settings.getRudderDeadBand());
						
						}
					}
				}).setNegativeButton(R.string.dialog_btn_no, null).show();	
			}
		});
    	
    	feedbackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(SettingsViewController.this.getContext(), FeedbackActivity.class);
				SettingsViewController.this.getContext().startActivity(intent);
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
        
        isAutoAltHoldModeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isAutoAltHoldMode) {
				ApplicationSettings settings = HexMiniApplication.sharedApplicaion().getAppSettings();
				settings.setIsAutoAltHoldMode(isAutoAltHoldMode);
				settings.save();
				if (delegate != null) {
					delegate.autoAltHoldModeValueDidChange(isAutoAltHoldMode);
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
				//TextView bleDeviceAddressTextView = (TextView)row.findViewById(R.id.bleDeviceAddressTextView);
			
				 bleDeviceNameTextView.setTextColor(Color.WHITE);
				 bleDeviceNameTextView.setBackgroundColor(Color.BLUE);
				//bleDeviceAddressTextView.setTextColor(Color.BLACK);
				if ("Any Flite".equals(mLeDevices.get(position).getName())
						|| "Flexbot".equals(mLeDevices.get(position).getName())
						|| "FlexBLE".equals(mLeDevices.get(position).getName())) {
					bleDeviceNameTextView.setText("Flexbot");
				}
				else{
					bleDeviceNameTextView.setText(R.string.unknown);
				}
				 
								//bleDeviceAddressTextView.setText(mLeDevices.get(position).getAddress());
				
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
	public void viewWillAppear() {
		// TODO Auto-generated method stub
		super.viewWillAppear();
		sendBleEnableRequest();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(IpcProxy.ACTION_BITRATE_CHANGED);
		filter.addAction(IpcProxy.ACTION_DECODEMODE_CHANGED);
		filter.addAction(IpcProxy.ACTION_RESOLUTION_CHANGED);
		mLocalBroadcastManager.registerReceiver(receiver, filter);
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
		
		mLocalBroadcastManager.unregisterReceiver(receiver);
	}
	
    BroadcastReceiver receiver = new BroadcastReceiver() {

	@Override
	public void onReceive(Context context, Intent intent) {
	    // TODO Auto-generated method stub
	    String action = intent.getAction();
	    if(action.equals(IpcProxy.ACTION_RESOLUTION_CHANGED)) {
		if(videoSetting != null) {
		    videoSetting.refreshResolutionConfig();
		}
	    }
	    else if(action.equals(IpcProxy.ACTION_DECODEMODE_CHANGED)) {
		if(videoSetting != null) {
		    videoSetting.refreshDecodeConfig();
		}
	    }
	    else if(action.equals(IpcProxy.ACTION_BITRATE_CHANGED)) {
		
	    }
	}
	
    };
}
