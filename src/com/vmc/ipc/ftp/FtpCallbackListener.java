package com.vmc.ipc.ftp;

public interface FtpCallbackListener {
	public void onProgress(int progress,int totle);
	public void onError(String err);
}
