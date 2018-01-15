/*
 * Copyright (C) 2011-2015 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.majorkernelpanic.streaming.video;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

import net.majorkernelpanic.streaming.MediaStream;
import net.majorkernelpanic.streaming.Stream;
import net.majorkernelpanic.streaming.exceptions.CameraInUseException;
import net.majorkernelpanic.streaming.exceptions.InvalidSurfaceException;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.hw.EncoderDebugger;
import net.majorkernelpanic.streaming.hw.NV21Convertor;
import net.majorkernelpanic.streaming.rtp.MediaCodecInputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/** 
 * Don't use this class directly.
 */
public abstract class VideoStream extends MediaStream {

	protected final static String TAG = "VideoStream";

	protected VideoQuality mRequestedQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
	protected VideoQuality mQuality = mRequestedQuality.clone(); 
	protected SurfaceHolder.Callback mSurfaceHolderCallback;
	protected SurfaceView mSurfaceView;
	protected SharedPreferences mSettings;
	protected int mRequestedOrientation = 0;
	protected int mOrientation = 0;

	protected boolean mSurfaceReady;
	protected boolean mUpdated;
	
	protected String mMimeType;
	protected int mCameraImageFormat;

	/**
	 * Sets a Surface to show a preview of recorded media (video). 
	 * You can call this method at any time and changes will take effect next time you call {@link #start()}.
	 */
	public synchronized void setSurfaceView(SurfaceView view) {
		mSurfaceView = view;
		if (mSurfaceHolderCallback != null && mSurfaceView != null && mSurfaceView.getHolder() != null) {
			mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
		}
		if (mSurfaceView.getHolder() != null) {
			mSurfaceHolderCallback = new Callback() {
				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					mSurfaceReady = false;
					Log.d(TAG,"Surface destroyed !");
				}
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					mSurfaceReady = true;
				}
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					Log.d(TAG,"Surface Changed !");
				}
			};
			mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
			mSurfaceReady = true;
		}
	}

	/** 
	 * Sets the orientation of the preview.
	 * @param orientation The orientation of the preview
	 */
	public void setPreviewOrientation(int orientation) {
		mRequestedOrientation = orientation;
		mUpdated = false;
	}

	/** 
	 * Sets the configuration of the stream. You can call this method at any time 
	 * and changes will take effect next time you call {@link #configure()}.
	 * @param videoQuality Quality of the stream
	 */
	public void setVideoQuality(VideoQuality videoQuality) {
		if (!mRequestedQuality.equals(videoQuality)) {
			mRequestedQuality = videoQuality.clone();
			mUpdated = false;
		}
	}

	/** 
	 * Returns the quality of the stream.  
	 */
	public VideoQuality getVideoQuality() {
		return mRequestedQuality;
	}

	/**
	 * Some data (SPS and PPS params) needs to be stored when {@link #getSessionDescription()} is called 
	 * @param prefs The SharedPreferences that will be used to save SPS and PPS parameters
	 */
	public void setPreferences(SharedPreferences prefs) {
		mSettings = prefs;
	}

	/**
	 * Configures the stream. You need to call this before calling {@link #getSessionDescription()} 
	 * to apply your configuration of the stream.
	 */
	public synchronized void configure() throws IllegalStateException, IOException {
		super.configure();
		mOrientation = mRequestedOrientation;
	}

	/**
	 * Returns a description of the stream using SDP. 
	 * This method can only be called after {@link Stream#configure()}.
	 * @throws IllegalStateException Thrown when {@link Stream#configure()} wa not called.
	 */	
	public abstract String getSessionDescription() throws IllegalStateException;

}
