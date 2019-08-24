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

package net.majorkernelpanic.streaming.video;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Base64;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.hw.EncoderDebugger;
import net.majorkernelpanic.streaming.mp4.MP4Config;
import net.majorkernelpanic.streaming.rtp.H264Packetizer;

import java.io.IOException;
import java.net.InetAddress;

/**
 * A class for streaming H.264 from the camera of an android device using RTP.
 * You should use a {@link Session} instantiated with {@link SessionBuilder} instead of using this class directly.
 * Call {@link #setDestinationAddress(InetAddress)}, {@link #setDestinationPorts(int)} and {@link #setVideoQuality(VideoQuality)}
 * to configure the stream. You can then call {@link #start()} to start the RTP stream.
 * Call {@link #stop()} to stop the stream.
 */
public class CameraH264Stream extends CameraVideoStream {

    private MP4Config mConfig;

	/**
	 * Constructs the H.264 stream.
	 * @param cameraId Can be either CameraInfo.CAMERA_FACING_BACK or CameraInfo.CAMERA_FACING_FRONT
	 * @param camera
	 */
	public CameraH264Stream(int cameraId, Camera camera) {
		super(cameraId, camera);
		mMimeType = "video/avc";
		mCameraImageFormat = ImageFormat.NV21;
		mPacketizer = new H264Packetizer();
	}

	/**
	 * Returns a description of the stream using SDP. It can then be included in an SDP file.
	 */
	public synchronized String getSessionDescription() throws IllegalStateException {
		if (mConfig == null) {
			throw new IllegalStateException("You need to call configure() first !");
		}
		return "m=video "+String.valueOf(getDestinationPorts()[0])+" RTP/AVP 96\r\n" +
		"a=rtpmap:96 H264/90000\r\n" +
		"a=fmtp:96 packetization-mode=1;profile-level-id="+mConfig.getProfileLevel()+";sprop-parameter-sets="+mConfig.getB64SPS()+","+mConfig.getB64PPS()+";\r\n";
	}	

	/**
	 * Starts the stream.
	 * This will also open the camera and display the preview if {@link #start()} ()} has not already been called.
	 */
	public synchronized void start() throws IllegalStateException, IOException {
		if (!mStreaming) {
			configure();
			byte[] pps = Base64.decode(mConfig.getB64PPS(), Base64.NO_WRAP);
			byte[] sps = Base64.decode(mConfig.getB64SPS(), Base64.NO_WRAP);
			((H264Packetizer)mPacketizer).setStreamParameters(pps, sps);
			super.start();
		}
	}

	/**
	 * Configures the stream. You need to call this before calling {@link #getSessionDescription()} to apply
	 * your configuration of the stream.
	 */
	public synchronized void configure() throws IllegalStateException, IOException {
		super.configure();
		mMode = mRequestedMode;
		mQuality = mRequestedQuality.clone();
		mConfig = testMediaCodecAPI();
	}

    /**
     * Tests if streaming with the given configuration (bit rate, frame rate, resolution) is possible
     * and determines the pps and sps. Should not be called by the UI thread.
     **/
	private MP4Config testMediaCodecAPI() throws RuntimeException, IOException {
		EncoderDebugger debugger = EncoderDebugger.debug(mSettings, mQuality.resX, mQuality.resY);
        return new MP4Config(debugger.getB64SPS(), debugger.getB64PPS());
	}
	
}
