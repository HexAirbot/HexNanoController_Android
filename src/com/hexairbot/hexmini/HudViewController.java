package com.hexairbot.hexmini;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hexairbot.hexmini.R;

import com.hexairbot.hexmini.HexMiniApplication.AppStage;
import com.hexairbot.hexmini.ble.BleConnectinManager;
import com.hexairbot.hexmini.gestures.EnhancedGestureDetector;
import com.hexairbot.hexmini.ipc.view.VideoSettingView;
import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.modal.Channel;
import com.hexairbot.hexmini.modal.OSDCommon;
import com.hexairbot.hexmini.modal.Transmitter;
import com.hexairbot.hexmini.sensors.DeviceOrientationChangeDelegate;
import com.hexairbot.hexmini.sensors.DeviceOrientationManager;
import com.hexairbot.hexmini.sensors.DeviceSensorManagerWrapper;
import com.hexairbot.hexmini.ui.AnimationIndicator;
import com.hexairbot.hexmini.ui.Button;
import com.hexairbot.hexmini.ui.Image;
import com.hexairbot.hexmini.ui.Indicator;
import com.hexairbot.hexmini.ui.Sprite;
import com.hexairbot.hexmini.ui.Text;
import com.hexairbot.hexmini.ui.ToggleButton;
import com.hexairbot.hexmini.ui.UIRenderer;
import com.hexairbot.hexmini.ui.Image.SizeParams;
import com.hexairbot.hexmini.ui.Sprite.Align;
import com.hexairbot.hexmini.ui.joystick.AcceleratorJoystick;
import com.hexairbot.hexmini.ui.joystick.AnalogueJoystick;
import com.hexairbot.hexmini.ui.joystick.JoystickBase;
import com.hexairbot.hexmini.ui.joystick.JoystickFactory;
import com.hexairbot.hexmini.ui.joystick.JoystickListener;
import com.hexairbot.hexmini.ui.joystick.JoystickFactory.JoystickType;
import com.hexairbot.hexmini.util.DebugHandler;
import com.hexairbot.hexmini.util.FontUtils;
import com.hexairbot.hexmini.util.SystemUtil;
import com.vmc.ipc.config.VmcConfig;
import com.vmc.ipc.proxy.IpcProxy;
import com.vmc.ipc.proxy.IpcProxy.OnRecordCompleteListener;
import com.vmc.ipc.service.ConnectStateManager;
import com.vmc.ipc.service.IpcControlService;
import com.vmc.ipc.service.OnIpcConnectChangedListener;
import com.vmc.ipc.util.MediaUtil;


