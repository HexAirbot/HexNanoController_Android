package com.vmc.ipc.view;


import com.hexairbot.hexmini.util.DebugHandler;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class VideoStageSurfaceHardware extends SurfaceView implements
	SurfaceHolder.Callback {

    private final static String TAG = "VideoStageSurfaceHardware";
    
    public VideoStageSurfaceHardware(Context context, AttributeSet attrs) {
	super(context, attrs);
	// TODO Auto-generated constructor stub
	init();
    }

    public VideoStageSurfaceHardware(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
	init();
    }

    private void init() {
	this.getHolder().addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	// getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	DebugHandler.logd(TAG,"surfaceChanged------1");
	if (holder.getSurface() != null) {
		DebugHandler.logd(TAG,"surfaceChanged------"+holder.getSurface());
	    nativeSetSurfaceView(holder.getSurface());
	}
    }

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
	DebugHandler.logd(TAG,"surfaceDestroyed------1");
	nativeSetSurfaceView(null);
    }

    public native void nativeSetSurfaceView(Surface surface);

}
