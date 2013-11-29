/**
 * 
 */
package com.hexairbot.hexmini.modal;

/**
 * @author koupoo
 *
 */
public class OSDCommon {
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
	
	public static String getDefaultOSDDataRequest() {
		return null;
	}
	
	public static byte[] getSimpleCommand(MSPCommnand command){
		byte dataPackage[] = new byte[6];
		
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
		
		return dataPackage;
	}
}
