package com.hexairbot.hexmini.ipc.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.hexairbot.hexmini.R;
import com.vmc.ipc.config.VmcConfig;
import com.vmc.ipc.proxy.IpcProxy;
import com.vmc.ipc.service.ConnectStateManager;
import com.vmc.ipc.util.DebugHandler;

public class VideoSettingView extends SettingView implements OnCheckedChangeListener,CompoundButton.OnCheckedChangeListener{
    
    private static final String TAG = "VideoSettingView";
    
    public final static String ACTION_DEBUG_PRIVEW = "action_debug_privew";
    public final static String EXTRA_DEBUG_PRIVEW = "switch";
    
    Context mContext = null;
    RadioGroup decodeModeCheck;
    RadioButton decodeModeCheck_auto;
    RadioButton decodeModeCheck_software;
    RadioButton decodeModeCheck_hardware;
    RadioGroup resolutionCheck;
    RadioButton resolutionCheck720p;
    RadioButton resolutionCheck576p;
    RadioButton resolutionCheck360p;
    RadioButton resolutionCheck540p;
    RadioGroup bitrateCheck;
    RadioButton bitrateCheck_fps;
    RadioButton bitrateCheck_quality;
    CheckBox remoteStoreCheck;
    IpcProxy proxy;
    ImageView debugSwitch;
    boolean isFirstRead = true;
    int debugOpenClick = 0;
    
