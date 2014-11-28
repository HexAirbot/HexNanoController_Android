package com.hexairbot.hexmini.modal;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSArray;
import com.dd.plist.NSNumber;
import com.dd.plist.NSString;

public class Channel {
	public final static String CHANNEL_NAME_AILERON  = "Aileron";
	public final static String CHANNEL_NAME_ELEVATOR = "Elevator";
	public final static String CHANNEL_NAME_RUDDER   = "Rudder";
	public final static String CHANNEL_NAME_THROTTLE = "Throttle";
	public final static String CHANNEL_NAME_AUX1     = "AUX1";
	public final static String CHANNEL_NAME_AUX2     = "AUX2";
	public final static String CHANNEL_NAME_AUX3     = "AUX3";
	public final static String CHANNEL_NAME_AUX4     = "AUX4";
	
	public final static String NAME                    = "Name";
	public final static String IS_REVERSED             = "IsReversed";
	public final static String TRIM_VALUE              = "TrimValue";
	public final static String OUTPUT_ADJUSTABLE_RANGE = "OutputAdjustableRange";
	public final static String DEFAULT_OUTPUT_VALUE    = "DefaultOutputValue";

	private NSDictionary data;
	private int idx;
	private String name;
	private float value;
	private float defaultOutputValue;
	private float outputAdjustabledRange;
	private float trimValue;
	private boolean isReversed;
	private ApplicationSettings ownerSettings;
	
	public Channel(ApplicationSettings settings, int idx){
		ownerSettings = settings;
		this.idx = idx;
		NSArray channels = (NSArray)settings.getData().objectForKey(ApplicationSettings.CHANNELS);
		data = (NSDictionary)channels.objectAtIndex(idx);
		
		name       = ((NSString)data.objectForKey(NAME)).getContent();
		isReversed = ((NSNumber)data.objectForKey(IS_REVERSED)).boolValue();
		trimValue  = ((NSNumber)data.objectForKey(TRIM_VALUE)).floatValue();
		outputAdjustabledRange = ((NSNumber)data.objectForKey(OUTPUT_ADJUSTABLE_RANGE)).floatValue();
		defaultOutputValue =  ((NSNumber)data.objectForKey(DEFAULT_OUTPUT_VALUE)).floatValue();
	}
	
	float clip(float value, float min, float max) {
		if(value>max)
			return max;
		if(value<min)
			return min;
		return value;
	}
	
	public int getIdx() {
		return idx;
	}
	
	public void setIdx(int idx){
		this.idx = idx;
	}

	public String getName() {
		return name;
	}
	

	public float getValue() {
		return value;
	}

	public void setValue(float value) {		
		this.value = clip(value, -1.0f, 1.0f);
		float outputValue = clip(value + trimValue, -1.0f, 1.0f); 
		if (isReversed) {
			outputValue = -outputValue;
		}
	    
		outputValue *= outputAdjustabledRange;
	    
		Transmitter.sharedTransmitter().setChannel(idx, outputValue);
		
	}

	public float getDefaultOutputValue() {
		return defaultOutputValue;
	}

	public void setDefaultOutputValue(float defaultOutputValue) {
		this.defaultOutputValue = defaultOutputValue;
		data.put(DEFAULT_OUTPUT_VALUE, defaultOutputValue);
	}

	public float getOutputAdjustabledRange() {
		return outputAdjustabledRange;
	}

	public void setOutputAdjustabledRange(float outputAdjustabledRange) {
		this.outputAdjustabledRange = outputAdjustabledRange;
		data.put(OUTPUT_ADJUSTABLE_RANGE, outputAdjustabledRange);
	}

	public float getTrimValue() {
		return trimValue;
	}

	public void setTrimValue(float trimValue) {
		this.trimValue = trimValue;
		data.put(TRIM_VALUE, trimValue);
	}

	public boolean isReversed() {
		return isReversed;
	}

	public void setReversed(boolean isReversed) {
		this.isReversed = isReversed;
		data.put(IS_REVERSED, isReversed);
	}

	public ApplicationSettings getOwnerSettings() {
		return ownerSettings;
	}

	public void setOwnerSettings(ApplicationSettings ownerSettings) {
		this.ownerSettings = ownerSettings;
	}
}
