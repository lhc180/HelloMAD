package edu.neu.madcourse.binbo.rocketrush.gameobjects;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import edu.neu.madcourse.binbo.R;

public class MenuBackgroundFar extends Background {

	public MenuBackgroundFar(Resources res) {
		super(res);
		setZOrder(ZOrders.BACKGROUND_FAR);		
		
		BitmapFactory.Options options = new BitmapFactory.Options(); 
        options.inPurgeable = true;
        options.inPreferredConfig = Config.RGB_565;        
		setImage(BitmapFactory.decodeResource(res, R.drawable.bg_menu, options));
	}
	
	@Override
	public void onSizeChanged(int width, int height) {
		if (mImage == null) return;
		// scale the background according to the surface size
		float radio = mHeight / (float) mWidth;	
		int scaledWidth  = width;
		int scaledHeight = (int)(width * radio);
		
		if (scaledWidth == mImage.getWidth() && scaledHeight == mImage.getHeight()) {
			return;
		}
		
		Bitmap newImage = 
			Bitmap.createScaledBitmap(mImage, scaledWidth, scaledHeight, true);	
		mImage.recycle(); // explicit call to avoid out of memory
		mImage = null;
		System.gc();
		
		setImage(newImage);					
	}
	
}
