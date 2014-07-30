package com.vmc.ipc.ftp;


public class FTPClient {
    public final static int FTP_CONNECT_MODE_PASV = 1;
    public final static int FTP_CONNECT_MODE_PORT = 2;
    public final static int FTP_TRANSFER_MODE_ASCII = 'A';
    public final static int FTP_TRANSFER_MODE_IMAGE = 'I';
    
    FtpCallbackListener listener = null;
    
    public FTPClient() {
	FtpInit();
    }
    
    public native int FtpInit();
    public native String FtpLastResponse();
    public native String FtpSysType();
    public native int FtpSize(String remoteFile,int transferMode);
    public native String FtpModDate(String remoteFile);
    public native void FtpSetCallback(FtpCallbackListener listener);
    public native void FtpClearCallback();
    
    public native int FtpConnect(String host);
    public native int FtpLogin(String user,String pass);
    public native int FtpQuit();
    public native int FtpSetConnectionMode(int connectMode);
    
    public native int FtpChdir(String path);
    public native int FtpMkDir(String path);
    public native int FtpRmdir(String path);
    public native int FtpDir(String outFile,String path); 
    public native int FtpNlst(String outFile,String path);
    public native int FtpCDUp();
    public native String FtpPwd();
    
    public native int FtpGet(String dstFile,String resFile,int transferMode);
    public native int FtpPut(String localFile,String remoteFile,int transferMode);
    public native int FtpDelete(String remoteFile);
    public native int FtpRename(String src,String dst);
    
}
