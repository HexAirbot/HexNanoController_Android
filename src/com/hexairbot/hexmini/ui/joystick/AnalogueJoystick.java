/*
 * AnalogueJoystick
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */

package com.hexairbot.hexmini.ui.joystick;
import com.hexairbot.hexmini.R;
import android.content.Context;


public class AnalogueJoystick 
	extends JoystickBase
{

	public AnalogueJoystick(Context context, Align align, boolean isRollPitchJoystick, boolean yStickIsBounced) 
	{
		super(context, align, isRollPitchJoystick, yStickIsBounced);
	}
 
	@Override
	protected int getBackgroundDrawableId() 
	{
		return R.drawable.joystick_bg_new;
	}

	
	@Override
	protected int getTumbDrawableId() 
	{
		return R.drawable.joystick_rudder_throttle_new;
	}
}
