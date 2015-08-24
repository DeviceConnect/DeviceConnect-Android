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
import android.text.TextUtils;
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
    implements RoiDeliveryContext.OnChangeListener, MixedReplaceMediaServer.ServerEventListener {

    public static final String SERVICE_ID = "roi";

    public static final String SERVICE_NAME = "ROI Image Service";

    private final Object lockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private Map<String, OmnidirectionalImage> mOmniImages =
        Collections.synchronizedMap(new HashMap<String, OmnidirectionalImage>());

    private Map<String, RoiDeliveryContext> mRoiContexts =
        Collections.synchronizedMap(new HashMap<String, RoiDeliveryContext>());

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected boolean onGetView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        requestView(response, serviceId, source, true);
        return false;
    }

    @Override
    protected boolean onPutView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        requestView(response, serviceId, source, false);
        return false;
    }

    private void requestView(final Intent response, final String serviceId,
                             final String source, final boolean isGet) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (!checkServiceId(serviceId)) {
                    MessageUtils.setNotFoundServiceError(response);
                    ((ThetaDeviceService) getContext()).sendResponse(response);
                    return;
                }
                try {
                    synchronized (lockObj) {
                        if (mServer == null) {
                            mServer = new MixedReplaceMediaServer();
                            mServer.setServerName("ThetaDevicePlugin Server");
                            mServer.setContentType("image/jpeg");
                            mServer.setServerEventListener(ThetaOmnidirectionalImageProfile.this);
                            mServer.start();
                        }
                    }

                    OmnidirectionalImage omniImage = mOmniImages.get(source);
                    if (omniImage == null) {
                        String origin = getContext().getPackageName();
                        omniImage = new OmnidirectionalImage(source, origin);
                        mOmniImages.put(source, omniImage);
                    }

                    RoiDeliveryContext roiContext = new RoiDeliveryContext(getContext(), omniImage);
                    String segment = UUID.randomUUID().toString();
                    String uri = mServer.getUrl() + "/" + segment;
                    roiContext.setUri(uri);
                    roiContext.setOnChangeListener(ThetaOmnidirectionalImageProfile.this);
                    roiContext.changeRendererParam(RoiDeliveryContext.DEFAULT_PARAM, true);
                    roiContext.renderWithBlocking();
                    roiContext.startExpireTimer();
                    mRoiContexts.put(uri, roiContext);

                    setResult(response, DConnectMessage.RESULT_OK);
                    if (isGet) {
                        setURI(response, uri + "?snapshot");
                    } else {
                        setURI(response, uri);
                    }
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
    }

    @Override
    protected boolean onDeleteView(final Intent request, final Intent response, final String serviceId,
                                   final String uri) {
        if (!checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        if (uri == null) {
            MessageUtils.setInvalidRequestParameterError(response, "uri is not specified.");
            return true;
        }
        RoiDeliveryContext roiContext = mRoiContexts.remove(omitParameters(uri));
        if (roiContext != null) {
            roiContext.destroy();
            mServer.stopMedia(roiContext.getSegment());
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutSettings(final Intent request, final Intent response, final String serviceId) {
        if (!checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        final String uri = omitParameters(getURI(request));
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
                byte[] roi = roiContext.renderWithBlocking();
                mServer.offerMedia(roiContext.getSegment(), roi);
            }
        });
        return true;
    }

    @Override
    public byte[] onConnect(final MixedReplaceMediaServer.Request request) {
        final String uri = request.getUri();
        final RoiDeliveryContext target = mRoiContexts.get(uri);
        if (target == null) {
            return null;
        }
        if (request.isGet()) {
            target.restartExpireTimer();
        } else {
            target.stopExpireTimer();
            target.startDeliveryTimer();
        }
        return target.getRoi();
    }

    @Override
    public void onDisconnect(final MixedReplaceMediaServer.Request request) {
        if (!request.isGet()) {
            RoiDeliveryContext roiContext = mRoiContexts.remove(request.getUri());
            if (roiContext != null) {
                roiContext.destroy();
            }
        }
    }

    @Override
    public void onCloseServer() {
        mRoiContexts.clear();
    }

    @Override
    public void onUpdate(final RoiDeliveryContext roiContext, final byte[] roi) {
        mServer.offerMedia(roiContext.getSegment(), roi);
    }

    @Override
    public void onExpire(final RoiDeliveryContext roiContext) {
        mServer.stopMedia(roiContext.getSegment());
        mRoiContexts.remove(roiContext.getUri());
        roiContext.destroy();
    }

    private boolean checkServiceId(final String serviceId) {
        if (TextUtils.isEmpty(serviceId)) {
            return false;
        }
        return serviceId.equals(SERVICE_ID);
    }

    private String omitParameters(final String uri) {
        int index = uri.indexOf("?");
        if (index >= 0) {
            return uri.substring(0, index);
        }
        return uri;
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
