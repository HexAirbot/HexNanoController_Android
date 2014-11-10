package com.hexairbot.hexmini;

import com.hexairbot.hexmini.HexMiniApplication.AppStage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment")
public class GalleryDialog extends DialogFragment {
	
    private static final String TAG = GalleryDialog.class.getSimpleName();
    
    private SettingsViewController settingsVC;
    private Context context;
    private GalleryDialogDelegate delegate;
    
    
    public GalleryDialog(Context context, GalleryDialogDelegate delegate)
    {
        super();
        this.delegate = delegate;
        this.context = context;
    }


    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.HexMiniTheme_SettingScreen);
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (delegate != null) {
            this.delegate.prepareDialog(this);
        }
    	
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.settings_screen, container, false);
            
            if (this.context == getActivity()) {
    			Log.d(TAG, "this.context == getActivity()");
    		}
            
        settingsVC = new SettingsViewController(this.context, inflater, v, (SettingsViewControllerDelegate)(((MainExActivity) delegate).getViewController()));
            
        initListeners();
        
        return v;
    }


    @Override
    public void onStart()
    {
        super.onStart();
        
        settingsVC.viewWillAppear();
        
        HexMiniApplication.sharedApplicaion().setAppStage(AppStage.SETTINGS);

        Log.d(TAG,"onStart sendBleEnableRequest");
    }


    @Override
    public void onStop()
    {
        super.onStop();
        
        HexMiniApplication.sharedApplicaion().setAppStage(AppStage.HUD);
        
        Log.d(TAG, "settingsVC viewWillDisappear");
        
        settingsVC.viewWillDisappear();
    }
  
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	
    }
    
    
    public void onOkClicked(View v)
    {
        dismiss();
    }


    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);

        if (delegate != null) {
            delegate.onDismissed(this);
        }
    }


    private void initListeners()
    {
        settingsVC.setBackBtnOnClickListner(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
		        switch (v.getId()) {
		        case R.id.backBtn:
		            dismiss();
		            break;
		        }
			}
        }); 
    }
    

    public void onClick(View v)
    {
        switch (v.getId()) {
        case R.id.backBtn:
            dismiss();
            break;
        }
    }
    
    
    public ViewController getViewController() {
		return settingsVC;
	}

}
