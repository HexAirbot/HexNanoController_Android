package com.hexairbot.hexmini.ipc.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class VideoStageSurfaceSoftware extends GLSurfaceView {

    public VideoStageSurfaceSoftware(Context context, AttributeSet attrs) {
	super(context, attrs);
	// TODO Auto-generated constructor stub
	init(context);
    }

    public VideoStageSurfaceSoftware(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
	init(context);
    }

    private void init(Context context) {
	this.setEGLContextClientVersion(2);
	this.setRenderer(new VideoStageRenderer(context, null));
    }

}
