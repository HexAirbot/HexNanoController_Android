package com.hexairbot.hexmini;

import com.hexairbot.hexmini.SettingsDialog;
import com.hexairbot.hexmini.HexMiniApplication.AppStage;
import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.modal.OSDCommon;
import com.hexairbot.hexmini.modal.Transmitter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;


@SuppressLint("NewApi")
public class HudActivity extends FragmentActivity implements SettingsDialogDelegate, OnTouchListener, HudViewControllerDelegate{
	private static final String TAG = HudActivity.class.getSimpleName();
	
	private SettingsDialog settingsDialog;
    private HudViewController hudVC;
    
    public static final int REQUEST_ENABLE_BT = 1;
    
    boolean isFirstRun =  true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		hudVC = new HudViewController(this, this);	
		hudVC.onCreate();
		
		ApplicationSettings settings = HexMiniApplication.sharedApplicaion()
				.getAppSettings();

		/*
		if (settings.isFirstRun()) {
			Intent intent = new Intent(this, HelpActivity.class);
			startActivity(intent);
			settings.setIsFirstRun(false);
			settings.save();
		}
		*/
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		HexMiniApplication.sharedApplicaion().setAppStage(AppStage.HUD);
		hudVC.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		hudVC.onPause();
	}
	
    @Override
    public void onStart()
    {
        super.onStart();
        
        hudVC.viewWillAppear();
    }


    @Override
    public void onStop()
    {
        super.onStop();
        
        HexMiniApplication.sharedApplicaion().setAppStage(AppStage.HUD);
    }
	
    protected void showSettingsDialog()
    {        
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        
        if(settingsDialog == null){
        	Log.d(TAG, "settingsDialog is null");
        	settingsDialog = new SettingsDialog(this, this);
        }
        
        settingsDialog.show(ft, "settings");
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
    
	
    private ApplicationSettings getSettings()
    {
        return ((HexMiniApplication) getApplication()).getAppSettings();
    }

    
	@Override
	public void settingsBtnDidClick(View settingsBtn) {
		hudVC.setSettingsButtonEnabled(false);
		showSettingsDialog();		
	}
	
	
	public ViewController getViewController() {
		return hudVC;
	}
	
	 @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
	            finish();
	            return;
	        }
	        super.onActivityResult(requestCode, resultCode, data);
	    }
	 
	 @Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (Transmitter.sharedTransmitter().getBleConnectionManager() != null) {
			Transmitter.sharedTransmitter().transmmitSimpleCommand(OSDCommon.MSPCommnand.MSP_DISARM);
			Transmitter.sharedTransmitter().getBleConnectionManager().close();
		}
		
		hudVC.onDestroy();
		hudVC = null;
	}
}

