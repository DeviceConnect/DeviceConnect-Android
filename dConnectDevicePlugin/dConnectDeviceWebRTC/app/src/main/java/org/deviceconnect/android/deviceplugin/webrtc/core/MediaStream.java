/*
 MediaStream.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.webrtc.core;

import android.hardware.Camera;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.webrtc.BuildConfig;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioRecord;
import org.webrtc.voiceengine.WebRtcAudioRecordModule;
import org.webrtc.voiceengine.WebRtcAudioRecordModuleFactory;

/**
 * MediaStream.
 *
 * @author NTT DOCOMO, INC.
 */
public class MediaStream {
    /**
     * Tag for debugging.
     */
    private static final String TAG = "WEBRTC";

    /**
     * Defined video track id.
     *
     * Constant Value: {@value}
     */
    public static final String VIDEO_TRACK_ID = "ARDAMSv0";

    /**
     * Defined audio track id.
     *
     * Constant Value: {@value}
     */
    public static final String AUDIO_TRACK_ID = "ARDAMSa0";

    /**
     * Defined the label name of MediaStream.
     *
     * Constant Value: {@value}
     */
    public static final String ARDAMS = "ARDAMS";

    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT= "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT  = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";

    private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
    private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
    private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
    private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";
    private static final String MAX_VIDEO_FPS_CONSTRAINT = "maxFrameRate";
    private static final String MIN_VIDEO_FPS_CONSTRAINT = "minFrameRate";

    /**
     * Width of video.
     *
     * Constant Value: {@value}
     */
    private static final int HD_VIDEO_WIDTH = 1280;

    /**
     * Height of video.
     *
     * Constant Value: {@value}
     */
    private static final int HD_VIDEO_HEIGHT = 720;

    /**
     * Maximum value of video's width.
     *
     * Constant Value: {@value}
     */
    private static final int MAX_VIDEO_WIDTH = 1280;

    /**
     * Maximum value of video's height.
     *
     * Constant Value: {@value}
     */
    private static final int MAX_VIDEO_HEIGHT = 1280;

    /**
     * Maximum value of video's fps.
     *
     * Constant Value: {@value}
     */
    private static final int MAX_VIDEO_FPS = 30;

    /**
     * MediaStream instance of WebRTC.
     */
    private org.webrtc.MediaStream mMediaStream;

    /**
     * PeerConnectionFactory instance.
     */
    private PeerConnectionFactory mFactory;
    private VideoRenderer.Callbacks mVideoRender;
    private VideoCapturerAndroid mVideoCapturer;
    private VideoSource mVideoSource;
    private VideoTrack mVideoTrack;
    private boolean mEnableVideo = true;
    private boolean mEnableAudio = true;

    private MediaConstraints mVideoConstraints;
    private MediaConstraints mAudioConstraints;
    private MediaConstraints mSdpMediaConstraints;

    /**
     * Option.
     */
    private PeerOption mOption;

    // for local
    MediaStream(final PeerConnectionFactory factory, final PeerOption option) {
        mFactory = factory;
        mOption = option;
        createMediaConstraints(option);
        mMediaStream = createMediaStream();
    }

    // for remote
    MediaStream(final org.webrtc.MediaStream stream) {
        mMediaStream = stream;
    }

    /**
     * Sets a VideoRenderer.
     * @param render
     */
    public void setVideoRender(final VideoRenderer.Callbacks render) {
        mVideoRender = render;

        if (render != null && mMediaStream.videoTracks.size() == 1) {
            mVideoTrack = mMediaStream.videoTracks.get(0);
            mVideoTrack.setEnabled(mEnableVideo);
            mVideoTrack.addRenderer(new VideoRenderer(mVideoRender));
        }
    }

