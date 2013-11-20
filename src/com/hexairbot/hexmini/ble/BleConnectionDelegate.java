package com.hexairbot.hexmini.ble;

public interface BleConnectionDelegate {
	public void didConnect(BleConnection connection);
	public void didDisconnect(BleConnection connection);
	public void didFailToConnect(BleConnection connection);
	public void didReceiveData(BleConnection connection, String data);
}
