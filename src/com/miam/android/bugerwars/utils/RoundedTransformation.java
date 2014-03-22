package com.miam.android.bugerwars.utils;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * Transformation that rounds a bitmap.
 * @author Julien Salvi
 */
public class RoundedTransformation implements Transformation {

	@Override 
	public Bitmap transform(Bitmap source) {
	    Bitmap result = Utils.getRoundedBitmap(source);
	    if (result != source) {
	    	source.recycle();
	    }
	    return result;
	}
	
	@Override 
	public String key() { return "rounded()"; }
	
}
