/*
 * JoystickBase
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */

package com.hexairbot.hexmini.ui.joystick;

import javax.microedition.khronos.opengles.GL10;

import com.hexairbot.hexmini.ui.Sprite;
import com.hexairbot.hexmini.ui.gl.GLSprite;

import com.hexairbot.hexmini.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public abstract class JoystickBase extends Sprite
{
	private static final String TAG = JoystickBase.class.getSimpleName();
	
    
    private boolean isPressed;
	
	private float xValue;
	private float yValue;
	
	private float baseX;
	private float baseY;
	
	protected float centerX;
	protected float centerY;
	
	private float thumbCenterX;
	private float thumbCenterY;
	
	private float opacity;
	
	protected RectF activationRect;
	
	private GLSprite bg;
	private GLSprite thumbNormal;
	private GLSprite thumbRollPitch;
	
	private JoystickListener analogueListener;

	private boolean inverseY;
	private int fingerId;

	private boolean isInitialized;
	
	private float xDeadBand;
	private float yDeadBand;
	private float operableRadiusRatio;
	
	private float xOperableRadiusWidth;
	private float yOperableRadiusWidth;
	
	private boolean isRollPitchJoystick;
	private boolean yStickIsBounced;
	
	
	public JoystickBase(final Context context, Align align, boolean isRollPitchJoystick, boolean yStickIsBounced)
	{
		super(align);
		
		this.yStickIsBounced = yStickIsBounced;
		
		opacity = 1.f;
		isPressed = false;
		isInitialized = false;
		this.isRollPitchJoystick = isRollPitchJoystick;
		
		centerX = 0;
		centerY = 0;
		
		xValue = 0;
		yValue = 0;

		int bgResId = getBackgroundDrawableId();
		int thumbResId = getTumbDrawableId();
		int thumbRollPitchId = getThumbRollPitchDrawableId();
		
		if (bgResId != 0) {
			bg = new GLSprite(context.getResources(), bgResId);
		}
		
		if (thumbResId != 0) {
			thumbNormal = new GLSprite(context.getResources(), thumbResId);
		}
		
		if (thumbRollPitchId != 0) {
		    thumbRollPitch = new GLSprite(context.getResources(), thumbRollPitchId);
		}
		
		alignment = align;
		fingerId = -1;
		
        //controlRatio = (float) (0.5 - (CONTROL_RATIO / 2.0));
        operableRadiusRatio = 0.82f;
		xOperableRadiusWidth = bg.width / 2.0f * operableRadiusRatio;
		yOperableRadiusWidth = bg.height / 2.0f * operableRadiusRatio;
	}
	

	public boolean getYStickIsBounced() {
		return yStickIsBounced;
	}
	
	
    public void surfaceChanged(GL10 gl, int width, int height)
	{	
    	Log.d(TAG, "surfaceChanged(GL10 gl, int width, int height)");
    	
		if (bg != null) {
			bg.onSurfaceChanged(gl, width, height);
		}
		
		if (thumbNormal != null) {
			thumbNormal.onSurfaceChanged(gl, width, height);
		}
		
		if (thumbRollPitch != null) {
		    thumbRollPitch.onSurfaceChanged(gl, width, height);
		}

		super.surfaceChanged(gl, width, height);
	
		updateActivatonRegion(width, height);
		
		isInitialized = true;
	}
	
	

	public void surfaceChanged(Canvas canvas) 
	{
		Log.d(TAG, "surfaceChanged(Canvas canvas)");
		
		super.surfaceChanged(canvas);
	
		updateActivatonRegion(canvas.getWidth(), canvas.getHeight());
		
		isInitialized = true;
	}


	private void updateActivatonRegion(int width, int height) 
	{
		switch (alignment) {
		case BOTTOM_LEFT:
			setActivationRect(new Rect(0,0, width / 2, height));
			break;
		case BOTTOM_RIGHT:
			setActivationRect(new Rect(width / 2, 0, width, height));
			break;
		default:
		    //not implemented
		}
	}
	

	public void draw(Canvas canvas)
	{
		updateControlOpacity();	
		
		if (bg != null)
			bg.onDraw(canvas, centerX-(bg.width >> 1) - margin.left, inverseY(centerY - (bg.height >> 1)));
		
		if (thumbNormal != null)
			thumbNormal.onDraw(canvas, thumbCenterX-(thumbNormal.width >> 1), inverseY(thumbCenterY - (thumbNormal.height >> 1)));
	}
	
	
	
    public void draw(GL10 gl)
    {
        updateControlOpacity();

        if (bg != null)
            bg.onDraw(gl, centerX - (bg.width >> 1), (centerY - (bg.height >> 1)));

        if (isRollPitchJoystick) {
            if (thumbRollPitch != null) {
                thumbRollPitch.onDraw(gl, thumbCenterX - (thumbNormal.width >> 1),
                        (thumbCenterY - (thumbNormal.height >> 1)));
            }
        } else {
            if (thumbNormal != null) {
                thumbNormal.onDraw(gl, thumbCenterX - (thumbNormal.width >> 1),
                        (thumbCenterY - (thumbNormal.height >> 1)));
            }
        }
    }

	
	public void setActivationRect(Rect rect)
	{
		this.activationRect = new RectF(rect);
	
		switch (alignment) {
		case BOTTOM_LEFT:
			baseX = activationRect.left + (bg.width / 2.0f) + margin.left;
			baseY = activationRect.bottom - (bg.height/2.0f) - margin.bottom;
			break;
		case BOTTOM_RIGHT:
			baseX = activationRect.right - (bg.width / 2.0f) - margin.right;
			baseY = activationRect.bottom - (bg.height/2.0f) - margin.bottom;
			break;
		default:
			// Not implemented yet    
		}
		
		Log.d("setActivationRect", "baseX:" + baseX + ";baseY:" + baseY);
		
		moveTo(baseX, inverseY(baseY));
		
		if(yStickIsBounced) {
			moveThumbTo(baseX, inverseY(baseY));
		}
		else{
			setYValue(this.yValue);
		}
		
        if (analogueListener != null) {
            analogueListener.onChanged(this, 0, 0);
            analogueListener.onReleased(this);
        }
        
		//moveToBase(activationRect);
	}


	protected void moveToBase(RectF rect) {
		moveTo(baseX, inverseY(baseY));
		moveThumbTo(baseX, inverseY(baseY));
		
        if (analogueListener != null) {
            analogueListener.onChanged(this, 0, 0);
            analogueListener.onReleased(this);
        }
	}
	
	public void moveTo(float x, float y)
	{
		this.centerX = x;
		this.centerY = y;
	}
	
	public void absolutMoveThumbTo(float x, float y){
		this.thumbCenterX = x;
		this.thumbCenterY = y;
	}
	
	public void moveThumbTo(float x, float y)
	{
		/*
		double dx = x - centerX;
		double dy = y - centerY;
		
		double distance = Math.sqrt(dx*dx + dy*dy);
		double angle = Math.atan2(dy, dx);
		
		float joy_radius = bg.width / 2.0f - thumbNormal.width * 0.33f / 2;
		
		if (distance  > joy_radius) {
			dx = Math.cos(angle) * joy_radius;
			dy = Math.sin(angle) * joy_radius;

			this.thumbCenterX = centerX + (float) dx;
			this.thumbCenterY = centerY + (float) dy;
		} else {
			this.thumbCenterX = x;
			this.thumbCenterY = y;
		}*/
		
		float nextX = x;
		float nextY = y;
		
		
		float dx = x - centerX;
		float dy = y - centerY;
		
		//float xOperableRadiusWidth = bg.width / 2.0f * operableRadiusRatio;
		//float yOperableRadiusWidth = bg.height / 2.0f * operableRadiusRatio;
		
		if (Math.abs(dx) > xOperableRadiusWidth) {
			Log.d("moveThumbTo", "Math.abs(dx) > operableRadiusWidth" + ";dx:" + dx);
			if (dx > 0) {
				nextX = centerX + xOperableRadiusWidth;
			}
			else{
				nextX = centerX - xOperableRadiusWidth;
			}
		}
		
		if (Math.abs(dy) > yOperableRadiusWidth) {
			Log.d("moveThumbTo", "Math.abs(dy) > operableRadiusWidthh" + ";dy:" + dy);
			if (dy > 0) {
				nextY = centerY + yOperableRadiusWidth;
			}
			else{
				nextY = centerY - yOperableRadiusWidth;
			}
		}
		
		
		this.thumbCenterX = nextX;
		this.thumbCenterY = nextY;
	}
	
	
	public void init(GL10 gl, int program) {
		bg.init(gl, program);
		thumbNormal.init(gl, program);
		thumbRollPitch.init(gl, program);
	}
	
	
	public boolean onTouchEvent(View v, MotionEvent event) 
	{
		if (activationRect == null)
			return false;
		
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		int pointerIdx = action >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

		switch (actionCode) {
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				
				if (fingerId == -1 && activationRect.contains(event.getX(pointerIdx), event.getY(pointerIdx))) {
					fingerId = event.getPointerId(pointerIdx);
					isPressed = true;
					onActionDown(event.getX(pointerIdx), event.getY(pointerIdx));
					return true;
				}

				return false;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
			{	
				if (fingerId == -1)
					return false;
				
				if (event.getPointerId(pointerIdx) != fingerId)
					return false;
						
				fingerId = -1;
				onActionUp(event.getX(pointerIdx),  event.getY(pointerIdx));
				isPressed = false;
				return true;
			}
			case MotionEvent.ACTION_MOVE: 
			{
				if (fingerId == -1)
					return false;
				
				for (int i=0; i<event.getPointerCount(); ++i)  {
					if (event.getPointerId(i) == fingerId) {
						onActionMove(event.getX(i), event.getY(i));		
						return true;
					}
				}

				return false;
			}
			default:
				return false;
		}
	}
	
	
	public void setOnAnalogueChangedListener(JoystickListener listener)
	{
		this.analogueListener = listener;
	}
	
	
	public void setInverseYWhenDraw(boolean inverse)
	{
		inverseY = inverse;
	}
	

    @Override
    protected void onAlphaChanged(float newAlpha)
    {
        opacity = newAlpha;
    }


    protected int getBackgroundDrawableId()
    {
        return R.drawable.joystick_bg_new;
    }



    protected int getTumbDrawableId()
    {
        return R.drawable.joystick_rudder_throttle_new;
    }

        
    private int getThumbRollPitchDrawableId()
    {
        return R.drawable.joystick_roll_pitch_new;
    }



    protected void onActionDown(float x, float y)
    {
    	Log.d("onActionDown x y", "x:"+ x + ";y:"+ y);
    	Log.d("onActionDown before", "centerX:"+ centerX + ";centerY:"+ centerY  + ";thumbCenterX:"+ thumbCenterX + ";thumbCenterY:"+ thumbCenterY);
    	
        isPressed = true;

        if (yStickIsBounced) {
        	moveTo(x, inverseY(y));
        	moveThumbTo(x, inverseY(y));
		}
        else {
        	moveTo(x,  inverseY(y - (inverseY(thumbCenterY) - baseY)));
        	moveThumbTo(x, inverseY(y));
		}
        

        if (analogueListener != null) {
        	if (yStickIsBounced) {
                analogueListener.onChanged(this, 0, 0);
                analogueListener.onPressed(this);
			}
        	else{
        		 float yValidBand = 1 - yDeadBand; 	
        	
                if ((centerY - inverseY(y)) > (bg.height / 2.0f * yDeadBand)) {
                	float percent = (centerY - inverseY(y) - (bg.height / 2.0f * yDeadBand)) / (yValidBand / 2.0f * bg.height);
                	
                	if(percent > 1.0f)
                		percent = 1;
                	
                	yValue = -percent;
        		}
                else if((inverseY(y) - centerY) > (bg.height / 2.0f * yDeadBand)) {
                	float percent = (inverseY(y) - centerY - (bg.height / 2.0f *  yDeadBand)) / (yValidBand / 2.0f * bg.height);
                	
                	if(percent > 1.0f)
                		percent = 1;
                	
                	yValue = percent;
        		}
                else {
        			yValue = 0;
                }
        		
                analogueListener.onChanged(this, 0, yValue);
                analogueListener.onPressed(this);
        	}
        }
        
        Log.d("onActionDown after", "centerX:"+ centerX + ";centerY:"+ centerY  + ";thumbCenterX:"+ thumbCenterX + ";thumbCenterY:"+ thumbCenterY);
    }


    protected void onActionMove(float x, float y)
    {       
        moveThumbTo(x, inverseY(y));
        
        //Log.d("onActionMove", "thumbCenterX:"+ thumbCenterX + ";thumbCenterY:"+ thumbCenterY);
        
        float xValidBand = 1 - xDeadBand;  // 0.5f - xDeadBand / 2.0f;
        
        if ((centerX - x) > (bg.width / 2.0f * xDeadBand)) {
        	float percent = (centerX - x - (bg.width / 2.0f * xDeadBand)) / (xValidBand / 2.0f * bg.width);
        	
        	if(percent > 1.0f)
        		percent = 1;
        	
        	xValue = -percent;
		}
        else if((x - centerX) > (bg.width / 2.0f * xDeadBand)) {
        	float percent = (x - centerX - (bg.width / 2.0f * xDeadBand)) / (xValidBand / 2.0f * bg.width);
        	
        	if(percent > 1.0f)
        		percent = 1;
        	
        	xValue = percent;
		}
        else {
			xValue = 0;
		}
        
        float yValidBand = 1 - yDeadBand; 
        
        if ((centerY - inverseY(y)) > (bg.height / 2.0f * yDeadBand)) {
        	float percent = (centerY - inverseY(y) - (bg.height / 2.0f * yDeadBand)) / (yValidBand / 2.0f * bg.height);
        	
        	if(percent > 1.0f)
        		percent = 1;
        	
        	yValue = -percent;
		}
        else if((inverseY(y) - centerY) > (bg.height / 2.0f * yDeadBand)) {
        	float percent = (inverseY(y) - centerY - (bg.height / 2.0f *  yDeadBand)) / (yValidBand / 2.0f * bg.height);
        	
        	if(percent > 1.0f)
        		percent = 1;
        	
        	yValue = percent;
		}
        else {
			yValue = 0;
		}
        
        if (analogueListener != null) {
        	analogueListener.onChanged(this, xValue, yValue);
        }
    }

    
    protected void onActionUp(float x, float y)
    {
        isPressed = false;
        
        if (yStickIsBounced) {
        	moveToBase(activationRect);
        	
        	//Log.d("a", "a");
		}
        else{
        	absolutMoveThumbTo(baseX,  inverseY(baseY - (thumbCenterY - centerY))); 
        	moveTo(baseX, inverseY(baseY));   
        	
            if (analogueListener != null) {
            	analogueListener.onReleased(this);
    		}
        }
        
        Log.d("onActionUp", "centerX:"+ centerX + ";centerY:"+ centerY  + ";thumbCenterX:"+ thumbCenterX + ";thumbCenterY:"+ thumbCenterY);
    }


	protected float inverseY(float y) 
	{
		if (inverseY) {
			return surfaceHeight - y;
		} else {
			return y;
		}
	}
	

	private void updateControlOpacity() 
	{
		if (isPressed) {
			if (bg != null)
				bg.alpha = 1.0f;
			
			if (bg != null)
				thumbNormal.alpha = 1.0f;
			
			if (thumbRollPitch != null) {
			    thumbRollPitch.alpha = 1.0f;
			}
		} else {
			
			if (bg != null)
				bg.alpha = opacity;
			
			if (thumbNormal != null)
				thumbNormal.alpha = opacity;
			
			if (thumbRollPitch != null) {
			    thumbRollPitch.alpha = opacity;
			}
		}
	}


	public void setAlign(Align alignment) 
	{
		this.alignment = alignment;
	}
	

	public boolean isInitialized() 
	{
		return isInitialized;
	}
	

	@Override
	public void setViewAndProjectionMatrices(float[] vMatrix, float[] projMatrix) {
		bg.setViewAndProjectionMatrices(vMatrix, projMatrix);
		thumbNormal.setViewAndProjectionMatrices(vMatrix, projMatrix);	
		thumbRollPitch.setViewAndProjectionMatrices(vMatrix, projMatrix);
	}


	@Override
	public int getWidth() 
	{
		return bg.width;
	}


	@Override
	public int getHeight() 
	{
		return bg.height;
	}
	
	
	public boolean isRollPitchJoystick()
	{
	    return isRollPitchJoystick;
	}

	
    @Override
    public void freeResources()
    {
       bg.freeResources();
       thumbNormal.freeResources();
       thumbRollPitch.freeResources();
    }
    

    /*
    private float getXValue(float centerX, float x, float radius)
    {
        return -1 * ((centerX - x) - (radius - (controlRatio * (radius * 2)))) / ((controlRatio * (radius * 2)));
    }


    private float getYValue(float centerY, float y, float radius)
    {
        return -1 * ((centerY - inverseY(y)) - (radius - (controlRatio * (radius * 2))))
                / ((controlRatio * (radius * 2)));
    }*/


    public void setIsRollPitchJoystick(boolean b)
    {
       this.isRollPitchJoystick = b;      
    }
    
	public float getXDeadBand() {
		return xDeadBand;
	}
	
	public void setXDeadBand(float xDeadBand) {
		this.xDeadBand = xDeadBand;
	}
	
	public float getYDeadBand() {
		return yDeadBand;
	}
	
	public void setYDeadBand(float yDeadBand) {
		this.yDeadBand = yDeadBand;
	}
	
	public float getOperableRadiusRatio() {
		return operableRadiusRatio;
	}
	
	public float getXValue(){
		return xValue;
	}
	
	public void setXValue(float xValue){
		this.xValue = xValue;
		//to do
	}
	
	public float getYValue(){
		return yValue;
	}
	
	public void setYValue(float yValue){
		this.yValue = yValue;
		
		float refinedValue = yValue;
		
		if (yValue > 1) {
			refinedValue = 1;
		}
		else if(yValue < -1){
			refinedValue = -1;
		}

		float yOffset = yOperableRadiusWidth * refinedValue;
		
		//Log.e(TAG, "y offset" + yOffset);
		
        //if (yStickIsBounced == false) {
        	//moveToBase(activationRect);
        	
        	//absolutMoveThumbTo(baseX,  inverseY(baseY - (thumbCenterY - centerY))); 
        	
		absolutMoveThumbTo(centerX,  centerY + yOffset); 
        	
        	//moveTo(baseX, inverseY(baseY));   
        	
//            if (analogueListener != null) {
//            	analogueListener.onReleased(this);
//    	//	}
		//}
	}

	@Override
	public void setNeedsUpdate() {
		isInitialized = false;
	}
}
