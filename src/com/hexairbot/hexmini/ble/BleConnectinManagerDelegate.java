package com.hexairbot.hexmini.ble;

public interface BleConnectinManagerDelegate {
	public void didConnect(BleConnectinManager manager, BleConnection connection);
	public void didDisconnect(BleConnectinManager manager, BleConnection connection);
	public void didFailToConnect(BleConnectinManager manager, BleConnection connection);
	public void didReceiveData(BleConnectinManager manager, BleConnection connection, String data);
}