    /**
     * Closes a MediaStream.
     */
    public synchronized void close() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "@@@ MediaStream::close");
        }

        if (mVideoCapturer != null) {
            mVideoCapturer.dispose();
            mVideoCapturer = null;
        }

        if (mVideoSource != null) {
            mVideoSource.stop();
            mVideoSource.dispose();
            mVideoSource = null;
        }
    }

    /**
     * Gets a org.webrtc.MediaStream.
     * @return org.webrtc.MediaStream
     */
    public org.webrtc.MediaStream getMediaStream() {
        return mMediaStream;
    }

    /**
     * Returns the enabled video for this MediaStream.
     * @return true if video enable, false otherwise
     */
    public boolean isEnableVideo() {
        return mEnableVideo;
    }

    /**
     * Sets the enabled video of this MediaStream.
     * @param enableVideo true if video is enabled, false otherwise.
     */
    public void setEnableVideo(final boolean enableVideo) {
        mEnableVideo = enableVideo;
    }

    /**
     * Returns the enabled audio for this MediaStream.
     * @return true if audio enable, false otherwise
     */
    public boolean isEnableAudio() {
        return mEnableAudio;
    }

    /**
     * Sets the enabled audio of this MediaStream.
     * @param enableAudio true if audio is enabled, false otherwise.
     */
    public void setEnableAudio(final boolean enableAudio) {
        mEnableAudio = enableAudio;
    }

    /**
     * Starts a video shoot.
     */
    public void startVideo() {
        if (mVideoSource != null) {
            mVideoSource.restart();
        }
    }

    /**
     * Stops a video shoot.
     */
    public void stopVideo() {
        if (mVideoSource != null) {
            mVideoSource.stop();
        }
    }

    /**
     * Creates constraints of MediaStream.
     * @param option option of constraint
     */
    private void createMediaConstraints(final PeerOption option) {
        mVideoConstraints = new MediaConstraints();

        int videoWidth = option.getVideoWidth();
        int videoHeight = option.getVideoHeight();

        if (videoWidth == 0 || videoHeight == 0) {
            videoWidth = HD_VIDEO_WIDTH;
            videoHeight = HD_VIDEO_HEIGHT;
        }

        if (videoWidth > 0 && videoHeight > 0) {
            videoWidth = Math.min(videoWidth, MAX_VIDEO_WIDTH);
            videoHeight = Math.min(videoHeight, MAX_VIDEO_HEIGHT);
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    MIN_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    MAX_VIDEO_WIDTH_CONSTRAINT, Integer.toString(videoWidth)));
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    MIN_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    MAX_VIDEO_HEIGHT_CONSTRAINT, Integer.toString(videoHeight)));
        }

        int videoFps = option.getVideoFps();
        if (videoFps > 0) {
            videoFps = Math.min(videoFps, MAX_VIDEO_FPS);
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    MIN_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
            mVideoConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    MAX_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
        }

        // Create Audio constraints
        mAudioConstraints = new MediaConstraints();
        if (option.isNoAudioProcessing()) {
            mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_NOISE_SUPPRESSION_CONSTRAINT , "false"));
        }

        // Create SDP constraints.
        mSdpMediaConstraints = new MediaConstraints();
        if (option.getAudioType() != PeerOption.AudioType.NONE) {
            mSdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio", "true"));
        } else {
            mSdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveAudio", "false"));
            mEnableAudio = false;
        }
        if (option.getVideoType() != PeerOption.VideoType.NONE) {
            mSdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo", "true"));
        } else {
            mSdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    "OfferToReceiveVideo", "false"));
            mEnableVideo = false;
        }
    }

    /**
     * Creates an instance of org.webrtc.MediaStream.
     * @return org.webrtc.MediaStream
     */
    private org.webrtc.MediaStream createMediaStream() {
        org.webrtc.MediaStream mediaStream = mFactory.createLocalMediaStream(ARDAMS);

        if (mEnableVideo) {
            mVideoCapturer = createVideoCapturer();
            if (mVideoCapturer != null) {
                mediaStream.addTrack(createVideoTrack(mVideoCapturer));
            } else {
                mEnableVideo = false;
            }
        }

        if (mEnableAudio) {
            createAudioCapturer();
            mediaStream.addTrack(mFactory.createAudioTrack(
                    AUDIO_TRACK_ID,
                    mFactory.createAudioSource(mAudioConstraints)));
        }

        return mediaStream;
    }

    /**
     * Creates a instance of WebRtcAudioRecord.
     */
    private void createAudioCapturer() {
        if (mOption.getAudioType() == PeerOption.AudioType.EXTERNAL_RESOURCE) {
            WebRtcAudioRecord.setAudioRecordModuleFactory(new WebRtcAudioRecordModuleFactory() {
                @Override
                public WebRtcAudioRecordModule create() {
                    AudioCapturerExternalResource module = new AudioCapturerExternalResource();
                    module.setUri(mOption.getAudioUri());
                    module.setSampleRate(mOption.getAudioSampleRate());
                    module.setBitDepth(mOption.getAudioBitDepth());
                    module.setChannel(mOption.getAudioChannel());
                    return module;
                }
            });
        } else {
            WebRtcAudioRecord.setAudioRecordModuleFactory(null);
        }
    }

    /**
     * Creates a instance of VideoCapturerAndroid.
     * @return VideoCapturerAndroid
     */
    private VideoCapturerAndroid createVideoCapturer() {
        switch (mOption.getVideoType()) {
            default:
            case NONE:
                return null;
            case CAMERA:
                return createCameraCapture();
            case EXTERNAL_RESOURCE:
                return createExternalResource();
        }
    }

    /**
     * Creates a instance of VideoCapturerAndroid for Camera.
     * @return VideoCapturerAndroid
     */
    private VideoCapturerAndroid createCameraCapture() {
        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
        if (numberOfCameras == 0) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Failed to open camera.");
            }
            mEnableVideo = false;
            return null;
        } else {
            String deviceName = null;
            switch (mOption.getVideoFacing()) {
                case Camera.CameraInfo.CAMERA_FACING_BACK:
                    deviceName = CameraEnumerationAndroid.getNameOfBackFacingDevice();
                    break;
                case Camera.CameraInfo.CAMERA_FACING_FRONT:
                    deviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                    break;
                default:
                    deviceName = CameraEnumerationAndroid.getDeviceName(0);
                    break;
            }
            if (deviceName == null) {
                deviceName = CameraEnumerationAndroid.getDeviceName(0);
            }
            VideoCapturerAndroid videoCapturer  = VideoCapturerAndroid.create(deviceName,
                    new VideoCapturerAndroid.CameraEventsHandler() {
                        @Override
                        public void onCameraError(String s) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraError: " + s);
                            }
                        }

                        @Override
                        public void onCameraFreezed(String s) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraFreezed: " + s);
                            }
                        }

                        @Override
                        public void onCameraOpening(int i) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraOpening: " + i);
                            }
                        }

                        @Override
                        public void onFirstFrameAvailable() {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onFirstFrameAvailable: ");
                            }
                        }

                        @Override
                        public void onCameraClosed() {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraClosed: ");
                            }
                        }
                    }, mOption.getEglBase().getEglBaseContext());
            mEnableVideo = (videoCapturer != null);
            return videoCapturer;
        }
    }

    /**
     * Creates a instance of VideoCapturerAndroid for external resource.
     * @return VideoCapturerAndroid
     */
    private VideoCapturerAndroid createExternalResource() {
        if (mOption.getVideoUri() == null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "failed to open video uri.");
            }
            mEnableVideo = false;
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "createExternalResource: " + mOption.getVideoUri());
            }

            VideoCapturerExternalResource capturerObject = new VideoCapturerExternalResource(
                    mOption.getEglBase().getEglBaseContext(),
                    mOption.getVideoUri(), mOption.getVideoWidth(), mOption.getVideoHeight());
            VideoCapturerAndroid videoCapturer  = VideoCapturerAndroid.create(capturerObject,
                    new VideoCapturerAndroid.CameraEventsHandler() {
                        @Override
                        public void onCameraError(String s) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraError: " + s);
                            }
                        }

                        @Override
                        public void onCameraFreezed(String s) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraFreezed: " + s);
                            }
                        }

                        @Override
                        public void onCameraOpening(int i) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraOpening: " + i);
                            }
                        }

                        @Override
                        public void onFirstFrameAvailable() {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onFirstFrameAvailable: ");
                            }
                        }

                        @Override
                        public void onCameraClosed() {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onCameraClosed: ");
                            }
                        }
                    }, mOption.getEglBase().getEglBaseContext());
            mEnableVideo = (videoCapturer != null);
            return videoCapturer;
        }
        return null;
    }

    /**
     * Creates a instance of VideoTrack to used in a VideoCapturerAndroid.
     * @param capturer Instance of VideoCapturerAndroid
     * @return VideoTrack
     */
    private VideoTrack createVideoTrack(final VideoCapturerAndroid capturer) {
        mVideoRender = mOption.getRender();
        mVideoSource = mFactory.createVideoSource(capturer, mVideoConstraints);
        mVideoTrack = mFactory.createVideoTrack(VIDEO_TRACK_ID, mVideoSource);
        mVideoTrack.setEnabled(mEnableVideo);
        mVideoTrack.addRenderer(new VideoRenderer(mVideoRender));
        return mVideoTrack;
    }
}
