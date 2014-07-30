package com.hexairbot.hexmini.ui;

import javax.microedition.khronos.opengles.GL10;

import com.hexairbot.hexmini.ui.gl.GLSprite;


import android.content.res.Resources;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class AnimationIndicator extends Sprite 
{
	private GLSprite[] indicatorStates;
	private int value;
	private boolean initialized;
	
	private boolean isStarted;
	private float duration;
	private long lastUpdateTime;
	private int imageIdx;
	
	public AnimationIndicator(Resources resources, int[] drawableIds, Align alignment) 
	{
		super(alignment);
	
		indicatorStates = new GLSprite[drawableIds.length];
		
		for (int i=0; i<drawableIds.length; ++i) {
			GLSprite sprite = new GLSprite(resources, drawableIds[i]);
			indicatorStates[i] = sprite;
		}
		
		isStarted = false;
	}

	public void start(float duration){
		isStarted = true;
		this.duration = duration;
	}
	
	public void stop(){
		isStarted = false;
	}
	
	@Override
	public void init(GL10 gl, int program) {
	    
		for (int i=0; i<indicatorStates.length; ++i) {
			GLSprite sprite = indicatorStates[i];
			sprite.init(gl, program);
		}
		
		initialized = true;
	}

	
	@Override
	public void draw(GL10 gl) 
	{
		if(visible){
		
		GLSprite sprite = null;
		
		if(isStarted){
			if(indicatorStates.length > 1){
				long currentTime = System.nanoTime();
				
				if((currentTime - lastUpdateTime) > (duration * 1000000000)){
					sprite = indicatorStates[(++imageIdx) % indicatorStates.length];
					lastUpdateTime = currentTime;
				}
				else{
					sprite = indicatorStates[imageIdx % indicatorStates.length];
				}
			}
			else{
				sprite = indicatorStates[0];
			}
		}
		else{
			sprite = indicatorStates[0];
		}
		
		sprite.onDraw(gl,  bounds.left, surfaceHeight - bounds.top - sprite.height);
		}
	}

	
	@Override
	public void draw(Canvas canvas) 
	{
		GLSprite sprite = indicatorStates[value];
		sprite.onDraw(canvas,  bounds.left, surfaceHeight - bounds.top - sprite.height);
	}

	
	@Override
	public boolean onTouchEvent(View v, MotionEvent event) 
	{
		return false;
	}

	
	@Override
	public boolean isInitialized() 
	{
		return initialized;
	}

	
	@Override
	public void setViewAndProjectionMatrices(float[] vMatrix, float[] projMatrix) 
	{
		for (int i=0; i<indicatorStates.length; ++i) {
			indicatorStates[i].setViewAndProjectionMatrices(vMatrix, projMatrix);
		}
	}

	
	@Override
	public int getWidth() 
	{
		return indicatorStates[value].width;
	}

	
	@Override
	public int getHeight() 
	{	
		return indicatorStates[value].height;
	}
	
	
	public void setValue(int value)
	{
		if (value < 0 || value >= indicatorStates.length) {
			throw new IllegalArgumentException("Value " + value + " is out of bounds");
		}
		
		this.value = value;
	}

    
	@Override
    public void freeResources()
    {
        for (int i=0; i<indicatorStates.length; ++i) {
            indicatorStates[i].freeResources();
        }
    }

	@Override
	public void setNeedsUpdate() {
		initialized = false;
	}
}
