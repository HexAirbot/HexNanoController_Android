package com.hexairbot.hexmini;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.CamcorderProfile;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hexairbot.hexmini.R;

import com.hexairbot.hexmini.HexMiniApplication.AppStage;
import com.hexairbot.hexmini.ipc.activity.GalleryActivity;
import com.hexairbot.hexmini.ipc.activity.IpcAlertDialog;
import com.hexairbot.hexmini.ipc.activity.IpcAlertDialogHandler;
import com.hexairbot.hexmini.modal.OSDCommon;
import com.hexairbot.hexmini.modal.Transmitter;
import com.vmc.ipc.service.ApConnectService;
import com.vmc.ipc.service.ConnectStateManager;
import com.vmc.ipc.service.IpcControlService;
import com.vmc.ipc.service.OnIpcConnectChangedListener;
import com.vmc.ipc.util.DebugHandler;
import com.vmc.ipc.util.MediaUtil;

public class MainExActivity extends FragmentActivity implements
		OnIpcConnectChangedListener, SettingsDialogDelegate, OnTouchListener,
		HudViewControllerDelegate {

	private static final String TAG = "MainExActivity";
	public static final int REQUEST_ENABLE_BT = 1;
	private static final int DIALOG_WIFI_DISABLE = 1000;

	private SettingsDialog settingsDialog;
	private HudExViewController hudVC;
	boolean isFirstRun = true;

	private ImageButton btnHome;
	private ImageButton btnSetting;
	private ImageButton btnPictures;
	private ImageButton btnVideos;

	TextView ssid;
	TextView connectState;
	private boolean isStarted = false;

	private long lastToastTime = 0;

	private IpcControlService controlService = null;

	private LinearLayout splash;
	private static final int STOPSPLASH = 0;
	private static final long SPLASHTIME = 1000;

	// private Handler splashHandler = new Handler() {
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case STOPSPLASH:
	// SystemClock.sleep(4000);
	// splash.setVisibility(View.GONE);
	// break;
	// }
	// super.handleMessage(msg);
	// }
	// };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.d(TAG, "----onCreate");

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		Intent intent = new Intent();
		intent.setClass(this, IpcControlService.class);
		this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		Intent apintent = new Intent();
		apintent.setAction(ApConnectService.ACTION_CHECK_AND_ENABLE_WIFI);
		apintent.setClass(this, ApConnectService.class);
		this.startService(apintent);

		showSystemInfo();
		
		
		
		/*
		 * // ----------just for debug connectState = (TextView)
		 * this.findViewById(R.id.connect_state); View serverSelect =
		 * this.findViewById(R.id.server_select); if
		 * (DebugHandler.showServerSelect) {
		 * connectState.setVisibility(View.VISIBLE);
		 * serverSelect.setVisibility(View.VISIBLE); ssid = (TextView)
		 * this.findViewById(R.id.ssid); refreshWifiInfo(); Spinner s1 =
		 * (Spinner) findViewById(R.id.spinner1); final ArrayAdapter<String>
		 * adapter = new ArrayAdapter<String>(this,
		 * android.R.layout.simple_expandable_list_item_1);
		 * adapter.add("请�?择服务器:");
		 * adapter.add("rtmp://192.168.1.1/live/stream");
		 * adapter.add("rtmp://10.0.14.153/live/stream");
		 * adapter.add("rtmp://10.0.12.191/live/stream");
		 * s1.setAdapter(adapter); s1.setOnItemSelectedListener(new
		 * OnItemSelectedListener() {
		 * 
		 * @Override public void onItemSelected(AdapterView<?> parent, View
		 * view, int position, long id) { Log.d(TAG, "Spinner1: position=" +
		 * position + " id=" + id); if (id > 0)
		 * connectIPC(adapter.getItem(position)); }
		 * 
		 * @Override public void onNothingSelected(AdapterView<?> parent) { //
		 * showToast("Spinner1: unselected"); } }); } else {
		 * serverSelect.setVisibility(View.GONE); } // //////////
		 */
		ConnectStateManager mConnectStateManager = ConnectStateManager.getInstance(this.getApplication());
		mConnectStateManager.init();
		mConnectStateManager.connect("rtmp://192.168.1.1/live/stream");
		valiateConnectState();
		// setContentView(R.layout.hud_view_controller_framelayout);
		// splash = (LinearLayout) findViewById(R.id.splash);
		//
		// Message msg = new Message();
		// msg.what = STOPSPLASH;
		// splashHandler.sendMessageDelayed(msg, SPLASHTIME);

		hudVC = new HudExViewController(this, this);
		hudVC.onCreate();
		hudVC.onResume();
		
		initBroadcastReceiver();
	}

	private void connectIPC(String address) {
		ConnectStateManager mConnectStateManager = ConnectStateManager
				.getInstance(this.getApplication());
		mConnectStateManager.connect(address);
	}

	private void valiateConnectState() {
		// boolean wifiEnabled = checkWifiEnable();
		// if (!wifiEnabled)
		// return;
		String reason = null;
		if (controlService == null) {
			reason = "can not control your device,because controlService is null.";
		} else {
			int state = controlService.getConnectStateManager().getState();
			if (state == ConnectStateManager.CONNECTING
					|| state == ConnectStateManager.DISCONNECTED) {
				reason = "your phone was not connect to the device.";
			}
		}
		if (reason != null) {
			//DebugHandler.logWithToast(this, reason, 2000);
			return;
		}
	}

	private boolean checkWifiEnable() {
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		boolean wifiEnabled = wifiManager.isWifiEnabled();
		if (!wifiEnabled) {
			showDialogWhenWifiCheckFail();
		}
		return wifiEnabled;
	}

	public void initBroadcastReceiver() {
		// IntentFilter filter = new IntentFilter();
		// filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		// filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		// filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		// this.registerReceiver(connectStateChangedReceiver, filter);
		IntentFilter dd = new IntentFilter();
		dd.addAction(ConnectStateManager.ACTION_CONNECT_STATE_CHANGED);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				connectStateChangedReceiver, dd);
	}

	public void destroyBroadcastReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				connectStateChangedReceiver);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			controlService = ((IpcControlService.LocalBinder) service)
					.getService();
			controlService.getConnectStateManager().addConnectChangedListener(
					MainExActivity.this);
			// onDroneServiceConnected();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			controlService.getConnectStateManager()
					.removeConnectChangedListener(MainExActivity.this);
			controlService = null;
		}
	};

	private void showDialogWhenWifiCheckFail() {
		IpcAlertDialog dialog = new IpcAlertDialog();
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(R.string.wifi_disabled);
		dialog.setPbtn_text(R.string.enable_wifi);
		dialog.setHandler(new IpcAlertDialogHandler() {

			@Override
			public void positive() {
				// TODO Auto-generated method stub
				Intent intent = new Intent("android.settings.WIFI_SETTINGS");
				MainExActivity.this.startActivity(intent);
			}

			@Override
			public void negtive() {
				// TODO Auto-generated method stub

			}
		});
		dialog.show(getSupportFragmentManager(), "wificheck");
	}

	private BroadcastReceiver connectStateChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN);
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)
					&& (state == WifiManager.WIFI_STATE_DISABLED || state == WifiManager.WIFI_STATE_UNKNOWN)) {
				//showDialogWhenWifiCheckFail();
			} else if (action
					.equals(ConnectStateManager.ACTION_CONNECT_STATE_CHANGED)) {
				DebugHandler.logd(TAG,
						ConnectStateManager.ACTION_CONNECT_STATE_CHANGED);
			}
			//refreshWifiInfo();
		}

	};

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == btnHome) {
				startWebHomeActivity();
			} else if (v == btnSetting) {
				// startSettingsActivity();
			} else if (v == btnVideos) {
				startMediaActivity(MediaUtil.MEDIA_TYPE_VIDEO);
			} else if (v == btnPictures) {
				startMediaActivity(MediaUtil.MEDIA_TYPE_IMAGE);
			}
		}
	};

	private void startMediaActivity(int type) {
		// if(!VmcConfig.getInstance().isStoreRemote()) {
		if (false) {
			if (MediaUtil.hasIpcMediaFile(type)) {
				// if(true) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
					intent.setType("vnd.android.cursor.dir/image");// ͼƬ�б�
				} else {
					intent.setType("vnd.android.cursor.dir/video");// ��Ƶ�б�
				}
				intent.putExtra("type", type);
				intent.setClass(this, GalleryActivity.class);
				this.startActivity(intent);
			} else {
				DebugHandler.logWithToast(this,
						"You don't have any multimedia files.", 2000);
			}
		} else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			if (type == MediaUtil.MEDIA_TYPE_IMAGE) {
				intent.setType("vnd.android.cursor.dir/image");
			} else {
				intent.setType("vnd.android.cursor.dir/video");
			}
			intent.putExtra("type", type);
			intent.putExtra("browser_type", GalleryActivity.BROWSER_TYPE_REMOTE);
			intent.setClass(this, GalleryActivity.class);
			this.startActivity(intent);
		}
	}

	private void startWebHomeActivity() {
		// Intent intent = new Intent();
		// intent.setClass(MainActivity.this, WebHomeActivity.class);
		// this.startActivity(intent);

		String url;
		int state = ConnectStateManager.getInstance(getApplication())
				.getState();
		if (state == ConnectStateManager.CONNECTING
				|| state == ConnectStateManager.DISCONNECTED) {
			url = "http://www.vimicro.com.cn";
		} else {
			url = "http://192.168.1.1";
		}
		Uri u = Uri.parse(url);
		Intent it = new Intent(Intent.ACTION_VIEW, u);
		this.startActivity(it);
	}

	private void showSystemInfo() {
		Display wm = this.getWindow().getWindowManager().getDefaultDisplay();
		DebugHandler.logd(TAG, "screen.w=" + wm.getWidth());
		DebugHandler.logd(TAG, "screen.w=" + wm.getHeight());
		DebugHandler.logd(TAG, "screen.getPixelFormat=" + wm.getPixelFormat());
		dumpVideoCapabilitiesInfo();
	}

	private void refreshWifiInfo() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (ssid != null && wifiInfo != null) {
			ssid.setText(wifiInfo.getSSID());
		}
	}

	@SuppressLint("NewApi")
	private void dumpVideoCapabilitiesInfo() {
		// Here we try to use different methods to determine the maximum video
		// frame size
		// that device supports

//		Log.i(TAG, "=== DEVICE VIDEO SUPPORT ====>>>>>>>>>");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			Log.i(TAG, "Codecs available to the system: ");
			for (int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
				MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);

				String[] supportedTypes = info.getSupportedTypes();
				StringBuilder supportedTypesBuilder = new StringBuilder();

				for (int j = 0; j < supportedTypes.length; ++j) {
					supportedTypesBuilder.append(supportedTypes[j]);
					if (j < (supportedTypes.length - 1)) {
						supportedTypesBuilder.append(", ");
					}
				}

				Log.i(TAG, info.getName() + " , supported types: "
						+ supportedTypesBuilder.toString());
				;
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
				Log.i(TAG, "Device supports HD video [720p]");
			} else if (CamcorderProfile
					.hasProfile(CamcorderProfile.QUALITY_480P)) {
				Log.i(TAG, "Device supports regular video [480p]");
			} else if (CamcorderProfile
					.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
				Log.i(TAG, "Device supports low quality video [240p]");
			} else {
				Log.w(TAG, "Can't determine video support of this device.");
			}
		}

		CamcorderProfile prof = CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH);
		if (prof != null) {
			Log.i(TAG, "Highest video frame size for this device is ["
					+ prof.videoFrameWidth + ", " + prof.videoFrameHeight + "]");
		} else {
			Log.w(TAG, "Unable to determine highest possible video frame size.");
		}

		Log.i(TAG, "<<<<<<<<<=== DEVICE VIDEO SUPPORT ===");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		destroyBroadcastReceiver();
		
		Log.e("onDestroy", "");

		if (Transmitter.sharedTransmitter().getBleConnectionManager() != null) {
			Transmitter.sharedTransmitter().transmmitSimpleCommand(
					OSDCommon.MSPCommnand.MSP_DISARM);
			Transmitter.sharedTransmitter().getBleConnectionManager().close();
		}

		hudVC.onDestroy();
		hudVC = null;

		this.unbindService(mConnection);
		Thread destroy = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		destroy.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		// mConnectStateManager.pause();
		super.onPause();
		//hudVC.onPause();
		
		Log.e("onPause", "onPause");
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
	}

	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		
		Log.e("onRestart", "onRestart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		HexMiniApplication.sharedApplicaion().setAppStage(AppStage.HUD);
		//hudVC.onResume();
		
		Log.e("onResume", "onResume");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		isStarted = true;
		// checkWifiEnable();
		//initBroadcastReceiver();

		hudVC.viewWillAppear();
		
		Log.e("onStart", "");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isStarted = false;
		//destroyBroadcastReceiver();
		HexMiniApplication.sharedApplicaion().setAppStage(AppStage.UNKNOWN);
		
		Log.e("onStop()", "onStop");
	}

	@Override
	public void OnIpcConnected() {
		// TODO Auto-generated method stub
		if (connectState != null)
			connectState.setText("connected");
		DebugHandler.logWithToast(this,
				this.getResources().getString(R.string.connect_success), 2000);
		String[] keys = { "device", "version" };
		String[] values = { "android", "4.1.2" };
		ConnectStateManager.getInstance(getApplication()).getIpcProxy()
				.sendMessage2Server(keys, values);
	}

	@Override
	public void OnIpcDisConnected() {
		// TODO Auto-generated method stub
		if (connectState != null)
			connectState.setText("disconnected");
		// long current = System.currentTimeMillis();
		// if(isStarted && current - lastToastTime > 2000) {
		// DebugHandler.logWithToast(this,
		// this.getResources().getString(R.string.connect_fail), 1000);
		// lastToastTime = current;
		// }
	}

	@Override
	public void onIpcPaused() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onIpcResumed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "----onConfigurationChanged");
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// Nothing need to be done here

		} else {
			// Nothing need to be done here
		}
	}

	@Override
	public void prepareDialog(SettingsDialog dialog) {

	}

	@Override
	public void onDismissed(SettingsDialog settingsDialog) {
		
		hudVC.setSettingsButtonEnabled(true);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}

	@Override
	public void settingsBtnDidClick(View settingsBtn) {
		hudVC.setSettingsButtonEnabled(false);
		showSettingsDialog();
	}

	public ViewController getViewController() {
		return hudVC;
	}

	protected void showSettingsDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);

		if (settingsDialog == null) {
			Log.d(TAG, "settingsDialog is null");
			settingsDialog = new SettingsDialog(this, this);
		}

		settingsDialog.show(ft, "settings");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
