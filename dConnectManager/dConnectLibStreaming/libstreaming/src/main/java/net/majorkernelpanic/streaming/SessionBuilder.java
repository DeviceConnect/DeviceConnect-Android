/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
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

package net.majorkernelpanic.streaming;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.audio.AudioStream;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;
import net.majorkernelpanic.streaming.video.VideoStream;

public class SessionBuilder {

	public final static String TAG = "SessionBuilder";

	// Default configuration
	private VideoQuality mVideoQuality = VideoQuality.DEFAULT_VIDEO_QUALITY;
	private AudioQuality mAudioQuality = AudioQuality.DEFAULT_AUDIO_QUALITY;
	private Context mContext;
	private VideoStream mVideoStream;
	private AudioStream mAudioStream;
	private int mTimeToLive = 64;
	private int mOrientation = 0;
	private SurfaceView mSurfaceView = null;
	private String mOrigin = null;
	private String mDestination = null;
	private Session.Callback mCallback = null;

	/**
	 * Creates a new {@link Session}.
	 * @return The new Session
	 */
	public Session build() {
		Session session = new Session();
		session.setOrigin(mOrigin);
		session.setDestination(mDestination);
		session.setTimeToLive(mTimeToLive);
		session.setCallback(mCallback);

//		switch (mAudioEncoder) {
//		case AUDIO_AAC:
//			AACStream stream = new AACStream();
//			session.addAudioTrack(stream);
//			if (mContext != null) {
//                stream.setPreferences(PreferenceManager.getDefaultSharedPreferences(mContext));
//            }
//			break;
//		case AUDIO_AMRNB:
//			session.addAudioTrack(new AMRNBStream());
//			break;
//		}
		if (mAudioStream != null) {
			session.addAudioTrack(mAudioStream);
		}

//		switch (mVideoEncoder) {
//		case VIDEO_H264:
//			CameraH264Stream stream = new CameraH264Stream(mCameraId, mCamera);
//			if (mContext != null) {
//                stream.setPreferences(PreferenceManager.getDefaultSharedPreferences(mContext));
//            }
//			session.addVideoTrack(stream);
//			break;
//		}
		if (mVideoStream != null) {
			session.addVideoTrack(mVideoStream);
		}

		if (session.getVideoTrack() != null) {
			VideoStream video = session.getVideoTrack();
			video.setVideoQuality(mVideoQuality);
			if (mSurfaceView != null) {
			    video.setSurfaceView(mSurfaceView);
            }
			video.setPreviewOrientation(mOrientation);
			video.setDestinationPorts(5006);
		}

		if (session.getAudioTrack() != null) {
			AudioStream audio = session.getAudioTrack();
			audio.setAudioQuality(mAudioQuality);
			audio.setDestinationPorts(5004);
		}

		return session;
	}

	/** 
	 * Access to the context is needed for the CameraH264Stream class to store some stuff in the SharedPreferences.
	 * Note that you should pass the Application context, not the context of an Activity.
	 **/
	public SessionBuilder setContext(Context context) {
		mContext = context;
		return this;
	}

	/** Sets the destination of the session. */
	public SessionBuilder setDestination(String destination) {
		mDestination = destination;
		return this; 
	}

	/** Sets the origin of the session. It appears in the SDP of the session. */
	public SessionBuilder setOrigin(String origin) {
		mOrigin = origin;
		return this;
	}

	/** Sets the video stream quality. */
	public SessionBuilder setVideoQuality(VideoQuality quality) {
		mVideoQuality = quality.clone();
		return this;
	}

	public SessionBuilder setAudioStream(AudioStream stream) {
		mAudioStream = stream;
		return this;
	}
	
	/** Sets the audio quality. */
	public SessionBuilder setAudioQuality(AudioQuality quality) {
		mAudioQuality = quality.clone();
		return this;
	}

	public SessionBuilder setVideoStream(VideoStream stream) {
		mVideoStream = stream;
		return this;
	}

	public SessionBuilder setTimeToLive(int ttl) {
		mTimeToLive = ttl;
		return this;
	}

	/** 
	 * Sets the SurfaceView required to preview the video stream. 
	 **/
	public SessionBuilder setSurfaceView(SurfaceView surfaceView) {
		mSurfaceView = surfaceView;
		return this;
	}
	
	/** 
	 * Sets the orientation of the preview.
	 * @param orientation The orientation of the preview
	 */
	public SessionBuilder setPreviewOrientation(int orientation) {
		mOrientation = orientation;
		return this;
	}	
	
	public SessionBuilder setCallback(Session.Callback callback) {
		mCallback = callback;
		return this;
	}
	
	/** Returns the context set with {@link #setContext(Context)}*/
	public Context getContext() {
		return mContext;	
	}

	/** Returns the destination ip address set with {@link #setDestination(String)}. */
	public String getDestination() {
		return mDestination;
	}

	/** Returns the origin ip address set with {@link #setOrigin(String)}. */
	public String getOrigin() {
		return mOrigin;
	}

	/** Returns the VideoQuality set with {@link #setVideoQuality(VideoQuality)}. */
	public VideoQuality getVideoQuality() {
		return mVideoQuality;
	}
	
	/** Returns the AudioQuality set with {@link #setAudioQuality(AudioQuality)}. */
	public AudioQuality getAudioQuality() {
		return mAudioQuality;
	}

	/** Returns the SurfaceView set with {@link #setSurfaceView(SurfaceView)}. */
	public SurfaceView getSurfaceView() {
		return mSurfaceView;
	}

	/** Returns the time to live set with {@link #setTimeToLive(int)}. */
	public int getTimeToLive() {
		return mTimeToLive;
	}

	/** Returns a new {@link SessionBuilder} with the same configuration. */
	public SessionBuilder clone() {
		return new SessionBuilder()
		.setDestination(mDestination)
		.setOrigin(mOrigin)
		.setSurfaceView(mSurfaceView)
		.setPreviewOrientation(mOrientation)
		.setVideoQuality(mVideoQuality)
		.setVideoStream(mVideoStream)
		.setTimeToLive(mTimeToLive)
		.setAudioStream(mAudioStream)
		.setAudioQuality(mAudioQuality)
		.setContext(mContext)
		.setCallback(mCallback);
	}

}