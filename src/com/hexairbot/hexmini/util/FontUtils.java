package com.hexairbot.hexmini.util;

import android.content.Context;
import android.graphics.Typeface;


public class FontUtils 
{	
	public static final class TYPEFACE 
	{	
		private static Typeface helvetica; 
	    public static final Typeface Helvetica(Context ctx) {
	    	
	    	if (helvetica == null) {
	    		helvetica = Typeface.createFromAsset(ctx.getAssets(), "fonts/helveticaneue-condensedbold.otf");
	    	}
	    	
	        return helvetica;
	    }
	} 
}
