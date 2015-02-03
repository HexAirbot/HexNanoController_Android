package com.hexairbot.hexmini.modal;


import android.util.Log;

import com.hexairbot.hexmini.modal.OSDCommon;

public class OSDData {
	private static final String TAG = OSDData.class.getSimpleName();
	
	private static int IDLE         = 0;
	private static int HEADER_START = 1;
	private static int HEADER_M     = 2;
	private static int HEADER_ARROW = 3;
	private static int HEADER_SIZE  = 4;
	private static int HEADER_CMD   = 5;
	private static int HEADER_ERR   = 6;
	
	public float getGyroX() {
		return gyroX;
	}


	public void setGyroX(float gyroX) {
		this.gyroX = gyroX;
	}


	public float getGyroY() {
		return gyroY;
	}


	public void setGyroY(float gyroY) {
		this.gyroY = gyroY;
	}


	public float getGyroZ() {
		return gyroZ;
	}


	public void setGyroZ(float gyroZ) {
		this.gyroZ = gyroZ;
	}


	public float getAccX() {
		return accX;
	}


	public void setAccX(float accX) {
		this.accX = accX;
	}


	public float getAccY() {
		return accY;
	}


	public void setAccY(float accY) {
		this.accY = accY;
	}


	public float getAccZ() {
		return accZ;
	}


	public void setAccZ(float accZ) {
		this.accZ = accZ;
	}


	public float getMagX() {
		return magX;
	}


	public void setMagX(float magX) {
		this.magX = magX;
	}


	public float getMagY() {
		return magY;
	}


	public void setMagY(float magY) {
		this.magY = magY;
	}


	public float getMagZ() {
		return magZ;
	}


	public void setMagZ(float magZ) {
		this.magZ = magZ;
	}


	public float getAltitude() {
		return altitude;
	}


	public void setAltitude(float altitude) {
		this.altitude = altitude;
	}


	public float getHead() {
		return head;
	}


	public void setHead(float head) {
		this.head = head;
	}


	public float getAngleX() {
		return angleX;
	}


	public void setAngleX(float angleX) {
		this.angleX = angleX;
	}


	public float getAngleY() {
		return angleY;
	}


	public void setAngleY(float angleY) {
		this.angleY = angleY;
	}


	public int getGpsSatCount() {
		return gpsSatCount;
	}


	public void setGpsSatCount(int gpsSatCount) {
		this.gpsSatCount = gpsSatCount;
	}


	public int getGpsLongitude() {
		return gpsLongitude;
	}


	public void setGpsLongitude(int gpsLongitude) {
		this.gpsLongitude = gpsLongitude;
	}


	public int getGpsLatitude() {
		return gpsLatitude;
	}


	public void setGpsLatitude(int gpsLatitude) {
		this.gpsLatitude = gpsLatitude;
	}


	public int getGpsAltitude() {
		return gpsAltitude;
	}


	public void setGpsAltitude(int gpsAltitude) {
		this.gpsAltitude = gpsAltitude;
	}


	public int getGpsDistanceToHome() {
		return gpsDistanceToHome;
	}


	public void setGpsDistanceToHome(int gpsDistanceToHome) {
		this.gpsDistanceToHome = gpsDistanceToHome;
	}


	public int getGpsDirectionToHome() {
		return gpsDirectionToHome;
	}


	public void setGpsDirectionToHome(int gpsDirectionToHome) {
		this.gpsDirectionToHome = gpsDirectionToHome;
	}


	public int getGpsFix() {
		return gpsFix;
	}


	public void setGpsFix(int gpsFix) {
		this.gpsFix = gpsFix;
	}


	public int getGpsUpdate() {
		return gpsUpdate;
	}


	public void setGpsUpdate(int gpsUpdate) {
		this.gpsUpdate = gpsUpdate;
	}


	public int getGpsSpeed() {
		return gpsSpeed;
	}


	public void setGpsSpeed(int gpsSpeed) {
		this.gpsSpeed = gpsSpeed;
	}


	public float getDebug1() {
		return debug1;
	}


	public void setDebug1(float debug1) {
		this.debug1 = debug1;
	}


	public float getDebug2() {
		return debug2;
	}


	public void setDebug2(float debug2) {
		this.debug2 = debug2;
	}


	public float getDebug3() {
		return debug3;
	}


	public void setDebug3(float debug3) {
		this.debug3 = debug3;
	}


	public float getDebug4() {
		return debug4;
	}


	public void setDebug4(float debug4) {
		this.debug4 = debug4;
	}


	public int getMode() {
		return mode;
	}


