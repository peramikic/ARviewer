/*
 *  Copyright (C) 2010 GSyC/LibreSoft, Universidad Rey Juan Carlos.
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
 *  Author : Raúl Román López <rroman@gsyc.es>
 *
 */

package com.libresoft.apps.ARviewer;


import com.libresoft.apps.ARviewer.Overlays.CustomViews;
import com.libresoft.apps.ARviewer.Tagging.AccurateTag;
import com.libresoft.apps.ARviewer.Tagging.MapTagging;
import com.libresoft.apps.ARviewer.Tagging.TagResult;
import com.libresoft.apps.ARviewer.Utils.LocationUtils;
import com.libresoft.sdk.ARviewer.Types.GenericLayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ARTagManager{
	private static final int MODULE_BASE = 10600;
	
	private static final int MENU_TAGGING = MODULE_BASE + 1;
	private static final int MENU_TAGGING_IMMEDIATE = MENU_TAGGING + 1;
	private static final int MENU_TAGGING_FAST = MENU_TAGGING_IMMEDIATE + 1;
	private static final int MENU_TAGGING_MAP = MENU_TAGGING_FAST + 1;
	private static final int MENU_TAGGING_ACCURATE_SIDE = MENU_TAGGING_MAP + 1;
	private static final int MENU_TAGGING_ACCURATE_LINE = MENU_TAGGING_ACCURATE_SIDE + 1;
//	private static final int MENU_TAGGING_ANGLE = MENU_TAGGING_ACCURATE_LINE + 1;
	
	public static final int DIALOG_MOVE = MODULE_BASE + 1;
	
	public static final int ACTIVITY_MAP = MODULE_BASE + 1;
	public static final int ACTIVITY_RESULT =  ACTIVITY_MAP + 1;
	
	public static final int TAG_NONE = -1;
	public static final int TAG_IMMEDIATE = 0;
	public static final int TAG_FAST = 1;
	public static final int TAG_MAP = 2;
	public static final int TAG_ACCURATE_SIDE = 3;
	public static final int TAG_ACCURATE_LINE = 4;
//	public static final int TAG_ANGLE = 5;
	
	private OnLocationChangeListener onLocationChangeListener = null;
	private OnTaggingFinishedListener onTaggingFinishedListener = null;
	
	private RelativeLayout tagIFContainer;
	
	private int isLocationServiceOn = -1;
	
	private int savingType;
	private float[] user_location = new float[3];
	private float[] user_location_fixed = new float[3];
	private float cam_altitude_inst;
	private float cam_altitude;

	private float[] resource_location = new float[3];
    private static AccurateTag accurateTag;
//    private static AngleTag angleTag;
    private float[] angles = new float[3];
    private float[] res_angles = new float[3];
    private double distance = 0;
	
	private Activity mActivity;
	private ARLayerManager layers;
	private GenericLayer myLayer;
	
	OnClickListener fast_click = new OnClickListener() {
		
		public void onClick(View v) {
			distance = CustomViews.getSeekbarValue();
			layers.removeExtraElement((View) v.getParent());
			tagAction();
		}
	};
	
	OnClickListener ok_click = new OnClickListener() {
		public void onClick(View v) {
			tagAction();
		}
	};
	
	public ARTagManager(Activity mActivity, ARLayerManager layers, GenericLayer myLayer, float[] user_location, float cam_altitude){
		this.mActivity = mActivity;
		this.layers = layers;
		this.myLayer = myLayer;
		setSavingType(TAG_NONE);
		setUserLocation(user_location);
		setCamAltitude(cam_altitude);
	}
	
	public void setLocationServiceOn(int isLocationServiceOn){
		this.isLocationServiceOn = isLocationServiceOn;
	}
	
	public void setSavingType(int savingType){
		this.savingType = savingType;
		switch(savingType){
		case TAG_IMMEDIATE:
			tagAction();
			return;
		case TAG_FAST:
			tagIFContainer = new RelativeLayout(mActivity);
			tagIFContainer.addView(CustomViews.createSeekBar(mActivity.getBaseContext(), 500, 1, "m.", fast_click));
			layers.addExtraElement(tagIFContainer, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			return;
		case TAG_MAP:
			break;
		case TAG_ACCURATE_LINE:
			accurateTag = new AccurateTag();
			break;
		case TAG_ACCURATE_SIDE:
			accurateTag = new AccurateTag();
			break;
//		case TAG_ANGLE:
//			angleTag = new AngleTag();
//			break;
		default:
			return;
		}
		tagIFContainer = new RelativeLayout(mActivity);
		tagIFContainer.addView(CustomViews.createButton(mActivity, ok_click));
		layers.addExtraElement(tagIFContainer, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
	}
	
	public void setUserLocation(float[] user_location){
		this.user_location = user_location.clone();
	}
	
	public void setUserLocationFixed(float[] user_location){
		this.user_location_fixed = user_location;
	}
	
	public void setResourceLocation(float[] resource_location){
		this.resource_location = resource_location.clone();
	}
	
	public void setAngles(float[] angles){
		this.angles = angles.clone();
	}
	
	public void setCamAltitude(float cam_altitude_inst){
		this.cam_altitude_inst = cam_altitude_inst;
	}
	
	public int getSavingType(){
		return savingType;
	}
	
	public float[] getResourceLocation(){
		return resource_location;
	}
	
	public float[] getUserLocation(){
		return user_location;
	}
	
	public float[] getAngles(){
		return angles;
	}
	
    public void launchResult(){
    	//layers.cleanExtraLayer();
    	Location loc = resourceCoords(savingType);
    	savingType = TAG_NONE;
		if(loc == null)
			Toast.makeText(mActivity, 
					"Error",
					Toast.LENGTH_LONG).show();
		else{
			
			Location myLoc = new Location("");
			myLoc.setLatitude(user_location_fixed[0]);
			myLoc.setLongitude(user_location_fixed[1]);

			Intent i = new Intent(mActivity, TagResult.class);
			i.putExtra("LAYER_ID", myLayer.getId());
			i.putExtra("LATITUDE", loc.getLatitude());
			i.putExtra("LONGITUDE", loc.getLongitude());
			i.putExtra("ALTITUDE", loc.getAltitude());
			i.putExtra("DISTANCE", Double.toString((double) myLoc.distanceTo(loc)));
			i.putExtra("HEIGHT", Double.toString((double)loc.getAltitude()- cam_altitude));
			
			mActivity.startActivityForResult(i, ACTIVITY_RESULT);
			
		}
    	
    }
	
    public Location resourceCoords(int savingType){
    	Location resource =  new Location("TAG");
    	float[] res_location;
    	float res_distance, res_height;
    	
    	
    	switch(savingType){
    	
    	case TAG_IMMEDIATE:
    		res_location = user_location_fixed;
    		break;
    	
    	case TAG_FAST:
    		res_location = LocationUtils.calculateIntersection(user_location_fixed, ARCompassManager.getAzimuth(res_angles), (float)distance);
    		break;
    		
    	case TAG_MAP:
    		res_location = resource_location;
    		break;
    		
    	case TAG_ACCURATE_SIDE:
    		res_location = accurateTag.getLocationArray();
    		break;
    	
    	case TAG_ACCURATE_LINE:
    		res_location = accurateTag.getLineLocationArray();
    		break;
    		
//    	case TAG_ANGLE:
//    		res_location = angleTag.getLocationArray();
//    		break;
    		
    	default:
    		res_location = new float[2];	
    		
    	}
    	
    	if(res_location==null)
    		return null;
    	
    	res_distance = LocationUtils.calculateDistance(user_location_fixed, res_location);
    	res_height = LocationUtils.calculateResourceHeight(ARCompassManager.getElevation(res_angles), res_distance);
    	
    	resource.setLatitude(res_location[0]);
    	resource.setLongitude(res_location[1]);
    	resource.setAltitude(res_height);
    	
    	//FIXME Testing lines
//    	TestingManager.setFinalTime();
//    	TestingManager.setResLoc(res_location[0], res_location[1], res_height);
//    	TestingManager.setUserLoc(user_location_fixed[0], user_location_fixed[1], cam_altitude);
//    	TestingManager.setAzimuth(ARCompassManager.getAzimuth(res_angles));
//    	TestingManager.setInclination(ARCompassManager.getElevation(res_angles));
//    	TestingManager.resetRealLocation();
    	
    	return resource;
    }
    
    public boolean tagAction(){
    	user_location_fixed = user_location.clone();
    	res_angles = angles.clone();
    	cam_altitude = cam_altitude_inst;
		
    	switch(savingType){

    	case TAG_IMMEDIATE:
    		launchResult();
    		break;

    	case TAG_FAST:
    		launchResult();
    		break;

    	case TAG_MAP:

    		Intent i = new Intent(mActivity, MapTagging.class);
    		i.putExtra("LATITUDE", user_location_fixed[0]);
    		i.putExtra("LONGITUDE", user_location_fixed[1]);
    		i.putExtra("AZIMUTH", ARCompassManager.getAzimuth(res_angles));
    		mActivity.startActivityForResult(i, ACTIVITY_MAP);

//    		is_resource_clickable=true;

    		break;

    	case TAG_ACCURATE_SIDE:
    		if(!accurateTag.addElem(user_location_fixed, ARCompassManager.getAzimuth(res_angles), ARCompassManager.getElevation(res_angles))){
    			mActivity.showDialog(DIALOG_MOVE);
    			Toast.makeText(mActivity, 
    					"Limit reached", 
    					Toast.LENGTH_LONG).show();
    		}else{
    			mActivity.showDialog(DIALOG_MOVE);
    			Toast.makeText(mActivity, 
    					"Saved", 
    					Toast.LENGTH_LONG).show();
    			return false;
    		}
    		break;

    	case TAG_ACCURATE_LINE:
    		if(!accurateTag.addElem(user_location_fixed, ARCompassManager.getAzimuth(res_angles), ARCompassManager.getElevation(res_angles))){
    			mActivity.showDialog(DIALOG_MOVE);
    			Toast.makeText(mActivity, 
    					"Limit reached", 
    					Toast.LENGTH_LONG).show();
    		}else{
    			mActivity.showDialog(DIALOG_MOVE);
    			Toast.makeText(mActivity, 
    					"Saved", 
    					Toast.LENGTH_LONG).show();
    			return false;
    		}
    		break;

//    	case TAG_ANGLE:
//
//    		if(angleTag.addElem(user_location_fixed, ARCompassManager.getAzimuth(res_angles), ARCompassManager.getElevation(res_angles))){
//    			launchResult();
//    		}else{
//    			Toast.makeText(mActivity, 
//    					"Saved", 
//    					Toast.LENGTH_LONG).show();
//    		}
//    		break;
//
    	}
    	return true;
    }
    
    
    public void onCreateOptionsMenu(Menu menu) {
    	
    	SubMenu sub0 = menu.addSubMenu(0, MENU_TAGGING, 0, "Tag resource")
    	.setIcon(R.drawable.tag);
    	sub0.add(0,MENU_TAGGING_IMMEDIATE, 0, "Immediate");
    	sub0.add(0,MENU_TAGGING_FAST, 0, "Fast");
    	sub0.add(0,MENU_TAGGING_MAP, 0, "Map");
    	sub0.add(0,MENU_TAGGING_ACCURATE_SIDE, 0, "Accurate side");
    	sub0.add(0,MENU_TAGGING_ACCURATE_LINE, 0, "Accurate line");
    	//    		sub0.add(0,MENU_TAGGING_ANGLE, 0, "Angular");
    		
    }
    
    public boolean onOptionsItemSelected (MenuItem item) {
    	
    	switch (item.getItemId()) {

    	case MENU_TAGGING_IMMEDIATE:
    		setSavingType(TAG_IMMEDIATE);
    		break;

    	case MENU_TAGGING_FAST:
    		setSavingType(TAG_FAST);
    		break;

    	case MENU_TAGGING_MAP:
    		setSavingType(TAG_MAP);
    		break;

    	case MENU_TAGGING_ACCURATE_SIDE:
    		setSavingType(TAG_ACCURATE_SIDE);
    		break;

    	case MENU_TAGGING_ACCURATE_LINE:
    		setSavingType(TAG_ACCURATE_LINE);
    		break;

//    	case MENU_TAGGING_ANGLE:
//    		setSavingType(TAG_ANGLE);
//    		break;

    	default:
    		return false;
    	}
        return true;
    }
    
    public Dialog onCreateDialog(int id) {    
    	switch (id) {
    	case DIALOG_MOVE:

    		LayoutInflater factory1 = LayoutInflater.from(mActivity);
    		final View textEntryView1 = factory1.inflate(R.layout.dialog_movement, null);

    		OnClickListener moveStraightListener = new OnClickListener(){
    			
    			public void onClick(View v) {
    				float[] user_location = LocationUtils.moveTo(getUserLocation(), ARCompassManager.getAzimuth(getAngles()), 
    						LocationUtils.MOVE_STRAIGHT, 1);

    				setUserLocation(user_location);
    				setUserLocationFixed(user_location);
    				
    				if(onLocationChangeListener != null)
    					onLocationChangeListener.onChange(user_location);
    				
    				mActivity.dismissDialog(DIALOG_MOVE);
    			}

    		};

    		OnClickListener moveRightListener = new OnClickListener(){
    			
    			public void onClick(View v) {
    				float[] user_location = LocationUtils.moveTo(getUserLocation(), ARCompassManager.getAzimuth(getAngles()), 
    						LocationUtils.MOVE_RIGHT, 1);

    				setUserLocation(user_location);
    				setUserLocationFixed(user_location);
    				
    				if(onLocationChangeListener != null)
    					onLocationChangeListener.onChange(user_location);
    				
    				mActivity.dismissDialog(DIALOG_MOVE);
    			}

    		};

    		OnClickListener moveBackListener = new OnClickListener(){
    			
    			public void onClick(View v) {
    				float[] user_location = LocationUtils.moveTo(getUserLocation(), ARCompassManager.getAzimuth(getAngles()), 
    						LocationUtils.MOVE_BACK, 1);

    				setUserLocation(user_location);
    				setUserLocationFixed(user_location);
    				
    				if(onLocationChangeListener != null)
    					onLocationChangeListener.onChange(user_location);
    				
    				mActivity.dismissDialog(DIALOG_MOVE);
    			}

    		};

    		OnClickListener moveLeftListener = new OnClickListener(){
    			
    			public void onClick(View v) {
    				float[] user_location = LocationUtils.moveTo(getUserLocation(), ARCompassManager.getAzimuth(getAngles()), 
    						LocationUtils.MOVE_LEFT, 1);

    				setUserLocation(user_location);
    				setUserLocationFixed(user_location);
    				
    				if(onLocationChangeListener != null)
    					onLocationChangeListener.onChange(user_location);
    				
    				mActivity.dismissDialog(DIALOG_MOVE);
    			}

    		};

    		Button btnStraight = (Button) textEntryView1.findViewById (R.id.btMoveStraight);
    		btnStraight.setClickable(true);
    		btnStraight.setOnClickListener(moveStraightListener);

    		Button btnBack = (Button) textEntryView1.findViewById (R.id.btMoveBack);
    		btnBack.setClickable(true);
    		btnBack.setOnClickListener(moveBackListener);

    		Button btnLeft = (Button) textEntryView1.findViewById (R.id.btMoveLeft);
    		btnLeft.setClickable(true);
    		btnLeft.setOnClickListener(moveLeftListener);

    		Button btnRight = (Button) textEntryView1.findViewById (R.id.btMoveRight);
    		btnRight.setClickable(true);
    		btnRight.setOnClickListener(moveRightListener);

    		if(isLocationServiceOn!=-1){
    			btnLeft.setVisibility(View.INVISIBLE);
    			btnRight.setVisibility(View.INVISIBLE);
    			btnStraight.setVisibility(View.INVISIBLE);
    			btnBack.setVisibility(View.INVISIBLE);
    		}

    		if(getSavingType() == ARTagManager.TAG_ACCURATE_LINE){
    			btnLeft.setVisibility(View.INVISIBLE);
    			btnRight.setVisibility(View.INVISIBLE);

    		}else{
    			btnStraight.setVisibility(View.INVISIBLE);
    			btnBack.setVisibility(View.INVISIBLE);
    		}

    		return new AlertDialog.Builder(mActivity)	      
    		.setCancelable(false)
    		.setTitle("Move for tagging")
    		.setView(textEntryView1)
    		.setPositiveButton("Done", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {

    				/* User clicked OK so do some stuff */ 
    				onTaggingFinishedListener.onFinish(true);
    				layers.removeExtraElement(tagIFContainer);
    				launchResult();
    				mActivity.removeDialog(DIALOG_MOVE);

    			}
    		})
    		.setNeutralButton("Skip", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {

    				/* User clicked OK so do some stuff */ 

    			}
    		})
    		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {

    				/* User clicked OK so do some stuff */ 

    				onTaggingFinishedListener.onFinish(false);
    				setSavingType(TAG_NONE);

    				layers.removeExtraElement(tagIFContainer);
    				mActivity.removeDialog(DIALOG_MOVE);
    			}
    		})
    		.create();
    	}
    	return null;
    }
    
    public boolean onActivityResult (int requestCode, int resultCode, Intent data) { 
		switch (requestCode) { 
		case ACTIVITY_MAP:

			if( resultCode != Activity.RESULT_CANCELED ) {

				float[] location = {data.getFloatExtra("RES_LATITUDE", 0), data.getFloatExtra("RES_LONGITUDE", 0), 0};
				setResourceLocation(location);

				launchResult();	    		
			} else{
				onTaggingFinishedListener.onFinish(false);
				setSavingType(TAG_NONE);
			}
			return true;

		case ACTIVITY_RESULT:

			if( resultCode != Activity.RESULT_CANCELED ) {

				onTaggingFinishedListener.onFinish(true);	    		
			} else{
				onTaggingFinishedListener.onFinish(false);
			}
			layers.removeExtraElement(tagIFContainer);
			return true;
    	default:
    		break;
		}
		return false;
    }
    
    
    
    

    public void setOnLocationChangeListener(OnLocationChangeListener onLocationChangeListener){
    	this.onLocationChangeListener = onLocationChangeListener;
    }
    
    public void setOnTaggingFinishedListener(OnTaggingFinishedListener onTaggingFinishedListener){
    	this.onTaggingFinishedListener = onTaggingFinishedListener;
    }
    
    public void unregisterListeners(){
    	onLocationChangeListener = null;
    	onTaggingFinishedListener = null;
    }
    
    public interface OnLocationChangeListener {
		public abstract void onChange(float[] values);
	}
    
    public interface OnTaggingFinishedListener {
		public abstract void onFinish(boolean success);
	}
}