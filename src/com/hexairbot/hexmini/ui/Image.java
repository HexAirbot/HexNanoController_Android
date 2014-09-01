package com.hexairbot.hexmini.ui;

import javax.microedition.khronos.opengles.GL10;

import com.hexairbot.hexmini.ui.gl.GLSprite;
import com.hexairbot.hexmini.util.TextureUtils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;


public class Image extends Sprite 
{
	public enum SizeParams {
		NONE,
		FILL_SCREEN,
		REPEATED
	}
	
	private GLSprite sprite;
	private SizeParams widthParam;
	private SizeParams heightParam;
	
	private boolean isInitialized;
	
	private boolean updateTexture;
	
   
    private Resources res;  
    private int resId;
	
	public Image(Resources resources, int resId, Align align)
	{
		super(align);
		
		widthParam = SizeParams.NONE;
		heightParam = SizeParams.NONE;
		
		isInitialized = false;
		sprite = new GLSprite(resources, resId);	
		this.res = resources;
		this.resId = resId;
	}
	
	
	@Override
	public void init(GL10 gl, int program) 
	{
		sprite.init(gl, program);
		isInitialized = true;
	}

	
	@Override
	public void surfaceChanged(GL10 gl, int width, int height) 
	{
		sprite.onSurfaceChanged(gl, width, height);
		
		if (widthParam == SizeParams.FILL_SCREEN) {
			sprite.setSize(width, sprite.height);
		}
		if(heightParam == SizeParams.FILL_SCREEN){
			sprite.setSize(sprite.width, height);
		}
		
		super.surfaceChanged(gl, width, height);
	}

	
	@Override
	public void surfaceChanged(Canvas canvas)
	{
		super.surfaceChanged(canvas);
	}
	
	private void invalidate()
	{
		updateTexture = true;
	}

	private Bitmap createBitmapToRender(boolean xRepeated, boolean yRepeated) {
		return TextureUtils.makeTexture(res, BitmapFactory.decodeResource(res, resId), surfaceWidth, surfaceHeight, true, true);
	}
	
	@Override
	public void draw(GL10 gl) 
	{
		if (visible) {
			if (updateTexture) {
				boolean xRepated = (widthParam == SizeParams.REPEATED) ? true : false;
				boolean yRepated = (heightParam == SizeParams.REPEATED) ? true : false;			
				
				Bitmap bitmap = createBitmapToRender(xRepated ,yRepated);
				sprite.updateTexture(res, bitmap);
				layout(surfaceWidth, surfaceHeight);
				
				updateTexture = false;
			}
			
			sprite.onDraw(gl, bounds.left, surfaceHeight - bounds.top - sprite.height);
		}
	}

	
	@Override
	public void draw(Canvas canvas)
	{
		if (visible) {
			sprite.onDraw(canvas, bounds.left, bounds.top);
		}
	}
	

	@Override
	public boolean onTouchEvent(View v, MotionEvent event) 
	{
		return false;
	}

	
	@Override
    protected void onAlphaChanged(float newAlpha)
    {
	    sprite.setAlpha(newAlpha);
    }


	@Override
	public boolean isInitialized() 
	{
		return isInitialized;
	}

	
	@Override
	public void setViewAndProjectionMatrices(float[] vMatrix, float[] projMatrix) 
	{
		sprite.setViewAndProjectionMatrices(vMatrix, projMatrix);
	}

	
	public void setBounds(Rect rect)
	{
		this.bounds = rect;
	}

	
	public void setPosition(int x, int y) 
	{
		bounds.offsetTo(x, y);
	}

	
	@Override
	public int getWidth() 
	{
		return sprite.width;
	}

	@Override
	public int getHeight() 
	{
		return sprite.height;
	}
	

	public void setSizeParams(SizeParams width, SizeParams height) 
	{
		widthParam = width;
		heightParam = height;
		
		if (width == SizeParams.REPEATED || height == SizeParams.REPEATED) {
			invalidate();
		}
	}

	
    @Override
    public void freeResources()
    {
        sprite.freeResources();
    }


	@Override
	public void setNeedsUpdate() {
		isInitialized = false;
	}
}
