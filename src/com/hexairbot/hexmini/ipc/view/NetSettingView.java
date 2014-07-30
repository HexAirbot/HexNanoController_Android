package com.hexairbot.hexmini.ipc.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import com.hexairbot.hexmini.R;
import com.vmc.ipc.config.VmcConfig;
import com.vmc.ipc.proxy.IpcProxy;
import com.vmc.ipc.service.ConnectStateManager;
import com.vmc.ipc.util.DebugHandler;
import com.vmc.ipc.util.FontUtils.TYPEFACE;

public class NetSettingView extends SettingView implements OnClickListener,OnCheckedChangeListener{

    public final static String TAG = "NetSettingView";
    
    private Context mContext = null;
    private IpcProxy proxy = null;
    private TextView currentConnectAp = null;
    private TextView lastConnectIpc = null;
    private CheckBox autoConnect = null;
    private Button selectAp = null;
    private WifiManager wifiManager;
    
    public NetSettingView(Context context,LayoutInflater inflater) {
	mContext = context;
	proxy = ConnectStateManager.getInstance(((Activity)mContext).getApplication()).getIpcProxy();
	content = inflater.inflate(R.layout.settings_page_net, null);
	currentConnectAp = (TextView)content.findViewById(R.id.ap_name);
	lastConnectIpc = (TextView)content.findViewById(R.id.last_available_ap);
	autoConnect = (CheckBox)content.findViewById(R.id.auto_connect_last_ap);
	selectAp = (Button)content.findViewById(R.id.select_ap);
	
	selectAp.setOnClickListener(this);
	autoConnect.setOnCheckedChangeListener(this);
	autoConnect.setChecked(VmcConfig.getInstance().isAutoConnect2AvailableAp());
	
	wifiManager = (WifiManager) mContext
		.getSystemService(Context.WIFI_SERVICE);
	checkAndEnableWifi();
	refreshCurrentAp();
	refreshLastConnectedIpc();
    }
    
    public void onStart() {
	initBroadcastReceiver();
    }
    
    public void onStop() {
	destroyBroadcastReceiver();
    }
    
    private void checkAndEnableWifi() {
	boolean wifiEnabled = wifiManager.isWifiEnabled();
	if (!wifiEnabled) {
	    DebugHandler.logWithToast(mContext, mContext.getResources().getString(R.string.enable_wifi_force), 2000);
	    int index = 0;
	    while(!wifiManager.setWifiEnabled(true) && index++ < 5) {
		DebugHandler.logWithToast(mContext, mContext.getResources().getString(R.string.enable_wifi_fail), 3000);
	    }
	}
    }

    public void initBroadcastReceiver() {
	IntentFilter filter = new IntentFilter();
	filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
	filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	mContext.registerReceiver(wifiStateChangedReceiver, filter);
    }
    
    public void destroyBroadcastReceiver() {
	mContext.unregisterReceiver(wifiStateChangedReceiver);
    }
    
    private void refreshCurrentAp() {
	String currentAp;
	WifiInfo info = wifiManager.getConnectionInfo();
	if(info == null) {
	    currentAp = mContext.getResources().getString(R.string.no_ap_to_connect);
	}
	currentAp = wifiManager.getConnectionInfo().getSSID();
	currentConnectAp.setText(currentAp);
    }
    

    private void refreshLastConnectedIpc() {
	String lastIpc = VmcConfig.getInstance().getLastAvailableIpcAp();
	if(lastIpc == null) {
	    lastIpc = mContext.getResources().getString(R.string.no_ap_to_connect);
	}
	lastConnectIpc.setText(lastIpc);
    }

    private BroadcastReceiver wifiStateChangedReceiver = new BroadcastReceiver() {

	@Override
	public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();
	    if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
		    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
			    WifiManager.WIFI_STATE_UNKNOWN);
	    } 
	    else if (action
		    .equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
		DebugHandler.logd(TAG,
			WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//		SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
	    }
	    refreshCurrentAp();
	}

    };
    
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	// TODO Auto-generated method stub
	VmcConfig.getInstance().setAutoConnect2AvailableAp(isChecked);
    }

    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	Intent intent = new Intent();
	intent.setAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
	mContext.startActivity(intent);
    }
}