	public void setMode(int mode) {
		this.mode = mode;
	}


	private int version;
	
	private int multiType;

	private float gyroX;
	private float gyroY;
	private float gyroZ;

	private float accX;
	private float accY;
	private float accZ;

	private float magX;
	private float magY;
	private float magZ;

	private float altitude;
	private float head;
	private float angleX;
	private float angleY;

	private int gpsSatCount;
	private int gpsLongitude;
	private int gpsLatitude;
	private int gpsAltitude;
	private int gpsDistanceToHome;
	private int gpsDirectionToHome;
	private int gpsFix;
	private int gpsUpdate;
	private int gpsSpeed;

	private float rcThrottle;
	private float rcYaw;
	private float rcRoll;
	private float rcPitch;
	private float rcAux1;
	private float rcAux2;
	private float rcAux3;
	private float rcAux4;

	private float debug1;
	private float debug2;
	private float debug3;
	private float debug4;


	private int pMeterSum;
	private float vBat;

	private int cycleTime;
	private int i2cError;

	private int mode;
	private int present;
	
	
	private int c_state;
	private boolean err_rcvd;
	private byte checksum;
	private byte cmd;
	private int offset, dataSize;
	private byte inBuf[];
	private int p;
    
    float mot[], servo[];
    long currentTime,mainInfoUpdateTime,attitudeUpdateTime;
    
    private int absolutedAccZ;
    private int flightState;
    
    private OSDDataDelegate delegate;
	
	public OSDDataDelegate getDelegate() {
		return delegate;
	}


	public void setDelegate(OSDDataDelegate delegate) {
		this.delegate = delegate;
	}


	public OSDData() {
		inBuf = new byte[256];
		mot = new float[8];
		servo = new float[8];
	}

	
	public void parseRawData(byte[] data){
	    int byteCount = data.length;
	    
	    //byte * dataPtr = (byte *)data.bytes;
	    
	    int idx;
	    byte c;
	    
	    for (int byteIdx = 0; byteIdx < byteCount; byteIdx++) {
	        c = data[byteIdx];
	        
	        if (c_state == IDLE) {
	            c_state = (c=='$') ? HEADER_START : IDLE;
	        } else if (c_state == HEADER_START) {
	            c_state = (c=='M') ? HEADER_M : IDLE;
	        } else if (c_state == HEADER_M) {
	            if (c == '>') {
	                c_state = HEADER_ARROW;
	            } else if (c == '!') {
	                c_state = HEADER_ERR;
	            } else {
	                c_state = IDLE;
	            }
	        } else if (c_state == HEADER_ARROW || c_state == HEADER_ERR) {
	            /* is this an error message? */
	            err_rcvd = (c_state == HEADER_ERR);        /* now we are expecting the payload size */
	            dataSize = (c&0xFF);
	            /* reset index variables */
	            p = 0;
	            offset = 0;
	            checksum = 0;
	            checksum ^= (c&0xFF);
	            /* the command is to follow */
	            c_state = HEADER_SIZE;
	        } else if (c_state == HEADER_SIZE) {
	            cmd = (byte)(c&0xFF);
	            checksum ^= (c&0xFF);
	            c_state = HEADER_CMD;
	        } else if (c_state == HEADER_CMD && offset < dataSize) {
	            checksum ^= (c&0xFF);
	            inBuf[offset++] = (byte)(c&0xFF);
	        } else if (c_state == HEADER_CMD && offset >= dataSize) {
	            /* compare calculated and transferred checksum */
	            if ((checksum&0xFF) == (c&0xFF)) {
	                if (err_rcvd) {
	                    //printf("Copter did not understand request type %d\n", c);
	                     c_state = IDLE;
	                    
	                } else {
	                    /* we got a valid response packet, evaluate it */                  
	                    evaluateCommand(cmd, dataSize);
	                }
	            } else {
	                Log.d(TAG, "invalid checksum for command" + ((int)(cmd&0xFF)) + ": " + (checksum&0xFF) + " expected, got " + (int)(c&0xFF));
	                
	                Log.d(TAG, "<" + (cmd&0xFF) + " " +  (dataSize&0xFF) + ">");
	                
	                for (idx = 0; idx < dataSize; idx++) {
	                    if (idx != 0) { 
	                    	Log.d(TAG, " ");   
	                    }
	                    Log.d(TAG, "" + (inBuf[idx] & 0xFF));
	                }
	                
	                Log.d(TAG, "} [" + c + "]\n");
	                
	                
	                String dataStr = new String(inBuf, 0, dataSize);
	                
	                Log.d(TAG, dataStr + "\n");
	            }
	            c_state = IDLE;
	        }

	    }
	}
	
