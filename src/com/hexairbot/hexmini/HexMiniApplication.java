package com.hexairbot.hexmini;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.res.AssetManager;
import android.util.Log;


import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.util.FileHelper;
//import com.vmc.ipc.config.ConfigStoreHandler;
//import com.vmc.ipc.config.VmcConfig;
//import com.vmc.ipc.util.MediaUtil;
import com.hexairbot.hexmini.ui.Text;


public class HexMiniApplication extends Application 
{   
	private static final String TAG = HexMiniApplication.class.getSimpleName();
    
	private static HexMiniApplication instance;
	
	private ApplicationSettings settings;
	private FileHelper fileHelper;
	
	private AppStage appStage = AppStage.UNKNOWN;
	
	private boolean isFullDuplex;


	public boolean isFullDuplex() {
		return isFullDuplex;
	}


	public void setFullDuplex(boolean isFullDuplex) {
		this.isFullDuplex = isFullDuplex;
	}


	static {
    	System.loadLibrary("vmcipc");
    }
	
    
	private Text debugTextView;
	
	
	public Text getDebugTextView() {
		return debugTextView;
	}


	public void setDebugTextView(Text debugTextView) {
		this.debugTextView = debugTextView;
	}

	private float alt;
	
	public void setCurrentAlt(float alt_){
		alt = alt_;
	}
	
	public float getCurrentAlt(){
		return alt;
	}
	
	public enum AppStage{
		UNKNOWN,
		HUD,
		SETTINGS
	};
	  
	
	@SuppressLint("NewApi")
    @Override
	public void onCreate() 
	{
		super.onCreate();
		Log.d(TAG, "OnCreate");
		
		instance = this;

		fileHelper = new FileHelper(this);
		
		copyDefaultSettingsFileIfNeeded();
		
		settings = new ApplicationSettings(getFilesDir() + "/Settings.plist");

//		MediaUtil.createIPCDir();
//		VmcConfig.getInstance().setConfigStoreHandler(new ConfigStoreHandler(this));
//		VmcConfig.getInstance().initNativeConfig(MediaUtil.getAppConfigDir());
	}
	
	
	@Override
	public void onTerminate() 
	{
		Log.d(TAG, "OnTerminate");
		super.onTerminate();
	}

	
	public ApplicationSettings getAppSettings()
	{
		return settings;
	}
	
	public FileHelper getFileHelper(){
		return fileHelper;
	}
	
	
    public static HexMiniApplication sharedApplicaion() {  
        return instance;  
    }  
    
    
    private void copyDefaultSettingsFileIfNeeded(){
		String settingsFileName        = "Settings.plist";        //user
		String defaultSettingsFileName = "DefaultSettings.plist"; //default

		if (fileHelper.hasDataFile(defaultSettingsFileName) == false) {
			AssetManager assetManager = getAssets();
			
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(settingsFileName);
				out =  openFileOutput(defaultSettingsFileName, MODE_PRIVATE);

				byte[] buffer = new byte[1024];
				int read;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}

				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (IOException e) {
				Log.e("tag", "Failed to copy asset file: " + settingsFileName, e);
			}
		}
		
		if (fileHelper.hasDataFile(settingsFileName) == false) {
			AssetManager assetManager = getAssets();
			
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(settingsFileName); //从Asset里面复制
				out =  openFileOutput(settingsFileName, MODE_PRIVATE);

				byte[] buffer = new byte[1024];
				int read;
				while ((read = in.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}

				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (IOException e) {
				Log.e("tag", "Failed to copy asset file: " + settingsFileName, e);
			}
		}
		else{
			ApplicationSettings userSettings = new ApplicationSettings(getFilesDir() + "/" + settingsFileName);
			
			if (userSettings.getSettingsVersion().equals("1.0.0")) { //old settings file, needed to be updated
				fileHelper.delDataFile(settingsFileName);
				fileHelper.delDataFile(defaultSettingsFileName);
				copyDefaultSettingsFileIfNeeded();
			}
		}
    }


	public AppStage getAppStage() {
		return appStage;
	}


	public void setAppStage(AppStage appStage) {
		this.appStage = appStage;
	}
}