    public VideoSettingView(Context context,LayoutInflater inflater) {
	mContext = context;
	proxy = ConnectStateManager.getInstance(((Activity)mContext).getApplication()).getIpcProxy();
	content = inflater.inflate(R.layout.settings_page_video, null);
	
	decodeModeCheck = (RadioGroup)content.findViewById(R.id.preview_codec_mode);
	decodeModeCheck_auto = (RadioButton)content.findViewById(R.id.codec_mode_auto);
	decodeModeCheck_software = (RadioButton)content.findViewById(R.id.codec_mode_software);
	decodeModeCheck_hardware = (RadioButton)content.findViewById(R.id.codec_mode_hardware);
	
	resolutionCheck = (RadioGroup)content.findViewById(R.id.preview_resolution);
	resolutionCheck720p = (RadioButton)content.findViewById(R.id.preview_resolution720P);
	resolutionCheck576p = (RadioButton)content.findViewById(R.id.preview_resolution576P);
	resolutionCheck360p = (RadioButton)content.findViewById(R.id.preview_resolution360P);
	resolutionCheck540p = (RadioButton)content.findViewById(R.id.preview_resolution540P);
	
	bitrateCheck = (RadioGroup)content.findViewById(R.id.preview_bitrate_control);
	bitrateCheck_quality = (RadioButton)content.findViewById(R.id.preview_bitrate_quality);
	bitrateCheck_fps = (RadioButton)content.findViewById(R.id.preview_bitrate_fps);
	
	decodeModeCheck.setOnCheckedChangeListener(this);
	resolutionCheck.setOnCheckedChangeListener(this);
	bitrateCheck.setOnCheckedChangeListener(this);
	

	remoteStoreCheck = (CheckBox)content.findViewById(R.id.check_remote_store);
	remoteStoreCheck.setChecked(VmcConfig.getInstance().isStoreRemote());
	remoteStoreCheck.setOnCheckedChangeListener(this);
	
	debugSwitch = (ImageView)content.findViewById(R.id.debug_switch);
	debugSwitch.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		// TODO Auto-generated method stub
		debugOpenClick++;
		if(debugOpenClick>5 && debugOpenClick<=8) {
		    DebugHandler.logdWithToast(mContext, String.format("you also need to click %d times for opening debug.", 8-debugOpenClick), 100);
		    if(debugOpenClick>=8) {
			Intent intent = new Intent(ACTION_DEBUG_PRIVEW);
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
		    }
		}
	    }
	    
	});
	
	refreshDecodeConfig();
	refreshResolutionConfig();
	refreshBitarateConfig();
	isFirstRead = false;
    }
    
    public void refreshDecodeConfig() {
	int decodeMode = VmcConfig.getInstance().getDecodeMode();
	switch(decodeMode) {
        	case 0:{
        	    decodeModeCheck_auto.setChecked(true);
        	    break;
        	}
        	case 1:{
        	    decodeModeCheck_software.setChecked(true);
        	    break;
        	}
        	case 2:{
        	    decodeModeCheck_hardware.setChecked(true);
        	    break;
        	}
        	default:{
        	    DebugHandler.logdWithToast(mContext, "it is invalid decodeMode",2000);
        	}
	}
    }
    
    public void refreshResolutionConfig() {
	String resolution = VmcConfig.getInstance().getResolution();
	DebugHandler.logd(TAG, "refreshResolutionConfig()--"+resolution);
	if(resolution.equalsIgnoreCase("1280,720")) {
	    resolutionCheck720p.setChecked(true);
	}
	else if(resolution.equalsIgnoreCase("640,360") || resolution.equalsIgnoreCase("640,368")) {
	    resolutionCheck360p.setChecked(true);
	}
	else if(resolution.equalsIgnoreCase("960,540") || resolution.equalsIgnoreCase("960,544")) {
	    resolutionCheck540p.setChecked(true);
	}
	else if(resolution.equalsIgnoreCase("720,576")) {
	    resolutionCheck576p.setChecked(true);
	}
	else {
	    DebugHandler.logdWithToast(mContext, "it is invalid resolution", 2000);
	}
	
    }
    

    public void refreshBitarateConfig() {
	int bitrateControl = VmcConfig.getInstance().getBitrateControl();
	switch(bitrateControl) {
    	case 1:{
    	    bitrateCheck_fps.setChecked(true);
    	    break;
    	}
    	case 2:{
    	    bitrateCheck_quality.setChecked(true);
    	    break;
    	}
	default:
	    DebugHandler.logdWithToast(mContext, "it is invalid bitrate control parameter", 2000);
	}
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
	// TODO Auto-generated method stub
	DebugHandler.logd(TAG, String.format("radiogroup checkchanged (%d)", checkedId));
	if(isFirstRead) return;
	switch(checkedId) {
		case R.id.codec_mode_auto:{
		    restartPreview(0);
		    break;
		}
		case R.id.codec_mode_software:{
		    restartPreview(1);
		    break;
		}
		case R.id.codec_mode_hardware:{
		    restartPreview(2);
		    break;
		}
		case R.id.preview_resolution720P:{
		    proxy.setPreviewResolution(1280, 720);
		    break;
		}
		case R.id.preview_resolution576P:{
		    proxy.setPreviewResolution(720, 576);
		    break;
		}
		case R.id.preview_resolution360P:{
		    proxy.setPreviewResolution(640, 360);
		    break;
		}
		case R.id.preview_resolution540P:{
		    proxy.setPreviewResolution(960, 540);
		    break;
		}
		case R.id.preview_bitrate_fps:{
		    proxy.setBitrateControlType(1);
		    VmcConfig.getInstance().setBitrateControl(1);
		    break;
		}
		case R.id.preview_bitrate_quality:{
		    proxy.setBitrateControlType(2);
		    VmcConfig.getInstance().setBitrateControl(2);
		    break;
		}
		default:
		    DebugHandler.logd(TAG,"you have do nothing.");
	}
    }
    
    public void restartPreview(int decodeMode) {
    /*
	Intent intent = new Intent();
	intent.setAction(ControllerActivity.ACTION_RESTART_PREVIEW);
	intent.putExtra("decodemode", decodeMode);
	LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	*/
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	// TODO Auto-generated method stub
	int id = buttonView.getId();
	switch(id) {
        	case R.id.check_remote_store:{
        	    VmcConfig.getInstance().setStoreRemote(isChecked);
        	    DebugHandler.logd(TAG, "set isStoreRemote "+ VmcConfig.getInstance().isStoreRemote());
        	    break;
        	}
	}
    }
    
    public void reset() {
	    resolutionCheck720p.performClick();
	    decodeModeCheck_software.performClick();
	    bitrateCheck_quality.performClick();
	    bitrateCheck_fps.performClick();
	    remoteStoreCheck.setChecked(false);
    }
}
