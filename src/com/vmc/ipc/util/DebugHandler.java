/**
 * 
 */
/**
 * @author Administrator
 *
 */
package com.vmc.ipc.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class DebugHandler {

    public static Boolean debug = true;
    public static Boolean showServerSelect = false;

    public static void logd(String tag, String info) {
	if (debug)
	    Log.d(tag, info);
    }

    public static void logdWithToast(Context context, String info, int duration) {
	if (debug) {
    		logd(context.getPackageName(), info);
    		Toast.makeText(context, info, duration).show();
	}
    }
    
    public static void logInsist(String tag, String info) {
	Log.d(tag, info);
    }

    public static void logWithToast(Context context, String info, int duration) {
	logd(context.getPackageName(), info);
	Toast.makeText(context, info, duration).show();
    }
}