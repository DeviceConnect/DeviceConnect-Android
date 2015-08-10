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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Theta Omnidirectional Image Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaOmnidirectionalImageProfile extends OmnidirectionalImageProfile
    implements RoiDeliveryContext.OnChangeListener {

    private final Object lockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private Map<String, OmnidirectionalImage> mOmniImages =
        Collections.synchronizedMap(new HashMap<String, OmnidirectionalImage>());

    private Map<String, RoiDeliveryContext> mRoiContexts =
        Collections.synchronizedMap(new HashMap<String, RoiDeliveryContext>());

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected boolean onPutView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        mExecutor.execute(new Runnable() {
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
                                            @Override
                                            public void run() {
                                                mServer.offerMedia(key, target.getRoi());
                                            }
                                        }, 250, 1000);
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

                    OmnidirectionalImage omniImage = mOmniImages.get(source);
                    if (omniImage == null) {
                        String origin = getContext().getPackageName();
                        omniImage = new OmnidirectionalImage(source, origin);
                    }

                    RoiDeliveryContext roiContext = new RoiDeliveryContext(getContext(), omniImage);
                    String segment = UUID.randomUUID().toString();
                    String uri = mServer.getUrl() + "/" + segment;
                    roiContext.setUri(Uri.parse(uri));
                    roiContext.setOnChangeListener(ThetaOmnidirectionalImageProfile.this);
                    roiContext.changeRendererParam(RoiDeliveryContext.DEFAULT_PARAM, true);
                    roiContext.render(true);
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
        });
        return false;
    }

    @Override
    protected boolean onDeleteView(final Intent request, final Intent response, final String serviceId,
                                   final String uri) {
        RoiDeliveryContext roiContext = mRoiContexts.remove(uri);
        if (roiContext != null) {
            roiContext.destroy();
            mServer.stopMedia(roiContext.getSegment());
            setResult(response, DConnectMessage.RESULT_OK);
            return true;
        } else {
            MessageUtils.setInvalidRequestParameterError(response, "The specified media is not found.");
            return true;
        }
    }

    @Override
    protected boolean onPutSettings(final Intent request, final Intent response, final String serviceId) {
        final String uri = getURI(request);
        final RoiDeliveryContext roiContext = mRoiContexts.get(uri);
        if (roiContext == null) {
            MessageUtils.setInvalidRequestParameterError(response, "The specified media is not found.");
            return true;
        }
        setResult(response, DConnectMessage.RESULT_OK);

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                RoiDeliveryContext.Param param = parseParam(request);
                roiContext.changeRendererParam(param, true);
                roiContext.render(true);
                mServer.offerMedia(roiContext.getSegment(), roiContext.getRoi());
            }
        });
        return true;
    }

    @Override
    public void onUpdate(final String segment, final byte[] roi) {
        mServer.offerMedia(segment, roi);
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
