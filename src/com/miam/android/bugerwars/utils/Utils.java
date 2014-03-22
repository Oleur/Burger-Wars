package com.miam.android.bugerwars.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;

/**
 * This class contains global methods for the whole application.
 * @author Julien Salvi
 */
public class Utils {
	
	public final static String URL_INTERGALACTIC_BURGER = "http://coffeeport.herokuapp.com/burgers/";
	public final static String PREFS_NAME = "MyPrefsFile";
	
	private static char[] c = new char[]{'k', 'm', 'b', 't'};

    // Prevents instantiation.
    private Utils() {}

    /**
     * Uses static final constants to detect if the device's platform version is Gingerbread or
     * later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb or
     * later.
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb MR1 or
     * later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }
    
    /**
     * Uses static final constants to detect if the device's platform version is KitKat.
     */
    public static boolean hasKitKat() {
    	return Build.VERSION.SDK_INT >= 19;
    }
    
    /**
	 * Check if the device is connected to WiFi or Data plan.
	 * @return true if connected or connecting, false otherwise.
	 */
	public static boolean isOnline(Context c) {
	    ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	/**
	 * 
	 * @param context
	 * @param dp
	 * @return
	 */
	public static int dpToPx(Context context, int dp) {
	    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	/**
	 * 
	 * @param context
	 * @param px
	 * @return
	 */
	public static int pxToDp(Context context, int px) {
		float scale = context.getResources().getDisplayMetrics().density;
		int dpAsPixels = (int) (px*scale + 0.5f);
		return dpAsPixels;
	}
	
	/**
	 * 
	 * @param context
	 */
	public static void trimCache(Context context) {
		try {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory()) {
				deleteDir(dir);
			}
		} catch (Exception e) { }
	}

	/**
	 * 
	 * @param dir
	 * @return
	 */
    public static boolean deleteDir(File dir) {
	   	if (dir != null && dir.isDirectory()) {
		   	String[] children = dir.list();
		   	for (int i = 0; i < children.length; i++) {
			   	boolean success = deleteDir(new File(dir, children[i]));
			   	if (!success) {
				   	return false;
			   	}
		   	}
	   	}
	   	// The directory is now empty so delete it
	   	return dir.delete();
   	}
    
	/**
	 * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
	 * @param n the number to format
	 * @param iteration in fact this is the class from the array c
	 * @return a String representing the number n formatted in a cool looking way.
	 */
	public static String trunkatedIntegerFormat(double n, int iteration) {
	    double d = ((long) n / 100) / 10.0;
	    boolean isRound = (d * 10) %10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
	    return (d < 1000? //this determines the class, i.e. 'k', 'm' etc
	        ((d > 99.9 || isRound || (!isRound && d > 9.99)? //this decides whether to trim the decimals
	         (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
	         ) + "" + c[iteration]) 
	        : trunkatedIntegerFormat(d, iteration+1));
	}
	
	/**
	 * Convert a bitmap to a byte array then encode it to a string.
	 * @param bitmap Bitmap to be encoded.
	 * @return 
	 */
	public static String bitmapToEncodedString(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    	byte[] byteArray = stream.toByteArray();
    	return Base64.encodeToString(byteArray, Base64.DEFAULT);
	}
	
	/**
	 * 
	 * @param bmpString
	 * @return
	 */
	public static Bitmap decodeStringToBitmap(String bmpString) {
	    byte[] decodedByte = Base64.decode(bmpString, 0);
	    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length); 
	}
	
	/**
	 * Apply a gradient effect to the picture in order to get a shadow from the top to the middle of the image.
	 * @param bitmap The bitmap to transform
	 * @return The transformed bitmap
	 */
	public static Bitmap getShadowedBitmap(Bitmap bitmap) {
		if (bitmap == null) return null;
		//Create a new bitmap with the original dimension.
	    Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
	    
	    Canvas c = new Canvas(b);
	    c.drawBitmap(bitmap, 0, 0, null);
	    //Linear gradient for the fade effect.
	    LinearGradient grad = new LinearGradient(bitmap.getWidth()/2, 0, bitmap.getWidth()/2, bitmap.getHeight()/2, 
	    		Color.BLACK, 0x00000000, TileMode.CLAMP);
	    
	    //Setup the paint component.
	    Paint p = new Paint();
	    p.setAntiAlias(true);
	    p.setStyle(Paint.Style.FILL);
	    p.setShader(grad);
	    
	    c.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), p);
	    return b;
	}
	
	/**
	 * Round a bitmap.
	 * @param bitmap The bitmap to be rounded.
	 * @return A rounded bitmap
	 */
	public static Bitmap getRoundedBitmap(Bitmap bitmap) {
		if (bitmap == null) return null;
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	            bitmap.getHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(output);

	    final int color = 0xff424242;
	    final Paint paint = new Paint();
	    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

	    paint.setAntiAlias(true);
	    canvas.drawARGB(0, 0, 0, 0);
	    paint.setColor(color);
	    
	    canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
	            bitmap.getWidth() / 2, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	    canvas.drawBitmap(bitmap, rect, rect, paint);
	    
	    return output;
	}
}
