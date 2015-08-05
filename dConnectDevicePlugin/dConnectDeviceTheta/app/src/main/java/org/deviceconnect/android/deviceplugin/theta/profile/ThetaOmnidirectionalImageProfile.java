package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.theta.opengl.SphereRenderer;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.roi.OmnidirectionalImage;
import org.deviceconnect.android.deviceplugin.theta.roi.RoiDeliveryContext;
import org.deviceconnect.android.deviceplugin.theta.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.deviceplugin.theta.opengl.PixelBuffer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.OmnidirectionalImageProfile;
import org.deviceconnect.message.DConnectMessage;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Theta Omnidirectional Image Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaOmnidirectionalImageProfile extends OmnidirectionalImageProfile
    implements RoiDeliveryContext.OnChangeListener, SensorEventListener {

    private static final long ROI_DELIVERY_INTERVAL = 200;
    private long mSensorEventTimestamp;
    private long mInterval;
    private float[] mRotationDelta = new float[3];

    private final Object lockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private Map<String, RoiDeliveryContext> mRoiContexts =
        Collections.synchronizedMap(new HashMap<String, RoiDeliveryContext>());

    @Override
    protected boolean onPutView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (lockObj) {
                        if (mServer == null) {
                            mServer = new MixedReplaceMediaServer();
                            mServer.setServerName("ThetaDevicePlugin Server");
                            mServer.setContentType("image/jpeg");
                            mServer.setServerEventListener(new MixedReplaceMediaServer.ServerEventListener() {
                                @Override
                                public void onConnect(final String uri) {
                                    final String key = Uri.parse(uri).getLastPathSegment();
                                    final RoiDeliveryContext target = mRoiContexts.get(uri);
                                    if (target != null) {
                                        final Timer timer = new Timer(); // TODO delete
                                        timer.schedule(new TimerTask() {

                                            private static final int MAX_COUNT = 4;
                                            private int mCount;

                                            @Override
                                            public void run() {
                                                if ((mCount++) < MAX_COUNT) {
                                                    mServer.offerMedia(key, target.getRoi());
                                                } else {
                                                    timer.cancel();
                                                }
                                            }
                                        }, 0, 250);
                                    }
                                }

                                @Override
                                public void onDisconnect(final String uri) {
                                    mRoiContexts.remove(uri);
                                }

                                @Override
                                public void onCloseServer() {
                                    mRoiContexts.clear();
                                }
                            });
                            mServer.start();
                        }
                    }

                    RoiDeliveryContext roiContext =
                        new RoiDeliveryContext(getContext(), new OmnidirectionalImage(source));
                    String segment = UUID.randomUUID().toString();
                    String uri = mServer.getUrl() + "/" + segment;
                    roiContext.setUri(Uri.parse(uri));
                    roiContext.setOnChangeListener(ThetaOmnidirectionalImageProfile.this);
                    roiContext.render(RoiDeliveryContext.DEFAULT_PARAM);
                    mRoiContexts.put(uri, roiContext);

                    setResult(response, DConnectMessage.RESULT_OK);
                    setURI(response, uri);
                } catch (MalformedURLException e) {
                    MessageUtils.setInvalidRequestParameterError(response, "uri is malformed: " + source);
                } catch (FileNotFoundException e) {
                    MessageUtils.setUnknownError(response, "Image is not found: " + source);
                } catch (IOException e) {
                    MessageUtils.setUnknownError(response, e.getMessage());
                } catch (Throwable e) {
                    e.printStackTrace();
                    MessageUtils.setUnknownError(response, e.getMessage());
                }
                ((ThetaDeviceService) getContext()).sendResponse(response);
            }
        }).start();
        return false;
    }

    @Override
    protected boolean onDeleteView(final Intent request, final Intent response, final String serviceId,
                                   final String uri) {
        mRoiContexts.remove(uri);
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutSettings(final Intent request, final Intent response, final String serviceId) {
        final String uri = getURI(request);
        final RoiDeliveryContext roiContext = mRoiContexts.get(uri);
        if (roiContext == null) {
            MessageUtils.setInvalidRequestParameterError(response, "The specified media is not found.");
            return true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String segment = Uri.parse(uri).getLastPathSegment();

                RoiDeliveryContext.Param param = parseParam(request);
                if (param.isVrMode()) {
                    if (!startVrMode(roiContext)) {
                        MessageUtils.setInvalidRequestParameterError(response,
                            "VR mode cannot be supported because no gyroscope is found.");
                        ((ThetaDeviceService) getContext()).sendResponse(response);
                        return;
                    }
                }
                roiContext.render(param);
                mServer.offerMedia(segment, roiContext.getRoi());

                setResult(response, DConnectMessage.RESULT_OK);
                ((ThetaDeviceService) getContext()).sendResponse(response);
            }
        }).start();
        return false;
    }

    @Override
    public void onUpdate(final String segment, final byte[] roi) {
        mServer.offerMedia(segment, roi);
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
//        if (event.sensor.getType() != Sensor.TYPE_GYROSCOPE) {
//            return;
//        }
//
//        synchronized (this) {
//            if (mSensorEventTimestamp != 0) {
//                long interval = event.timestamp - mSensorEventTimestamp;
//                if (interval >= ROI_DELIVERY_INTERVAL) {
//                    float[] delta = new float[3];
//                    delta[0] = mRotationDelta[2] * 20;
//                    delta[1] = mRotationDelta[1] * 20;
//                    delta[2] = mRotationDelta[0] * 20;
//
//                    for (Map.Entry<String, RoiDeliveryContext> entry : mRoiContexts.entrySet()) {
//                        RoiDeliveryContext roiContext = entry.getValue();
//                        if (roiContext.getCurrentParam().isVrMode()) {
//                            changeDirection(delta, roiContext);
//                        }
//                    }
//
//                    mRotationDelta = new float[3];
//                    mSensorEventTimestamp = event.timestamp;
//                }
//            } else {
//                mSensorEventTimestamp = event.timestamp;
//            }
//            mRotationDelta[0] += event.values[0];
//            mRotationDelta[1] += event.values[1];
//            mRotationDelta[2] += event.values[2];
//
//        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
        // Nothing to do.
    }

    private void changeDirection(final float[] delta, final RoiDeliveryContext roiContext) {
        RoiDeliveryContext.Param param = roiContext.getCurrentParam();
        param.addCameraRoll(delta[0]);
        param.addCameraPitch(delta[1]);
        param.addCameraYaw(delta[2]);
        roiContext.render(param);

        String segment = roiContext.getUri().getLastPathSegment();
        mServer.offerMedia(segment, roiContext.getRoi());
    }

    private boolean startVrMode(final RoiDeliveryContext roiContext) {
        SensorManager sensorMgr = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (sensors.size() > 0) {
            Sensor sensor = sensors.get(0);
            sensorMgr.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            return true;
        } else {
            return false;
        }
    }

    private RoiDeliveryContext.Param parseParam(final Intent request) {
        Double x = getX(request);
        Double y = getY(request);
        Double z = getZ(request);
        Double roll = getRoll(request);
        Double pitch = getPitch(request);
        Double yaw = getYaw(request);
        Double fov = getFOV(request);
        Double sphereSize = getSphereSize(request);
        Integer width = getWidth(request);
        Integer height = getHeight(request);
        Boolean stereo = getStereo(request);
        Boolean vr = getVR(request);

        RoiDeliveryContext.Param param = new RoiDeliveryContext.Param();
        if (vr != null) {
            param.setVrMode(vr);
        }
        if (x != null) {
            param.setCameraX(x);
        }
        if (y != null) {
            param.setCameraY(y);
        }
        if (z != null) {
            param.setCameraZ(z);
        }
        if (roll != null && !param.isVrMode()) {
            param.setCameraRoll(roll);
        }
        if (pitch != null && !param.isVrMode()) {
            param.setCameraPitch(pitch);
        }
        if (yaw != null && !param.isVrMode()) {
            param.setCameraYaw(yaw);
        }
        if (fov != null) {
            param.setCameraFov(fov);
        }
        if (sphereSize != null) {
            param.setSphereSize(sphereSize);
        }
        if (width != null) {
            param.setImageWidth(width);
        }
        if (height != null) {
            param.setImageHeight(height);
        }
        if (stereo != null) {
            param.setStereoMode(stereo);
        }
        return param;
    }
}
