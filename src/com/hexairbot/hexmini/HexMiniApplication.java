package com.hexairbot.hexmini;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.res.AssetManager;
import android.provider.Settings;
import android.util.Log;

import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.util.FileHelper;

public class HexMiniApplication 
	extends Application 
{   
	private static final String TAG = HexMiniApplication.class.getSimpleName();
    
	private static HexMiniApplication instance;
	
	private ApplicationSettings settings;
	private FileHelper fileHelper;
	
	private AppStage appStage = AppStage.UNKNOWN;
	
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
		String settingsFileName = "Settings.plist";

		if (fileHelper.hasDataFile(settingsFileName) == false) {
			AssetManager assetManager = getAssets();

			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(settingsFileName);
				out = openFileOutput(settingsFileName, MODE_PRIVATE);

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
    }


	public AppStage getAppStage() {
		return appStage;
	}


	public void setAppStage(AppStage appStage) {
		this.appStage = appStage;
	}
}
