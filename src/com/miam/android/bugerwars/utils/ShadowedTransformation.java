package com.miam.android.bugerwars.utils;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * Shadow transformation which apply a gradient effect to an image.
 * @author Julien Salvi
 */
public class ShadowedTransformation implements Transformation {
	
	@Override 
	public Bitmap transform(Bitmap source) {
	    Bitmap result = Utils.getShadowedBitmap(source);
	    if (result != source) {
	    	source.recycle();
	    }
	    return result;
	}
	
	@Override 
	public String key() { return "shadowed()"; }

}
