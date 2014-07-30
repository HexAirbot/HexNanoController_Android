package com.vmc.ipc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vmc.ipc.config.VmcConfig;
import com.vmc.ipc.util.FontUtils.TYPEFACE;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class SystemUtil {

    private final static String TAG = "SystemUtil";

    public static String getCurrentFormatTime() {
	SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	String nowTime = format.format(new Date());
	return nowTime;
    }

    public static boolean isDebugModel(Context context) {
	PackageManager mgr = context.getPackageManager();
	try {
	    ApplicationInfo info = mgr.getApplicationInfo(
		    context.getPackageName(), 0);
	    return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE;
	} catch (NameNotFoundException e) {
	    Log.e(TAG, e.getMessage(), e);
	}
	return false;
    }
    
    public static void saveAvialableAp(Context context) {
	WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	WifiInfo info = wifiManager.getConnectionInfo();
	if(info == null) {
	    return;
	}
	String currentAp = info.getSSID();
	VmcConfig.getInstance().setLastAvailableIpcAp(currentAp);
    }
}
