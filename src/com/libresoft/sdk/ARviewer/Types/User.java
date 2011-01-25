/*
 *
 *  Copyright (C) 2009-2011 GSyC/LibreSoft, Universidad Rey Juan Carlos
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
 *  Author : Roberto Calvo Palomino <rocapal@libresoft.es>
 *
 */

package com.libresoft.sdk.ARviewer.Types;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import com.libresoft.sdk.ARviewer.Utils.BitmapUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class User extends GeoNode implements Serializable {

	// Serializable UID
	private static final long serialVersionUID = -9121943636715457236L;
	
	private String  mName;	
	private String  mLastName;
	private String  mUsername;
	private String  mEmail;
	private String  mStatus;
	private String  mStatus_since;
	private String  mPassword;
	
	private ArrayList<User> mFriends;
	private String mGroups;
	private String mPhotos;
	
	private String mCountry;
	private Integer mPostCode;
	private String mBirthday;
	
	private Integer mAvatarId;
	private String mAvatarUrl;
	
	byte[]  mByteBitMapAvatar;
	
	
	public User (Integer id, String name, String lastName, String username, String email, String state,
			     Double latitude, Double longitude, Double altitude, Double radius, String since,
			     String country, Integer postCode, String birthday, ArrayList<User> friends, Integer avatarId, String avatarUrl, String status_since,
			     String position_since)
	{
		super(id,latitude,longitude,altitude,radius,since,position_since);
		

		mName = name;
		mUsername = username;
		mEmail = email;
		mStatus = state;
		mStatus_since = status_since;
	
		mFriends = friends;
		
		mCountry = country;
		mPostCode = postCode;
		mBirthday = birthday;
		
		mLastName = lastName;
		
		if(avatarId == null)
			mAvatarId = 0;
		else
			mAvatarId = avatarId;
		mAvatarUrl = avatarUrl;
		
		mByteBitMapAvatar=null;
		
	}
	

	public String getName() {
		return mName;
	}
	public String getLastName() {
		return mLastName;
	}
	public String getUsername() {
		return mUsername;
	}
	public String getEmail() {
		return mEmail;
	}
	public String getState() {
		return mStatus;
	}

	public ArrayList<User> getFriends()
	{
		return mFriends;
	}
	
	public String getPassword()
	{
		return mPassword;
	}
	
	public void setPassword (String password)
	{
		mPassword = password;
	}


	public String getCountry() {
		return mCountry;
	}


	public void setCountry(String country) {
		mCountry = country;
	}


	public Integer getPostCode() {
		return mPostCode;
	}


	public void setPostCode(Integer postCode) {
		mPostCode = postCode;
	}


	public String getBirthday() {
		return mBirthday;
	}


	public void setBirthday(String birthday) {
		mBirthday = birthday;
	}
	
	public String getAvatarUrl()
	{
		return mAvatarUrl;
	}
	
	public Integer getAvatarId()
	{
		return mAvatarId;
	}
	
	public void setAvatarBitmap (Bitmap avatar)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		if (!avatar.compress(Bitmap.CompressFormat.JPEG, 100, baos))
		{
			Log.e("getAvatarBitmap","Error: Don't compress de image");
			return;
		}
		Log.e("setAvatarBitmap", "Compress correctly");
		mByteBitMapAvatar = baos.toByteArray();
	}
	
	public Bitmap getAvatarBitmap ()
	{
		if (getAvatarId()==0)
			return null;
		
		Bitmap bitmapImage = null;
		
		if (mByteBitMapAvatar == null)
		{
			Log.d("AVATAR",getAvatarUrl());
						
			bitmapImage = BitmapUtils.loadBitmap(mAvatarUrl);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			if (!bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos))
			{
				Log.e("getAvatarBitmap","Error: Don't compress de image");
				return null;
			}
			mByteBitMapAvatar = baos.toByteArray();
			
		}
		
		return BitmapFactory.decodeStream( new ByteArrayInputStream( mByteBitMapAvatar) );
		
	}
	
	public boolean isBitmapAvatar(){
		return (mByteBitMapAvatar!=null);
	}
}
