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
    public native String FtpLastResponse() throws FtpException;
    public native String FtpSysType() throws FtpException;
    public native int FtpSize(String remoteFile,int transferMode) throws FtpException;
    public native String FtpModDate(String remoteFile) throws FtpException;
    public native void FtpSetCallback(FtpCallbackListener listener) throws FtpException;
    public native void FtpClearCallback() throws FtpException;
    
    public native int FtpConnect(String host) throws FtpException;
    public native int FtpLogin(String user,String pass) throws FtpException;
    public native int FtpQuit() throws FtpException;
    public native int FtpSetConnectionMode(int connectMode) throws FtpException;
    
    public native int FtpChdir(String path) throws FtpException;
    public native int FtpMkDir(String path) throws FtpException;
    public native int FtpRmdir(String path) throws FtpException;
    public native int FtpDir(String outFile,String path) throws FtpException; 
    public native int FtpNlst(String outFile,String path) throws FtpException;
    public native int FtpCDUp() throws FtpException;
    public native String FtpPwd() throws FtpException;
    
    public native int FtpGet(String dstFile,String resFile,int transferMode);
    public native int FtpPut(String localFile,String remoteFile,int transferMode);
    public native int FtpDelete(String remoteFile);
    public native int FtpRename(String src,String dst);
    
}
