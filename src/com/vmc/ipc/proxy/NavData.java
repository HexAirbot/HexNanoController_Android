package com.vmc.ipc.proxy;

public class NavData {

    public int batteryStatus;
    public boolean recording;
    public boolean cameraReady;
    public boolean recordReady;
    public boolean usbActive;
    public int usbRemainingTime;
    public int emergencyState;
    // public boolean flying;
    public int numFrames;
    public boolean initialized;

    public NavData() {
	batteryStatus = 0;
	// emergencyState = ERROR_STATE_NONE;
	// flying = false;
	initialized = false;
	usbActive = false;
	usbRemainingTime = -1;
	cameraReady = false;
	recordReady = false;
    }

    public void copyFrom(NavData navData) {
	this.batteryStatus = navData.batteryStatus;
	this.emergencyState = navData.emergencyState;
	// this.flying = navData.flying;
	this.initialized = navData.initialized;
	this.recording = navData.recording;
	this.numFrames = navData.numFrames;
	this.usbActive = navData.usbActive;
	this.usbRemainingTime = navData.usbRemainingTime;
	this.cameraReady = navData.cameraReady;
	this.recordReady = navData.recordReady;
    }
}
