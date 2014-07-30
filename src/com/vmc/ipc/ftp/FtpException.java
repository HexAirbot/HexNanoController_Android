package com.vmc.ipc.ftp;

public class FtpException extends Exception {
    public FtpException() {
	super();
    }
    
    public FtpException(String reason) {
	super(reason);
    }
}