public class HudViewController extends ViewController
	implements OnTouchListener,
			   OnGestureListener,
			   SettingsViewControllerDelegate, DeviceOrientationChangeDelegate
{
	private static final String TAG = "HudViewController";
	
    public final static String ACTION_RESTART_PREVIEW = "action_restart_preview";
	
	private static final int JOY_ID_LEFT          = 1;
	private static final int JOY_ID_RIGHT         = 2;
	private static final int MIDLLE_BG_ID         = 3;
	private static final int TOP_BAR_ID           = 4;
	private static final int BOTTOM_BAR_ID        = 5;
	private static final int TAKE_OFF_BTN_ID      = 6;
	private static final int STOP_BTN_ID          = 7;
	private static final int SETTINGS_BTN_ID      = 8;
	private static final int ALT_HOLD_TOGGLE_BTN  = 9;
	private static final int STATE_TEXT_VIEW      = 10;
	private static final int BATTERY_INDICATOR_ID = 11;
	private static final int HELP_BTN             = 12;
	private static final int BOTTOM_LEFT_SKREW    = 13;
	private static final int BOTTOM_RIGHT_SKREW   = 14;
	private static final int LOGO                 = 15;
	private static final int STATUS_BAR           = 16;
	
	private static final int DEVICE_BATTERY_INDICATOR  = 17;
	private static final int BACK_BTN                  = 18;
	private static final int RECORD_BTN                = 19;
	private static final int CAPTURE_BTN               = 20;
	private static final int WIFI_INDICATOR_ID         = 21;
	private static final int RECORDING_INDICATOR       = 22;
	
	private final float  BEGINNER_ELEVATOR_CHANNEL_RATIO  = 0.5f;
	private final float  BEGINNER_AILERON_CHANNEL_RATIO   = 0.5f;
	private final float  BEGINNER_RUDDER_CHANNEL_RATIO    = 0.0f;
	private final float  BEGINNER_THROTTLE_CHANNEL_RATIO  = 0.8f;
	
	private Image bottomBarBg;

	private Button stopBtn;
	private Button takeOffBtn;
	private Button settingsBtn;
	private ToggleButton altHoldToggleBtn;
	
	private Button captureBtn;
	private Button recordBtn;
	private Button backBtn;
	
	private Text stateTextView;
	
	private boolean isAltHoldMode;
	private boolean isAccMode;
	
	private Button[] buttons;
	
	private Indicator batteryIndicator;
	private Indicator deviceBatteryIndicator;
	private Indicator wifiIndicator;
	private AnimationIndicator recordingIndicator;
	
	private Text txtBatteryStatus;
	
	private GLSurfaceView glView;
	
	private JoystickBase[] joysticks;   //[0]roll and pitch, [1]rudder and throttle
	private float joypadOpacity;
	private GestureDetector gestureDetector;
	
	private UIRenderer renderer;
	
    private HudViewControllerDelegate delegate;
    
    private boolean isLeftHanded;
    private JoystickListener rollPitchListener;
    private JoystickListener rudderThrottleListener;
    
    private ApplicationSettings settings;
    
    private Channel aileronChannel;
    private Channel elevatorChannel;
    private Channel rudderChannel;
    private Channel throttleChannel;
    private Channel aux1Channel;
    private Channel aux2Channel;
    private Channel aux3Channel;
    private Channel aux4Channel;
    
    private DeviceOrientationManager deviceOrientationManager;
    private static final float ACCELERO_TRESHOLD = (float) Math.PI / 180.0f * 2.0f;
    private static final int PITCH = 1;
    private static final int ROLL = 2;
    private float pitchBase;
    private float rollBase;
    private boolean rollAndPitchJoystickPressed;
    
    private IpcControlService controlService;
    
    private GLSurfaceView videoStageSoft = null;
    private SurfaceView videoStageHard = null;
    
    private LocalBroadcastManager mLocalBroadcastManager;
    
    private IpcProxy ipcProxy;
    
    private boolean isStartRecord = false;
    final CustomOnRecordCompleteListener mCustomOnRecordCompleteListener = new CustomOnRecordCompleteListener();
    
    private boolean isAcPlugin = false;
    
    private void setVideoEnv(){
    	SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this.context);

		int decodeMode = VmcConfig.getInstance().getDecodeMode();
		if (decodeMode == -1) {
			decodeMode = IpcProxy.DEFAULT_DECODE_MODE;
		}
		setDecodeMode(decodeMode);
		
	    Intent intent = new Intent();
		intent.setClass(this.context, IpcControlService.class);
		this.context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	    
	    mLocalBroadcastManager = LocalBroadcastManager.getInstance(this.context);
	    registerAllBroadcastReceiver();
		
    	
    }
    
	public HudViewController(Activity context, HudViewControllerDelegate delegate)
	{
		Log.e("***haha", "***test");
		
		this.delegate = delegate;
		this.context = context;
		Transmitter.sharedTransmitter().setBleConnectionManager(new BleConnectinManager(context));      
		
		settings = ((HexMiniApplication)context.getApplication()).getAppSettings();
		
	    joypadOpacity = settings.getInterfaceOpacity();
	    isLeftHanded  = settings.isLeftHanded();
	    
		this.context = context;
		gestureDetector = new EnhancedGestureDetector(context, this);
		
		joysticks = new JoystickBase[2];

		glView = new GLSurfaceView(context);
		glView.setEGLContextClientVersion(2);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//LinearLayout hud = (LinearLayout)inflater.inflate(R.layout.hud, null);
		//LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		//hud.addView(glView, layoutParams);
		//glView.setBackgroundResource(R.drawable.settings_bg);
		
		
		context.setContentView(R.layout.hud_view_controller_framelayout);
		
		FrameLayout mainFrameLayout = (FrameLayout)context.findViewById(R.id.mainFrameLaytout);
		
		//let glView to be transparent
		glView.setZOrderOnTop(true);
		glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		 
		mainFrameLayout.addView(glView);
		
		ConnectStateManager mConnectStateManager = ConnectStateManager
				.getInstance(HexMiniApplication.sharedApplicaion());
		ipcProxy = mConnectStateManager.getIpcProxy();
		
		videoStageSoft = (GLSurfaceView)context.findViewById(R.id.video_bg_soft2);
		videoStageHard = (SurfaceView)context.findViewById(R.id.video_bg_hard2);
		

		setVideoEnv();
		
		//context.setContentView(glView);
		
		renderer = new UIRenderer(context, null);
	
		initGLSurfaceView();

		Resources res = context.getResources();

		Image topBarBg = new Image(res, R.drawable.bar_top, Align.TOP_CENTER);
		topBarBg.setSizeParams(SizeParams.FILL_SCREEN, SizeParams.NONE);  //Width水平伸缩至全屏，height保持不边
		topBarBg.setAlphaEnabled(true);
		
		bottomBarBg = new Image(res, R.drawable.bar_bottom, Align.BOTTOM_CENTER);
		bottomBarBg.setSizeParams(SizeParams.FILL_SCREEN, SizeParams.NONE);
		bottomBarBg.setAlphaEnabled(true);
		
		Image middleBg = new Image(res, R.drawable.bg_tile, Align.CENTER);
		middleBg.setAlpha(0.5f);
		middleBg.setVisible(true);
		middleBg.setSizeParams(SizeParams.REPEATED, SizeParams.REPEATED);  //Width水平伸缩至全屏，height保持不边
		middleBg.setAlphaEnabled(true);
		
		Image bottomLeftSkrew = new Image(res, R.drawable.screw, Align.BOTTOM_LEFT);
		Image bottomRightSkrew = new Image(res, R.drawable.screw, Align.BOTTOM_RIGHT);
		
		Image logo = new Image(res, R.drawable.logo, Align.BOTTOM_LEFT);
		logo.setMargin(0, 0, (int)res.getDimension(R.dimen.hud_logo_margin_bottom), (int)res.getDimension(R.dimen.hud_logo_margin_left));
		
		Image statusBar = new Image(res, R.drawable.status_bar, Align.TOP_LEFT);
		statusBar.setMargin((int)res.getDimension(R.dimen.hud_status_bar_margin_top), 0, 0, (int)res.getDimension(R.dimen.hud_status_bar_margin_left));
		
		settingsBtn = new Button(res, R.drawable.btn_settings_normal, R.drawable.btn_settings_hl, Align.TOP_RIGHT);
		settingsBtn.setMargin((int)res.getDimension(R.dimen.hud_btn_settings_margin_top), (int)res.getDimension(R.dimen.hud_btn_settings_margin_right), 0, 0);
		
		Button helpBtn = new Button(res, R.drawable.btn_help_normal, R.drawable.btn_help_hl, Align.TOP_RIGHT);
		helpBtn.setMargin((int)res.getDimension(R.dimen.hud_btn_settings_margin_top), (int)res.getDimension(R.dimen.hud_btn_settings_margin_right) * 4, 0, 0);
			
		takeOffBtn = new Button(res, R.drawable.btn_take_off_normal, R.drawable.btn_take_off_hl, Align.BOTTOM_CENTER);		
		takeOffBtn.setAlphaEnabled(true);
		stopBtn = new Button(res, R.drawable.btn_stop_normal, R.drawable.btn_stop_hl, Align.TOP_CENTER);
		stopBtn.setAlphaEnabled(true);
		
		String state = context.getResources().getString(R.string.settings_item_connection_state_not_conneceted);
		stateTextView = new Text(context, state, Align.TOP_LEFT);
		stateTextView.setMargin((int)res.getDimension(R.dimen.hud_state_text_margin_top), 0, 0, (int)res.getDimension(R.dimen.hud_state_text_margin_left));
		stateTextView.setTextColor(Color.WHITE);
		stateTextView.setTypeface(FontUtils.TYPEFACE.Helvetica(context));
		stateTextView.setTextSize(res.getDimensionPixelSize(R.dimen.hud_state_text_size));
		
		int batteryIndicatorRes[] = {R.drawable.btn_battery_0,
				R.drawable.btn_battery_1,
				R.drawable.btn_battery_2,
				R.drawable.btn_battery_3,
				R.drawable.btn_battery_4
		};

		batteryIndicator = new Indicator(res, batteryIndicatorRes, Align.TOP_LEFT);
		batteryIndicator.setMargin((int)res.getDimension(R.dimen.hud_batterry_indicator_margin_top), 0, 0, (int)res.getDimension(R.dimen.hud_batterry_indicator_margin_left));
		
		altHoldToggleBtn = new ToggleButton(res, R.drawable.alt_hold_off, R.drawable.alt_hold_off_hl, 
                R.drawable.alt_hold_on, R.drawable.alt_hold_on_hl,
                R.drawable.alt_hold_on, Align.TOP_LEFT);
		
		altHoldToggleBtn.setMargin(res.getDimensionPixelOffset(R.dimen.hud_alt_hold_toggle_btn_margin_top), 0, 0, res.getDimensionPixelOffset(R.dimen.hud_alt_hold_toggle_btn_margin_left));
		altHoldToggleBtn.setChecked(settings.isAltHoldMode());
		altHoldToggleBtn.setVisible(false);
		//altHoldToggleBtn.setAlphaEnabled(true);
		
		settingsBtn = new Button(res, R.drawable.btn_settings_normal, R.drawable.btn_settings_hl, Align.TOP_RIGHT);
		settingsBtn.setMargin((int)res.getDimension(R.dimen.hud_btn_settings_margin_top), (int)res.getDimension(R.dimen.hud_btn_settings_margin_right), 0, 0);
		
		captureBtn = new Button(res, R.drawable.btn_photos, R.drawable.btn_photos_pressed, Align.TOP_RIGHT);
		captureBtn.setMargin((int)res.getDimension(R.dimen.hud_btn_capture_margin_top), (int)res.getDimension(R.dimen.hud_btn_capture_margin_right), 0, 0);
		
		recordBtn = new Button(res, R.drawable.btn_videos, R.drawable.btn_videos_selected, Align.TOP_RIGHT);
		recordBtn.setMargin((int)res.getDimension(R.dimen.hud_btn_record_margin_top), (int)res.getDimension(R.dimen.hud_btn_record_margin_right), 0, 0);     
		
		backBtn = new Button(res, R.drawable.btn_back_normal, R.drawable.btn_back_hl, Align.TOP_LEFT);
		backBtn.setMargin((int)res.getDimension(R.dimen.hud_btn_back_margin_top), 0, 0, (int)res.getDimension(R.dimen.hud_btn_back_margin_left));
		
		int wifiIndicatorRes[] = {R.drawable.btn_wifi_0,
				R.drawable.btn_wifi_1,
				R.drawable.btn_wifi_2,
				R.drawable.btn_wifi_3
		};

		wifiIndicator = new Indicator(res, wifiIndicatorRes, Align.TOP_RIGHT);
		wifiIndicator.setMargin((int)res.getDimension(R.dimen.hud_wifi_indicator_margin_top), (int)res.getDimension(R.dimen.hud_wifi_indicator_margin_right), 0, 0);
		
		int deviceBatteryIndicatorRes[] = {R.drawable.btn_battery_0,
				R.drawable.btn_battery_1,
				R.drawable.btn_battery_2,
				R.drawable.btn_battery_3,
				R.drawable.btn_battery_4
		};

		deviceBatteryIndicator = new Indicator(res, deviceBatteryIndicatorRes, Align.TOP_RIGHT);
		deviceBatteryIndicator.setMargin((int)res.getDimension(R.dimen.hud_device_battery_indicator_margin_top), (int)res.getDimension(R.dimen.hud_device_battery_indicator_margin_right), 0, 0);
		
		int recordingIndicatorRes[] = {R.drawable.btn_record2, 
				R.drawable.btn_record1_pressed};
		recordingIndicator = new AnimationIndicator(res, recordingIndicatorRes, Align.TOP_RIGHT);
		recordingIndicator.setMargin((int)res.getDimension(R.dimen.hud_recording_idicator_margin_top), (int)res.getDimension(R.dimen.hud_recording_idicator_margin_right), 0, 0);
		recordingIndicator.setAlphaEnabled(true);
		recordingIndicator.setVisible(false);
		
		buttons = new Button[8];
		buttons[0] = settingsBtn;
		buttons[1] = takeOffBtn;
		buttons[2] = stopBtn;
		buttons[3] = altHoldToggleBtn;
		buttons[4] = helpBtn;
		buttons[5] = captureBtn;
		buttons[6] = recordBtn;
		buttons[7] = backBtn;
		
		//renderer.addSprite(MIDLLE_BG_ID, middleBg);
		renderer.addSprite(TOP_BAR_ID, topBarBg);
		renderer.addSprite(BOTTOM_BAR_ID, bottomBarBg);
		
		
		renderer.addSprite(BOTTOM_LEFT_SKREW, bottomLeftSkrew);
		
		renderer.addSprite(BOTTOM_RIGHT_SKREW, bottomRightSkrew);
		
		
		renderer.addSprite(LOGO, logo);
		renderer.addSprite(STATUS_BAR, statusBar);
		renderer.addSprite(BATTERY_INDICATOR_ID, batteryIndicator);
		renderer.addSprite(TAKE_OFF_BTN_ID, takeOffBtn);
		renderer.addSprite(STOP_BTN_ID, stopBtn);
		renderer.addSprite(SETTINGS_BTN_ID, settingsBtn);
		renderer.addSprite(ALT_HOLD_TOGGLE_BTN, altHoldToggleBtn);
		renderer.addSprite(CAPTURE_BTN, captureBtn);
		renderer.addSprite(RECORD_BTN, recordBtn);
		renderer.addSprite(BACK_BTN, backBtn);
		renderer.addSprite(WIFI_INDICATOR_ID, wifiIndicator);
		renderer.addSprite(DEVICE_BATTERY_INDICATOR, deviceBatteryIndicator);
		renderer.addSprite(RECORDING_INDICATOR, recordingIndicator);
		renderer.addSprite(STATE_TEXT_VIEW, stateTextView);
		
		//renderer.addSprite(HELP_BTN, helpBtn);
		
		
		isAccMode = settings.isAccMode();
		deviceOrientationManager = new DeviceOrientationManager(new DeviceSensorManagerWrapper(this.context), this);
		deviceOrientationManager.onCreate();
		
		
		initJoystickListeners();
		
		helpBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HudViewController.this.context, HelpActivity.class);
				HudViewController.this.context.startActivity(intent);
			}
		});
		
		
		if (isAccMode) {
			initJoysticks(JoystickType.ACCELERO);
		}
		else{
			initJoysticks(JoystickType.ANALOGUE);
		}
		
		initListeners();
		
		initChannels();
		
		if (settings.isHeadFreeMode()) {
			aux1Channel.setValue(1);
		}
		else {
			aux1Channel.setValue(-1);
		}
		
		if (settings.isAltHoldMode()) {
			aux2Channel.setValue(1);
		}
		else{
			aux2Channel.setValue(-1);
		}
		
	    if (settings.isBeginnerMode()) {	       
			new AlertDialog.Builder(context)
			.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.dialog_title_info)
			.setMessage(R.string.beginner_mode_info)
			.setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).show();
	    }
	}
	
	private void initChannels() {
	    aileronChannel  = settings.getChannel(Channel.CHANNEL_NAME_AILERON);
	    elevatorChannel = settings.getChannel(Channel.CHANNEL_NAME_ELEVATOR);
	    rudderChannel   = settings.getChannel(Channel.CHANNEL_NAME_RUDDER);
	    throttleChannel = settings.getChannel(Channel.CHANNEL_NAME_THROTTLE);
	    aux1Channel     = settings.getChannel(Channel.CHANNEL_NAME_AUX1);
	    aux2Channel     = settings.getChannel(Channel.CHANNEL_NAME_AUX2);
	    aux3Channel     = settings.getChannel(Channel.CHANNEL_NAME_AUX3);
	    aux4Channel     = settings.getChannel(Channel.CHANNEL_NAME_AUX4);
	    
	    aileronChannel.setValue(0.0f);
	    elevatorChannel.setValue(0.0f);
	    rudderChannel.setValue(0.0f);
	    throttleChannel.setValue(-1);
	}
	
	private void initJoystickListeners()
    {
	        rollPitchListener = new JoystickListener()
	        {
	            public void onChanged(JoystickBase joy, float x, float y)
	            {
	            	if(HexMiniApplication.sharedApplicaion().getAppStage() == AppStage.SETTINGS){
	            		//Log.e(TAG, "AppStage.SETTINGS ignore rollPitchListener onChanged");
	            		return;
	            	}
	            	
	            	if (isAccMode == false && rollAndPitchJoystickPressed == true) {
		        		//Log.e(TAG, "rollPitchListener onChanged x:" + x + "y:" + y);
		        		
		        		if (settings.isBeginnerMode()) {
		        			aileronChannel.setValue(x * BEGINNER_AILERON_CHANNEL_RATIO);
		        			elevatorChannel.setValue(y * BEGINNER_ELEVATOR_CHANNEL_RATIO);
						}
		        		else{
			                aileronChannel.setValue(x);
			                elevatorChannel.setValue(y);
		        		}
					}
	            }

	            @Override
	            public void onPressed(JoystickBase joy)
	            {
	            	rollAndPitchJoystickPressed = true;
	            }

	            @Override
	            public void onReleased(JoystickBase joy)
	            {
	            	rollAndPitchJoystickPressed = false;
	            	
	                aileronChannel.setValue(0.0f);
	                elevatorChannel.setValue(0.0f);
	               
	            }
	        };

	        rudderThrottleListener = new JoystickListener()
	        {
	            public void onChanged(JoystickBase joy, float x, float y)
	            {
	            	if(HexMiniApplication.sharedApplicaion().getAppStage() == AppStage.SETTINGS){
	            		Log.e(TAG, "AppStage.SETTINGS ignore rudderThrottleListener onChanged");
	            		return;
	            	}
	            	
	            	
	        		Log.e(TAG, "rudderThrottleListener onChanged x:" + x + "y:" + y);
	        		
	        		
	        		if (settings.isBeginnerMode()) {
	        			rudderChannel.setValue(x * BEGINNER_RUDDER_CHANNEL_RATIO);
		        		throttleChannel.setValue((BEGINNER_THROTTLE_CHANNEL_RATIO - 1) + y * BEGINNER_THROTTLE_CHANNEL_RATIO);

					}else{
		        		rudderChannel.setValue(x);
		        		throttleChannel.setValue(y);
					}
	            }

	            @Override
	            public void onPressed(JoystickBase joy)
	            {
	            	
	            }

	            @Override
	            public void onReleased(JoystickBase joy)
	            {
	        		rudderChannel.setValue(0.0f);
	        		
	        		Log.e(TAG, "rudderThrottleListener onReleased"+joy.getYValue());
	        		
	        		throttleChannel.setValue(joy.getYValue());
	            }
	        };
    }
	
	private void initListeners() {
		settingsBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if (delegate != null) {
					delegate.settingsBtnDidClick(arg0);
				}

			}
		});
		
		takeOffBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			    throttleChannel.setValue(-1);
			    getRudderAndThrottleJoystick().setYValue(-1);
				Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_ARM);
			}
		});
		
		stopBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_DISARM);
			}
		});
		
		
		altHoldToggleBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isAltHoldMode = !isAltHoldMode;
				settings.setIsAltHoldMode(isAltHoldMode);
				settings.save();
				
				altHoldToggleBtn.setChecked(isAltHoldMode);
				
				if (isAltHoldMode) {
					aux2Channel.setValue(1);
				}
				else{
					aux2Channel.setValue(-1);
				}
			}
		});
	
		initVideoListener();
	}
	
	/*
	  private OnClickListener mOnClickListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
			    if (v == backBtn) {
				HudViewController.this.context.finish();
			    } else if (v == btn_capture) {
				AsyncTask<Void, Void, Void> captureTask = new AsyncTask<Void, Void, Void>() {

				    @Override
				    protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					v.setEnabled(false);
				    }

				    @Override
				    protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					playSound(soundid_camera_click);
					if (!VmcConfig.getInstance().isStoreRemote()) {
					    String dirPath = Environment
						    .getExternalStorageDirectory()
						    .getAbsolutePath()
						    + MediaUtil.IPC_IMAGE_DIR;
					    String filePath = System.currentTimeMillis()
						    + ".jpg";
					    ConnectStateManager
						    .getInstance(
							    ControllerActivity.this
								    .getApplication())
						    .getIpcProxy()
						    .doTakePhoto(dirPath, filePath, false);
					    MediaUtil.scanIpcMediaFile(ControllerActivity.this,
						    dirPath + filePath);
					} else {
					    ipcProxy.takePhotoRemote(false);
					}
					return null;
				    }

				    protected void onPostExecute(Void result) {
					v.setEnabled(true);
				    }
				};
				captureTask.execute();

			    } else if (v == btn_record) {
				btn_setting.setEnabled(false);
				final AnimationDrawable animation = (AnimationDrawable) img_indication_record
					.getDrawable();
				if (!isStartRecord) {
				    playSound(soundid_video_record);
				    AsyncTask<Void, Void, Void> startRecordTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
					    // TODO Auto-generated method stub
					    if (!VmcConfig.getInstance().isStoreRemote()) {
						String dirPath = Environment
							.getExternalStorageDirectory()
							.getAbsolutePath()
							+ MediaUtil.IPC_VIDEO_DIR;
						String filePath = System.currentTimeMillis()
							+ ".mp4";
						mCustomOnRecordCompleteListener.setPath(dirPath
							+ filePath);
						ipcProxy.addOnRecordCompleteListener(mCustomOnRecordCompleteListener);
						ipcProxy.doStartRecord(dirPath, null, filePath,
							false);
					    } else {
						ipcProxy.startRecordRemote(false);
					    }
					    ControllerActivity.this
						    .runOnUiThread(new Runnable() {

							@Override
							public void run() {
							    // TODO Auto-generated method stub
							    img_indication_record
								    .setVisibility(View.VISIBLE);
							    animation.start();
							}
						    });
					    isStartRecord = true;
					    return null;
					}
				    };
				    startRecordTask.execute();
				} else {
				    playSound(soundid_video_record);
				    AsyncTask<Void, Void, Void> stopRecordTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
					    // TODO Auto-generated method stub
					    if (!VmcConfig.getInstance().isStoreRemote()) {
						ipcProxy.doStopRecord();
						ipcProxy.onRecordComplete(true);
						ipcProxy.removeOnRecordCompleteListener(mCustomOnRecordCompleteListener);
					    } else {
						ipcProxy.stopRecordRemote();
					    }
					    isStartRecord = false;
					    return null;
					}

					protected void onPostExecute(Void result) {
					    animation.stop();
					    img_indication_record.setVisibility(View.GONE);
					    btn_setting.setEnabled(true);
					}
				    };
				    stopRecordTask.execute();
				}
			    } else if (v == btn_setting) {
				SettingsDialog mSettingsDialog = SettingsDialog
					.newInstance(SettingsDialog.VIDEO_SETTING_PAGE);
				mSettingsDialog.show(getSupportFragmentManager(), "setting");
			    } else if (v == debugSwitch) {
				switchDebugInfo(!isDebugShow);
			    }
			}
		    };
	 */
	
	private void initVideoListener(){
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((Activity)(HudViewController.this.context)).finish();
			}
		});
		
		captureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AsyncTask<Void, Void, Void> captureTask = new AsyncTask<Void, Void, Void>() {

				    @Override
				    protected void onPreExecute() {
					// TODO Auto-generated method stub
					super.onPreExecute();
					captureBtn.setEnabled(false);
				    }

				    @Override
				    protected Void doInBackground(Void... params) {
					// TODO Auto-generated method stub
					//playSound(soundid_camera_click);
					if (!VmcConfig.getInstance().isStoreRemote()) {
					    String dirPath = Environment
						    .getExternalStorageDirectory()
						    .getAbsolutePath()
						    + MediaUtil.IPC_IMAGE_DIR;
					    String filePath = System.currentTimeMillis()
						    + ".jpg";
					    ConnectStateManager
						    .getInstance(HexMiniApplication.sharedApplicaion())
						    .getIpcProxy()
						    .doTakePhoto(dirPath, filePath, false);
					    MediaUtil.scanIpcMediaFile(HudViewController.this.context,
						    dirPath + filePath);
					} else {
					    ipcProxy.takePhotoRemote(false);
					}
					return null;
				    }

				    protected void onPostExecute(Void result) {
				    	captureBtn.setEnabled(true);
				    }
				};
				captureTask.execute();

			}
		});
		
		recordBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				settingsBtn.setEnabled(false);
				//final AnimationDrawable animation = (AnimationDrawable) img_indication_record.getDrawable();
				if (!isStartRecord) {
				   // playSound(soundid_video_record);
				    AsyncTask<Void, Void, Void> startRecordTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
					    // TODO Auto-generated method stub
					    if (!VmcConfig.getInstance().isStoreRemote()) {
						String dirPath = Environment
							.getExternalStorageDirectory()
							.getAbsolutePath()
							+ MediaUtil.IPC_VIDEO_DIR;
						String filePath = System.currentTimeMillis()
							+ ".mp4";
						mCustomOnRecordCompleteListener.setPath(dirPath
							+ filePath);
						ipcProxy.addOnRecordCompleteListener(mCustomOnRecordCompleteListener);
						ipcProxy.doStartRecord(dirPath, null, filePath,
							false);
					    } else {
						ipcProxy.startRecordRemote(false);
					    }
					    ((Activity)HudViewController.this.context)
						    .runOnUiThread(new Runnable() {

							@Override
							public void run() {
							    // TODO Auto-generated method stub
							    //img_indication_record.setVisibility(View.VISIBLE);
							    //animation.start();
								
								recordingIndicator.setVisible(true);
								recordingIndicator.start(0.3f);
								recordingIndicator.setAlpha(1);
							}
						    });
					    isStartRecord = true;
					    return null;
					}
				    };
				    startRecordTask.execute();
				} else {
				   // playSound(soundid_video_record);
				    AsyncTask<Void, Void, Void> stopRecordTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
					    // TODO Auto-generated method stub
					    if (!VmcConfig.getInstance().isStoreRemote()) {
						ipcProxy.doStopRecord();
						ipcProxy.onRecordComplete(true);
						ipcProxy.removeOnRecordCompleteListener(mCustomOnRecordCompleteListener);
					    } else {
						ipcProxy.stopRecordRemote();
					    }
					    isStartRecord = false;
					    return null;
					}

					protected void onPostExecute(Void result) {
						recordingIndicator.stop();
						recordingIndicator.setVisible(false);
						recordingIndicator.setAlpha(0);
					    //animation.stop();
					    //img_indication_record.setVisibility(View.GONE);
					    settingsBtn.setEnabled(true);
					}
				    };
				    stopRecordTask.execute();
				}
			}
		});
		
		
	}
	
	private void initGLSurfaceView() {
		if (glView != null) {
			glView.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
			glView.setRenderer(renderer);
			glView.setOnTouchListener(this);
		}
	}
	
	 private void initJoysticks(JoystickType rollAndPitchType)
	    {
	        JoystickBase rollAndPitchJoystick      = getRollAndPitchJoystick(); 
	        JoystickBase rudderAndThrottleJoystick = getRudderAndThrottleJoystick();
	        
	        if (rollAndPitchType == JoystickType.ANALOGUE) {
	            if (rollAndPitchJoystick == null || !(rollAndPitchJoystick instanceof AnalogueJoystick)) {
	            	rollAndPitchJoystick = JoystickFactory.createAnalogueJoystick(this.getContext(), false, rollPitchListener, true);
	            	rollAndPitchJoystick.setXDeadBand(settings.getAileronDeadBand());
	            	rollAndPitchJoystick.setYDeadBand(settings.getElevatorDeadBand());
	            } 
	            else {
	            	rollAndPitchJoystick.setOnAnalogueChangedListener(rollPitchListener);
	            }
			}
	        else if(rollAndPitchType == JoystickType.ACCELERO){
	            if (rollAndPitchJoystick == null || !(rollAndPitchJoystick instanceof AcceleratorJoystick)) {
	            	rollAndPitchJoystick = JoystickFactory.createAcceleroJoystick(this.getContext(), false, rollPitchListener, true);
	            	//rollAndPitchJoystick.setXDeadBand(settings.getAileronDeadBand());
	            	//rollAndPitchJoystick.setYDeadBand(settings.getElevatorDeadBand());
	            } 
	            else {
	            	rollAndPitchJoystick.setOnAnalogueChangedListener(rollPitchListener);
	            }
	        }
	        
	        if (rudderAndThrottleJoystick == null || !(rudderAndThrottleJoystick instanceof AnalogueJoystick)) {
	        	rudderAndThrottleJoystick = JoystickFactory.createAnalogueJoystick(this.getContext(), false, rudderThrottleListener, false);
	        	rudderAndThrottleJoystick.setXDeadBand(settings.getRudderDeadBand());
	        } 
	        else {
	        	rudderAndThrottleJoystick.setOnAnalogueChangedListener(rudderThrottleListener);
	        }
	        
	        rollAndPitchJoystick.setIsRollPitchJoystick(true);
	        rudderAndThrottleJoystick.setIsRollPitchJoystick(false);
	        
	        joysticks[0] = rollAndPitchJoystick;
	        joysticks[1] = rudderAndThrottleJoystick;
	        
	        setJoysticks();
	        
	        getRudderAndThrottleJoystick().setYValue(-1);
	    }
	
	public void setJoysticks()
	{
		JoystickBase rollAndPitchJoystick = joysticks[0];
		JoystickBase rudderAndThrottleJoystick = joysticks[1];
		
		if (rollAndPitchJoystick != null) 
		{
			if (isLeftHanded) {
			    joysticks[0].setAlign(Align.BOTTOM_RIGHT);
			    joysticks[0].setAlpha(joypadOpacity);
			}else{
				joysticks[0].setAlign(Align.BOTTOM_LEFT);
				joysticks[0].setAlpha(joypadOpacity);
			}
			
			rollAndPitchJoystick.setNeedsUpdate();
		}
	
		if (rudderAndThrottleJoystick != null)	{
			if (isLeftHanded) {
			    joysticks[1].setAlign(Align.BOTTOM_LEFT);
			    joysticks[1].setAlpha(joypadOpacity);
			}else{
			    joysticks[1].setAlign(Align.BOTTOM_RIGHT);
			    joysticks[1].setAlpha(joypadOpacity);
			}
			
			rudderAndThrottleJoystick.setNeedsUpdate();
		}
		
		for (int i=0; i<joysticks.length; ++i) {
		    JoystickBase joystick = joysticks[i];
		    
			if (joystick != null) {
				joystick.setInverseYWhenDraw(true);

				int margin = context.getResources().getDimensionPixelSize(R.dimen.hud_joy_margin);
				
				joystick.setMargin(0, margin, bottomBarBg.getHeight() + margin, margin);
			}
		}
		
		renderer.removeSprite(JOY_ID_LEFT);
		renderer.removeSprite(JOY_ID_RIGHT);

		if (rollAndPitchJoystick != null) {
			if (isLeftHanded) {
				renderer.addSprite(JOY_ID_RIGHT, rollAndPitchJoystick);
			}
			else{
				renderer.addSprite(JOY_ID_LEFT, rollAndPitchJoystick);
			}
		}
		
		if (rudderAndThrottleJoystick != null) {
			if (isLeftHanded) {
				renderer.addSprite(JOY_ID_LEFT, rudderAndThrottleJoystick);
			}
			else{
				renderer.addSprite(JOY_ID_RIGHT, rudderAndThrottleJoystick);
			}
		}
	}
	
	public JoystickBase getRollAndPitchJoystick()
	{
		return joysticks[0];
	}
	
	public JoystickBase getRudderAndThrottleJoystick()
	{
			return joysticks[1];
	}
	
	public void setInterfaceOpacity(float opacity)
	{
		if (opacity < 0 || opacity > 100.0f) {
			Log.w(TAG, "Can't set interface opacity. Invalid value: " + opacity);
			return;
		}
		
		joypadOpacity = opacity / 100f;
		
		Sprite joystick = renderer.getSprite(JOY_ID_LEFT);
		joystick.setAlpha(joypadOpacity);
		
		joystick = renderer.getSprite(JOY_ID_RIGHT);
		joystick.setAlpha(joypadOpacity);
	}

	public void setBatteryValue(final int percent)
	{
		if (percent > 100 || percent < 0) {
			Log.w(TAG, "Can't set battery value. Invalid value " + percent);
			return;
		}
				
		int imgNum = Math.round((float) percent / 100.0f * 4.0f);

		//txtBatteryStatus.setText(percent + "%");
		
		if (imgNum < 0)
			imgNum = 0;
		
		if (imgNum > 4) 
			imgNum = 4;

		if (batteryIndicator != null) {
			batteryIndicator.setValue(imgNum);
		}
	}
	
	public void setSettingsButtonEnabled(boolean enabled)
	{
		settingsBtn.setEnabled(enabled);
	}
	
	public void setDoubleTapClickListener(OnDoubleTapListener listener) 
	{
		gestureDetector.setOnDoubleTapListener(listener);	
	}
	
	public void onPause()
	{
		if (glView != null) {
			glView.onPause();
		}
		
		deviceOrientationManager.pause();
	}
	
	public void onResume()
	{
		if (glView != null) {
			glView.onResume();
		}
		
		deviceOrientationManager.resume();
		
		if (ipcProxy != null)
		    ipcProxy.doStartPreview();
	}

    //glView onTouch Event handler
	public boolean onTouch(View v, MotionEvent event)
	{
		boolean result = false;
		
		for (int i=0; i<buttons.length; ++i) {
			if (buttons[i].processTouch(v, event)) {
				result = true;
				break;
			}
		}
		
		if (result != true) {	
			gestureDetector.onTouchEvent(event);
			
			for (int i=0; i<joysticks.length; ++i) {
				JoystickBase joy = joysticks[i];
				if (joy != null) {
					if (joy.processTouch(v, event)) {
						result = true;
					}
				}
			}
		}
		
		return result;
	}
	
	public void onDestroy()
	{
	    renderer.clearSprites();
	    deviceOrientationManager.destroy();
	    unregisterAllBroadcastReceiver();
	    this.context.unbindService(mConnection);
	}

	public boolean onDown(MotionEvent e) 
	{
		return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) 
	{
		return false;
	}

	public void onLongPress(MotionEvent e) 
	{
    	// Left unimplemented	
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) 
	{
		return false;
	}

	public void onShowPress(MotionEvent e) 
	{
	    // Left unimplemented
	}

	public boolean onSingleTapUp(MotionEvent e) 
	{
		return false;
	}
	
	public View getRootView()
	{
	    if (glView != null) {
	        return glView;
	    }
	    
	    Log.w(TAG, "Can't find root view");
	    return null;
	}

	@Override
	public void interfaceOpacityValueDidChange(float newValue) {
		setInterfaceOpacity(newValue);
	}

	@Override
	public void leftHandedValueDidChange(boolean isLeftHanded) {
		this.isLeftHanded = isLeftHanded;

		setJoysticks();
		
		Log.e(TAG, "THRO:" + throttleChannel.getValue());
		
		getRudderAndThrottleJoystick().setYValue(throttleChannel.getValue());
	}

	@Override
	public void accModeValueDidChange(boolean isAccMode) {
		this.isAccMode = isAccMode;
		
		initJoystickListeners();
		
		if (isAccMode) {
			initJoysticks(JoystickType.ACCELERO);
		}
		else{
			initJoysticks(JoystickType.ANALOGUE);
		}
	}
	
    
	@Override
	public void headfreeModeValueDidChange(boolean isHeadfree) {
		if (settings.isHeadFreeMode()) {
			aux1Channel.setValue(1);
		}
		else {
			aux1Channel.setValue(-1);
		}
	}
	
	@Override
	public void aileronAndElevatorDeadBandValueDidChange(float newValue) {
	    JoystickBase rollAndPitchJoyStick  = getRollAndPitchJoystick();
        
	    rollAndPitchJoyStick.setXDeadBand(newValue);
	    rollAndPitchJoyStick.setYDeadBand(newValue);
	}

	@Override
	public void rudderDeadBandValueDidChange(float newValue) {
	    JoystickBase rudderAndThrottleStick  = getRudderAndThrottleJoystick();
        
	    rudderAndThrottleStick.setXDeadBand(newValue);
	}

	@Override
	public void onDeviceOrientationChanged(float[] orientation,
			float magneticHeading, int magnetoAccuracy) {
		  if (rollAndPitchJoystickPressed == false) {
	            pitchBase = orientation[PITCH];
	            rollBase = orientation[ROLL];
                aileronChannel.setValue(0.0f);
                elevatorChannel.setValue(0.0f);
                
               // Log.d(TAG, "before pressed ROLL:" + rollBase + ",PITCH:" + pitchBase);
	      }
		  else {
	            float x = (orientation[PITCH] - pitchBase);
	            float y = (orientation[ROLL] - rollBase);

	            if (isAccMode) {
					Log.d(TAG, "ROLL:" + (-x) + ",PITCH:" + y);
					
					if (Math.abs(x) > ACCELERO_TRESHOLD || Math.abs(y) > ACCELERO_TRESHOLD) {
			            if (settings.isBeginnerMode()) {
							aileronChannel.setValue(-x * BEGINNER_AILERON_CHANNEL_RATIO);
			                elevatorChannel.setValue(y * BEGINNER_ELEVATOR_CHANNEL_RATIO);
						}else{
							aileronChannel.setValue(-x);
			                elevatorChannel.setValue(y);
						}
					}
				}
	        }
	}

	@Override
	public void didConnect() {
		String state = context.getResources().getString(R.string.settings_item_connection_state_conneceted);
		stateTextView.setText(state);
	}

	@Override
	public void didDisconnect() {
		String state = context.getResources().getString(R.string.settings_item_connection_state_not_conneceted);
		stateTextView.setText(state);
	}

	@Override
	public void didFailToConnect() {
		String state = context.getResources().getString(R.string.settings_item_connection_state_not_conneceted);
		stateTextView.setText(state);
	}

	@Override
	public void beginnerModeValueDidChange(boolean isBeginnerMode) {
		
	}
	
	private void registerAllBroadcastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		this.context.registerReceiver(receiver, filter);
		IntentFilter decodeFilter = new IntentFilter();
		decodeFilter
				.addAction(IpcControlService.ACTION_NAVDATA_BATTERYSTATECHANGED);
		decodeFilter.addAction(IpcProxy.ACTION_DECODEMODE_CHANGED);
		decodeFilter.addAction(IpcProxy.ACTION_CONNECT_QUALITY_CHANGED);
		decodeFilter.addAction(ACTION_RESTART_PREVIEW);
		decodeFilter.addAction(IpcProxy.ACTION_REFRESH_DEBUG);
		decodeFilter.addAction(VideoSettingView.ACTION_DEBUG_PRIVEW);
		mLocalBroadcastManager.registerReceiver(receiver, decodeFilter);
	}
	

	private void unregisterAllBroadcastReceiver() {
		this.context.unregisterReceiver(receiver);
		mLocalBroadcastManager.unregisterReceiver(receiver);
	}
	
    private BroadcastReceiver receiver = new BroadcastReceiver() {

	@Override
	public void onReceive(Context arg0, Intent intent) {
	    // TODO Auto-generated method stub
	    String action = intent.getAction();
	    if (action.equals(Intent.ACTION_TIME_CHANGED)) {
	    	//*text_time.setText(SystemUtil.getCurrentFormatTime());
	    } else if (action.equals(Intent.ACTION_TIME_TICK)) {
	    	//*text_time.setText(SystemUtil.getCurrentFormatTime());
	    } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
		final int level = intent.getIntExtra(
			BatteryManager.EXTRA_LEVEL, 0);
		final int scale = intent.getIntExtra(
			BatteryManager.EXTRA_SCALE, 0);
		final int status = intent.getIntExtra(
			BatteryManager.EXTRA_STATUS, 0);
		//*battery_phone.setImageLevel(level / 25);
		//*battery_phone_text.setText(level + "%");
	    } else if (action
		    .equals(IpcControlService.ACTION_NAVDATA_BATTERYSTATECHANGED)) {
		final String str = intent
			.getStringExtra(IpcControlService.EXTRA_BATTERY_LEVEL);
		// Log.e(TAG, String.format("device level=%s", level));
		if (str == null)
		    return;
		String[] infos = str.trim().split(",");
		int level = Integer.parseInt(infos[0]);
		boolean plugin = false;
		if (infos.length > 1) {
		    plugin = Integer.parseInt(infos[1]) > 0 ? true : false;
		}
		if (isAcPlugin != plugin) { //表示正在充电
			/**
		    if (plugin) {
			battery_device
				.setImageResource(R.drawable.indication_ac_plugin);
			AnimationDrawable animation = (AnimationDrawable) battery_device
				.getDrawable();
			animation.start();
		    } else {
			battery_device
				.setImageResource(R.drawable.device_battery_level);
		    }
		    */
		}
		if (!plugin) { //表示没有在充电
		    deviceBatteryIndicator.setValue(Math.min(level / 25, 3));
		}
		isAcPlugin = plugin;
		//*battery_device_text.setText(level + "%");
		if (level < 10) {
		    //*showWarningMessage(getResources().getString(R.string.BATTERY_LOW_ALERT));
		} else {
		    //*hideWarningMessage();
		}
	    } else if (action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
	    	refreshWifiLevel();
	    } else if (action.equals(IpcProxy.ACTION_DECODEMODE_CHANGED)) {
		DebugHandler.logd(TAG, "IpcProxy.ACTION_DECODEMODE_CHANGED");
		onDecodeModeChanged(intent
			.getStringExtra(IpcProxy.EXTRA_DECODE_MODE));
	    } else if (action.equals(IpcProxy.ACTION_CONNECT_QUALITY_CHANGED)) {
		int state = intent.getIntExtra(IpcProxy.EXTRA_CONNECT_QUALITY,
			0);
		if (state == 1) {
		    //*hideWarningMessage();
		} else if (state == -1) {
		    //*showWarningMessage(getResources().getString(R.string.VIDEO_CONNECTION_ALERT));
		}
	    } else if (action.equals(ACTION_RESTART_PREVIEW)) {
		int mode = intent.getIntExtra("decodemode", 1);
		DebugHandler.logd(TAG, action + "---" + mode);
		ipcProxy.stopPreview();
		setDecodeMode(mode);
		ipcProxy.startPreview();
	    } else if (action.equals(IpcProxy.ACTION_REFRESH_DEBUG)) {
		String info = intent.getStringExtra(IpcProxy.EXTRA_DEBUG_INFO);
		// DebugHandler.logd(TAG, "info:"+info);
		//*if (debugInfo != null && info != null && info.length() > 0)
		//*    debugInfo.setText(info);
	    } else if (action.equals(VideoSettingView.ACTION_DEBUG_PRIVEW)) {
		//*debugSwitch.setVisibility(View.VISIBLE);
	    }
	}
    };

	public void setDecodeMode(int decodeMode) {
		DebugHandler.logd(TAG, "decodeMode is " + decodeMode);
		ipcProxy.setIpcDecMode(decodeMode);
		switch (decodeMode) {
		case 0: {
			videoStageHard.setVisibility(View.GONE);
			videoStageSoft.setVisibility(View.GONE);
			videoStageHard.setVisibility(View.VISIBLE);
			break;
		}
		case 1: {
			videoStageSoft.setVisibility(View.VISIBLE);
			videoStageHard.setVisibility(View.GONE);
			break;
		}
		case 2: {
			videoStageHard.setVisibility(View.GONE);
			videoStageSoft.setVisibility(View.GONE);
			videoStageHard.setVisibility(View.VISIBLE);
			break;
		}
		}
		VmcConfig.getInstance().setDecodeMode(decodeMode);
	}

	
	private class CustomOnRecordCompleteListener implements
			OnRecordCompleteListener {

		String filePath;

		public CustomOnRecordCompleteListener() {
		}

		public CustomOnRecordCompleteListener(String path) {
			filePath = path;
		}

		public void setPath(String path) {
			filePath = path;
		}

		@Override
		public void onRecordComplete(boolean isSuccess) {
			// TODO Auto-generated method stub
			if (isSuccess)
				MediaUtil.scanIpcMediaFile(HudViewController.this.context, filePath);
			else {
				DebugHandler.logWithToast(HudViewController.this.context,
						"Sorry!Record fail.", 2000);
			}
			ipcProxy.removeOnRecordCompleteListener(this);
		}

	};
	
	private void onDecodeModeChanged(String mode) {
		DebugHandler.logd(TAG, "onDecodeModeChanged:" + mode);
		if (mode.equals("softdec")) {
			videoStageHard.setVisibility(View.GONE);
			videoStageSoft.setVisibility(View.VISIBLE);
		} else {
			videoStageHard.setVisibility(View.VISIBLE);
			videoStageSoft.setVisibility(View.GONE);
		}
	}
	
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			controlService = ((IpcControlService.LocalBinder) service)
					.getService();
			controlService.getConnectStateManager().addConnectChangedListener(
					mOnIpcConnectChangedListener);
			// onDroneServiceConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			controlService.getConnectStateManager()
					.removeConnectChangedListener(mOnIpcConnectChangedListener);
			controlService = null;
		}
	};
    
    private OnIpcConnectChangedListener mOnIpcConnectChangedListener = new OnIpcConnectChangedListener() {

	@Override
	public void OnIpcConnected() {

	}

	@Override
	public void OnIpcDisConnected() {

	}

	@Override
	public void onIpcPaused() {

	}

	@Override
	public void onIpcResumed() {

	}
    };
    
    
    private void refreshWifiLevel() {
    	WifiManager wifiManager = (WifiManager)context.getSystemService(android.content.Context.WIFI_SERVICE);
    	WifiInfo info = wifiManager.getConnectionInfo();
    	if (info.getBSSID() != null) {
    		int strength = WifiManager.calculateSignalLevel(info.getRssi(), 4);
    		Log.d(TAG, String.format("strength=%d", strength));
    				
    		int imgNum = strength;

    		//txtBatteryStatus.setText(percent + "%");
    		
    		if (imgNum < 0)
    			imgNum = 0;
    		
    		if (imgNum > 3) 
    			imgNum = 3;

    		if (wifiIndicator != null) {
    			wifiIndicator.setValue(imgNum);
    		}
    	}
    }

    
    @Override
	public void viewWillAppear() {
		// TODO Auto-generated method stub
		super.viewWillAppear();
	}
	
	
	@Override
	public void viewWillDisappear() {
		// TODO Auto-generated method stub
		super.viewWillDisappear();
		
		// TODO Auto-generated method stub
		if (isStartRecord) {
		    AsyncTask<Void, Void, Void> stopRecordTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
			    // TODO Auto-generated method stub
			    if (!VmcConfig.getInstance().isStoreRemote()) {
				ipcProxy.doStopRecord();
				ipcProxy.onRecordComplete(true);
				ipcProxy.removeOnRecordCompleteListener(mCustomOnRecordCompleteListener);
			    } else {
				ipcProxy.stopRecordRemote();
			    }
			    isStartRecord = false;
			    ipcProxy.doStopPreview();
			    return null;
			}
		    };
		    stopRecordTask.execute();
		} else {
		    ipcProxy.doStopPreview();
		}
		
		/*
		if (mSoundPool != null)
		    mSoundPool.release();
		super.onStop();
		*/
	}

	@Override
	public void tringToConnect(String target) {
		// TODO Auto-generated method stub
		
	}
}
