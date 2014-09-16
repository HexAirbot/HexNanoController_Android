/**
 * 
 */
package com.hexairbot.hexmini.modal;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;

/**
 * @author koupoo
 *
 */
public class OSDCommon {
	private static final Byte[] MSP_HEADER = {'$', 'M', '<'};
	
	public static final int MSP_IDENT = 100;
	public static final int MSP_STATUS = 101;
	public static final int MSP_RAW_IMU = 102;
	public static final int MSP_SERVO = 103;
	public static final int MSP_MOTOR = 104;
	public static final int MSP_RC = 105;
	public static final int MSP_RAW_GPS = 106;
	public static final int MSP_COMP_GPS = 107;
	public static final int MSP_ATTITUDE = 108;
	public static final int MSP_ALTITUDE = 109;
	public static final int MSP_BAT = 110;
	public static final int MSP_RC_TUNING = 111;
	public static final int MSP_PID = 112;
	public static final int MSP_BOX = 113;
	public static final int MSP_MISC = 114;
	public static final int MSP_MOTOR_PINS = 115;
	public static final int MSP_BOXNAMES = 116;
	public static final int MSP_PIDNAMES = 117;
	public static final int MSP_SET_RAW_RC_TINY = 150;
	public static final int MSP_ARM = 151;
	public static final int MSP_DISARM = 152;
	public static final int MSP_TRIM_UP = 153;
	public static final int MSP_TRIM_DOWN = 154;
	public static final int MSP_TRIM_LEFT = 155;
	public static final int MSP_TRIM_RIGHT = 156;
	
	public static final int MSP_HEX_NANO = 199;
	
	public static final int MSP_SET_RAW_RC = 200;
	public static final int MSP_SET_RAW_GPS = 201;
	public static final int MSP_SET_PID = 202;
	public static final int MSP_SET_BOX = 203;
	public static final int MSP_SET_RC_TUNING = 204;
	public static final int MSP_ACC_CALIBRATION = 205;
	public static final int MSP_MAG_CALIBRATION = 206;
	public static final int MSP_SET_MISC = 207;
	public static final int MSP_RESET_CONF = 208;
	public static final int MSP_EEPROM_WRITE = 250;
	public static final int MSP_DEBUG = 254;     
	
	private static int mainInfoRequest[] = {MSP_ATTITUDE, MSP_ALTITUDE, MSP_BAT};
	
	public enum MSPCommnand{
		 MSP_IDENT(100),
		 MSP_STATUS(101),               
		 MSP_RAW_IMU(102),               
		 MSP_SERVO(103),                 
		 MSP_MOTOR(104),                 
		 MSP_RC(105),                    
		 MSP_RAW_GPS(106),               
		 MSP_COMP_GPS(107),              
		 MSP_ATTITUDE(108),              
		 MSP_ALTITUDE(109),              
		 MSP_BAT(110),                   
		 MSP_RC_TUNING(111),             
		 MSP_PID(112),                   
		 MSP_BOX(113),                   
		 MSP_MISC(114),                  
		 MSP_MOTOR_PINS(115),            
		 MSP_BOXNAMES(116),              
		 MSP_PIDNAMES(117),              
		 MSP_SET_RAW_RC_TINY(150),       
		 MSP_ARM(151),                   
		 MSP_DISARM(152),                
		 MSP_TRIM_UP(153),               
		 MSP_TRIM_DOWN(154),             
		 MSP_TRIM_LEFT(155),             
		 MSP_TRIM_RIGHT(156),            
		 MSP_HEX_NANO(199),
		 MSP_SET_RAW_RC(200),            
		 MSP_SET_RAW_GPS(201),           
		 MSP_SET_PID(202),               
		 MSP_SET_BOX(203),               
		 MSP_SET_RC_TUNING(204),         
		 MSP_ACC_CALIBRATION(205),       
		 MSP_MAG_CALIBRATION(206),       
		 MSP_SET_MISC(207),              
		 MSP_RESET_CONF(208),            
		 MSP_EEPROM_WRITE(250),          
		 MSP_DEBUG(254);                 

		private int commandName;
		
		private MSPCommnand(int commandName){
			this.commandName = commandName;
		}
		
		public int value(){
			return commandName;
		}
	}
	
	public static byte[] getDefaultOSDDataRequest() {
		ArrayList<Byte> requestList = requestMSPList(mainInfoRequest);
	    
	    int requestDataSize = requestList.size();
	    
	    byte[] request = new byte[requestDataSize];
	    
	    for(int idx = 0; idx < requestDataSize; idx++){
	    	request[idx] = requestList.get(idx);
	    }
		
		return request;
	}
	
	public static byte[] getSimpleCommand(MSPCommnand command){
		byte dataPackage[] = new byte[18];
		
		dataPackage[0] = '$';
		dataPackage[1] = 'M';
		dataPackage[2] = '<';
		dataPackage[3] = 0;
		dataPackage[4] = (byte) command.value();
		
	    byte checkSum = 0;
	    
	    int dataSizeIdx = 3;
	    int checkSumIdx = 5;
	    
	    checkSum ^= (dataPackage[dataSizeIdx] & 0xFF);
	    checkSum ^= (dataPackage[dataSizeIdx + 1] & 0xFF);
	    
	    dataPackage[checkSumIdx] = checkSum;
	    
	    for(int idx = 6; idx < 18; idx++){
	    	dataPackage[idx] = '\0';
	    }
		
		return dataPackage;
	}
	
	/*创建出特定的带参数的请求
	 *@param msp 请求的命令
	 *@param payload 请求命令的参数
	 *@returns 返回创建的请求
	 */
	private static ArrayList<Byte> requestMSPWithPayload (int msp, byte[] payload) {
	    //List<byte> bf = new List<byte>;
	    
		ArrayList<Byte> bf = new ArrayList();
	    
	    if(msp < 0) {
	        return null;
	    }
	    
	    for (int i = 0; i < MSP_HEADER.length; i++) {
	    	bf.add(MSP_HEADER[i]);
		}
	    
	    byte checksum=0;
	    
	    int payloadLength = payload.length;
	    
	    byte pl_size = payloadLength != 0 ? (byte)payloadLength : 0;
	    
	    bf.add(pl_size);
	    
	    checksum ^= (pl_size&0xFF);
	    
	    bf.add((byte)(msp & 0xFF));
	    
	    checksum ^= (msp&0xFF);
	    
	    if (payloadLength != 0) {        
	        byte b;
	        for(int byteIdx = 0; byteIdx < payloadLength; byteIdx++){
	            b = payload[byteIdx];            
	            bf.add((byte)(b&0xFF));
	            checksum ^= (b&0xFF);
	        }
	        
	    }
	    bf.add(checksum);
	    
	    return bf;
	}
	
	/*创建出特定的待参数的请求
	 *@param msps 请求的命令列表
	 *@returns 返回创建的请求
	 */
	private static ArrayList<Byte> requestMSPList (int[] msps) {
	    ArrayList<Byte> requestList = new ArrayList<Byte>();
	    byte emptyPayload[] = new byte[0];
	    
	    for(int mspIdx = 0; mspIdx < msps.length; mspIdx++){
	        requestList.addAll(requestMSPWithPayload(msps[mspIdx], emptyPayload));//

	    }
	    return requestList;
	}

	/*创建出特定命令的请求
	 *@param msp 请求的命令
	 *@returns 返回创建的请求
	 */
	private static ArrayList<Byte> requestMSP(int msp) {
	    byte emptyPayload[] = new byte[0];
	    
	    return requestMSPWithPayload(msp, emptyPayload);
	}
}