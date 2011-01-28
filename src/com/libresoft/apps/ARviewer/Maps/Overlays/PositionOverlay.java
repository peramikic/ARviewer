/*
 *
 *  Copyright (C) 2009 GSyC/LibreSoft
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/. 
 *
 *  Author : Roberto Calvo Palomino <rocapal@gsyc.es>
 *
 */

package com.libresoft.apps.ARviewer.Maps.Overlays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Shader.TileMode;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.libresoft.apps.ARviewer.R;

public class PositionOverlay extends Overlay
{
	private final int mRadius = 5;
	private int AVATAR_LENGTH;
	
	private Bitmap avatar = null;
	private GeoPoint geoPoint;
	
	private boolean mPerimeter = false;
	private Location mLocationPerimeter;
	
	public PositionOverlay(Context mContext){
		avatar = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.user_70), 
				48, 48, true);
		AVATAR_LENGTH = avatar.getWidth()/2;
	}
	
	public void setPoint(GeoPoint point){
		geoPoint = point;
	}
	
	public void withPerimeter(Location location)
	{
		mPerimeter = true;
		mLocationPerimeter = location;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
        if(geoPoint == null)
        	return;
        
		Point point= new Point();
		Projection projection = mapView.getProjection();
	        
		projection.toPixels(geoPoint, point);
	        
		RectF oval = new RectF (point.x - mRadius, point.y - mRadius,
								point.x + mRadius, point.y + mRadius);
	    
		Paint paint = new Paint();
		paint.setARGB(250,255,0,0);
		paint.setShader(new RadialGradient(point.x, point.y, mRadius, Color.RED, Color.BLACK, TileMode.MIRROR));
		paint.setAntiAlias(true);
		paint.setFakeBoldText(true);
		paint.setTextAlign(Align.CENTER);
		
		Paint backPaint = new Paint();
		backPaint.setARGB(175,0,0,0);
		backPaint.setShader(new LinearGradient(point.x, point.y - 3*mRadius, 
				point.x, point.y + mRadius, Color.rgb(150, 230, 150), Color.BLACK, TileMode.MIRROR));
		backPaint.setAntiAlias(true);
	        
		String text = "User";
		float tlongitude = paint.measureText(text);
		if(tlongitude > 100){
			tlongitude = 100;
			paint.setTextAlign(Align.LEFT);
		}
		
		RectF backRect = new RectF (point.x + 2 + mRadius, point.y - 3*mRadius,
									point.x + 2 + mRadius + tlongitude + 20, point.y + mRadius);
		Path path = new Path();
		path.moveTo(point.x + 2 + mRadius + 10, point.y - mRadius/2);
		path.lineTo(point.x + 2 + mRadius + tlongitude +10, point.y - mRadius/2);
	        
		canvas.drawOval(oval,paint); 
		
		if (mPerimeter)
		{
			
	        GeoPoint geoPointPerimeter = new GeoPoint(  (int) (mLocationPerimeter.getLatitude() * 1000000), 
					   						            (int) (mLocationPerimeter.getLongitude() * 1000000));
	        
			Point pointPerimeter = new Point();
			Projection projectionPerimeter = mapView.getProjection();
		        
			projectionPerimeter.toPixels(geoPointPerimeter, pointPerimeter);
			
			Paint borderPaint = new Paint();
		    borderPaint.setStyle(Paint.Style.STROKE);
		    borderPaint.setAntiAlias(true);
		    borderPaint.setStrokeWidth(3);
		    borderPaint.setARGB(250,255,0,0);
		    
			canvas.drawCircle(pointPerimeter.x,pointPerimeter.y,100,borderPaint);
			
		}
		
		
		
		canvas.drawRoundRect(backRect, 5, 5, backPaint);
		
		if(avatar != null){
			
			float x_c = point.x + 2 + mRadius + (tlongitude + 20)/2;
			float y_b = point.y - 3*mRadius;
			
			int offset = AVATAR_LENGTH + 10;
			RectF avatarRect = new RectF (x_c - offset, y_b - (offset * 2),
					x_c + offset, y_b);
			
			Paint avatarPaint = new Paint();
			avatarPaint.setARGB(175,0,0,0);
			avatarPaint.setShader(new LinearGradient(x_c, y_b - (offset * 2), 
					x_c, y_b, Color.WHITE, Color.DKGRAY, TileMode.MIRROR));
			avatarPaint.setAntiAlias(true);
			
			canvas.drawRoundRect(avatarRect, 5, 5, avatarPaint);
			canvas.drawBitmap(avatar, x_c - AVATAR_LENGTH, y_b - (AVATAR_LENGTH*2 + 10), null);
		}
		
		
//		canvas.drawText (text, point.x + 2*mRadius, point.y, paint);
		paint.setShader(null);
		paint.setARGB(250,255,255,255);
		canvas.drawTextOnPath(text, path, 0, 0, paint);
	}
	
}
