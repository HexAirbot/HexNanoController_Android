package com.hexairbot.hexmini.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
}
