package com.hexairbot.hexmini.ble;

public interface BleConnectinManagerDelegate {
	public void didConnect(BleConnectinManager manager);
	public void didDisconnect(BleConnectinManager manager);
	public void didFailToConnect(BleConnectinManager manager);
	public void didReceiveData(BleConnectinManager manager, byte[] data);
}
