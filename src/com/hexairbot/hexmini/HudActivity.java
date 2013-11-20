package com.hexairbot.hexmini;

import com.hexairbot.hexmini.SettingsDialog;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import com.hexairbot.hexmini.ui.*;
import com.hexairbot.hexmini.ui.joystick.AnalogueJoystick;
import com.hexairbot.hexmini.ui.joystick.JoystickBase;
import com.hexairbot.hexmini.ui.joystick.JoystickFactory;
import com.hexairbot.hexmini.ui.joystick.JoystickListener;
import com.hexairbot.hexmini.HudViewController.JoystickType;
import com.hexairbot.hexmini.modal.ApplicationSettings;
import com.hexairbot.hexmini.modal.Transmitter;


@SuppressLint("NewApi")
public class HudActivity extends FragmentActivity implements SettingsDialogDelegate, OnTouchListener, HudViewControllerDelegate{
	private static final String TAG = HudActivity.class.getSimpleName();
	
	private SettingsDialog settingsDialog;
    private HudViewController hudVC;
    
    public static final int REQUEST_ENABLE_BT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		hudVC = new HudViewController(this, this);	
		
		Transmitter.sharedTransmitter().start();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

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
	        // User chose not to enable Bluetooth.
	        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
	            finish();
	            return;
	        }
	        super.onActivityResult(requestCode, resultCode, data);
	    }

}

