package com.vmc.ipc.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConfigStoreHandler {
    
    public static final String PREFS_NAME = "client_config";
    
    SharedPreferences settings;
    
    public ConfigStoreHandler(Context context) {
	settings = context.getSharedPreferences(PREFS_NAME, 0);
    }
    
    public void putConfigString(String key,String value) {
	Editor editor = settings.edit();
	editor.putString(key, value);
	editor.commit();
    }
    
    public void putConfigInt(String key,int value) {
	Editor editor = settings.edit();
	editor.putInt(key, value);
	editor.commit();
    }
    
    public void putConfigBoolean(String key,boolean value) {
	Editor editor = settings.edit();
	editor.putBoolean(key, value);
	editor.commit();
    }
    
    public String getConfig(String key,String defaultvalue) {
	String value = settings.getString(key, defaultvalue);
	return value;
    }
    
    public int getConfig(String key,int defaultValue) {
	int value = settings.getInt(key, defaultValue);
	return value;
    }
    
    public boolean getConfig(String key,boolean defaultValue) {
	boolean value = settings.getBoolean(key, defaultValue);
	return value;
    }
    
    public void resetConfig() {
	settings.edit().clear();
    }
}