	private float read32(){	    
	    return (inBuf[p++]&0xff) + ((inBuf[p++]&0xff)<<8) + ((inBuf[p++]&0xff)<<16) + ((inBuf[p++]&0xff)<<24);
	}

	private short read16(){
		return (short) ((inBuf[p++]&0xff) + ((inBuf[p++])<<8));
	}
	
	private int read8(){
	    return inBuf[p++]&0xff;
	}

	
	private void evaluateCommand(byte cmd_, int dataSize){    
	    int i;
	    int icmd = (int)(cmd_ & 0xFF);
	    switch(icmd) {
	        case OSDCommon.MSP_IDENT:
	            version = read8();
	            multiType = read8();
	            read8(); // MSP version
	            read32();// capability
	            break;
	        case OSDCommon.MSP_STATUS:
	            cycleTime = read16();
	            i2cError  = read16();
	            present   = read16();
	            mode      = (int)read32();
	            break;
	        case OSDCommon.MSP_RAW_IMU:
	            accX = read16();
	            accY = read16();
	            accZ = read16();
	            gyroX = read16() / 8;
	            gyroY = read16() / 8;
	            gyroZ = read16() / 8;
	            magX = read16() / 3;
	            magY = read16() / 3;
	            magZ = read16() / 3;             
	            break;
	        case OSDCommon.MSP_SERVO:
	            for(i=0;i<8;i++) 
	                servo[i] = read16(); 
	            break;
	        case OSDCommon.MSP_MOTOR:
	            for(i=0;i<8;i++) 
	                mot[i] = read16(); 
	            break;
	        case OSDCommon.MSP_RC:
	            rcRoll     = read16();
	            rcPitch    = read16();
	            rcYaw      = read16();
	            rcThrottle = read16();    
	            rcAux1 = read16();
	            rcAux2 = read16();
	            rcAux3 = read16();
	            rcAux4 = read16();
	            break;
	        case OSDCommon.MSP_RAW_GPS:
	            gpsFix = read8();
	            gpsSatCount = read8();
	            gpsLatitude = (int)read32();
	            gpsLongitude = (int)read32();
	            gpsAltitude = read16();
	            gpsSpeed = read16(); 
	            break;
	        case OSDCommon.MSP_COMP_GPS:
	            gpsDistanceToHome = read16();
	            gpsDirectionToHome = read16();
	            gpsUpdate = read8(); 
	            break;
	        case OSDCommon.MSP_ATTITUDE:
	            angleX = read16()/10;  //[-180,180]，往右roll时，为正数
	            angleY = read16()/10;  //[-180,180]，头往上仰时，为负
	            head = read16(); 
	            break;
	        case OSDCommon.MSP_ALTITUDE:
	            altitude = (float) read32(); //[self int32ToFloat:read32()];
	            break;
	        case OSDCommon.MSP_BAT:
	            vBat = read8() / 256.0f * 5;
	            pMeterSum = read16(); 
	            break;
	        case OSDCommon.MSP_RC_TUNING:
	            break;
	        case OSDCommon.MSP_ACC_CALIBRATION:
	            break;
	        case OSDCommon.MSP_MAG_CALIBRATION:
	            break;
	        case OSDCommon.MSP_PID:
	            break;
	        case OSDCommon.MSP_BOX:
	            break;
	        case OSDCommon.MSP_BOXNAMES:
	            break;
	        case OSDCommon.MSP_PIDNAMES:
	            break;
	        case OSDCommon.MSP_MISC:
	            break;
	        case OSDCommon.MSP_MOTOR_PINS:
	            break;
	        case OSDCommon.MSP_SET_RAW_RC_TINY:
	            break;
	        case OSDCommon.MSP_DEBUG:
	            debug1 = read16();
	            debug2 = read16();
	            debug3 = read16();
	            debug4 = read16();
	            break;
	        case OSDCommon.MSP_HEX_NANO:
	            flightState = read8();
	            read16();  //throttle
	            altitude = read16();
	            angleX = read16()/10;  //[-180,180]，往右roll时，为正数
	            angleY = read16()/10;  //[-180,180]，头往上仰时，为负
	            head = read16();
	            vBat = read8() / 256.0f * 5;
	            read8(); //pitch trim
	            read8(); //roll trim
	            Log.d(TAG, "one frame" + angleX);
	            if(delegate != null) {
	            	delegate.osdDataDidUpdateOneFrame();
	            }
	            break;
	        default:
	            break;
	           
	    }
	}
}
