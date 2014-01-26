/*
 * TextureUtils
 *
 *  Created on: May 24, 2011
 *      Author: Dmytro Baryskyy
 */


package com.hexairbot.hexmini.util;

import android.R.bool;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class TextureUtils 
{
	/**
	 * Finds greater nearest number that is power of 2
	 * @return long
	 */
	public static long roundPower2(final long x)
	{
		int rval=256;

		while(rval < x)
			rval <<= 1;

		return rval;
	}
	
	
	/**
	 * Makes a texture from any bitmap. 
	 * (Texture should have size that is power of 2) 
	 * @param bmp bitmap
	 * @return BitmapDrawable that has size that is power of 2. Bitmap is not stretched, free space 
	 * is filled with default color.
	 */
	public static Bitmap makeTexture(Resources res, Bitmap bmp) 
	{
		if (bmp == null) {
			throw new IllegalArgumentException("Bitmap can't be null");
		}
		
		int height = (int) roundPower2(bmp.getHeight());
		int width = (int) roundPower2(bmp.getWidth());
		
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(bmp, 0, 0, null);
		
		return result;
	}
	
	public static Bitmap makeTexture(Resources res, Bitmap srcBitmap, int destWidth, int destHeight, boolean xRepeated, boolean yRepeated) 
	{
		int xCnt = (destWidth + srcBitmap.getWidth() - 1) / srcBitmap.getWidth();
		int yCnt = (destHeight + srcBitmap.getHeight() - 1) / srcBitmap.getHeight();
		

		int width = destWidth;
		int height = destHeight;
		
		if (!xRepeated) {
			width = srcBitmap.getWidth();
		}

		if (!yRepeated) {
			height = srcBitmap.getHeight();
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bitmap);
		
		if (xRepeated && yRepeated) {
			for(int xIdx = 0; xIdx < xCnt; ++xIdx){
				for(int yIdx = 0; yIdx < yCnt; ++yIdx){
					canvas.drawBitmap(srcBitmap, xIdx * srcBitmap.getWidth(), yIdx * srcBitmap.getHeight(), null);
				}
			}
		}
		else if(xRepeated && !yRepeated){
			for(int xIdx = 0; xIdx < xCnt; ++xIdx){
				canvas.drawBitmap(srcBitmap, xIdx * srcBitmap.getWidth(), 0, null);
			}
		}
		else if(!xRepeated && yRepeated){
			for(int yIdx = 0; yIdx < yCnt; ++yIdx){
				canvas.drawBitmap(srcBitmap, 0, yIdx * srcBitmap.getHeight(), null);
			}
		}
		else{
			canvas.drawBitmap(srcBitmap, 0, 0, null);
		}

		return bitmap;
	}
}
