package com.hexairbot.hexmini.sensors;

public interface DeviceOrientationChangeDelegate
{
    public void onDeviceOrientationChanged(float[] orientation, float magneticHeading, int magnetoAccuracy);
}
