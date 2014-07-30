package com.hexairbot.hexmini.ipc.view;

import android.view.View;

public class SettingView {
    String title = null;
    View content;
    
    public void setTitle(String title) {
	this.title = title;
    }
    public String getTitle() {
	return title;
    }
    
    public View getContent() {
	return content;
    }
    
    public void setContent(View content) {
	this.content = content;
    }
    
    public void onStart() {
	
    }

    public void onStop() {
	
    }
    
    public void reset() {
	
    }
}
