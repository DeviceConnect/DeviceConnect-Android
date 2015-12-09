/*
 ThetaOmnidirectionalImageProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.theta.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.deviceconnect.android.deviceplugin.theta.ThetaDeviceService;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewParam;
import org.deviceconnect.android.deviceplugin.theta.core.SphericalViewRenderer;
import org.deviceconnect.android.deviceplugin.theta.core.sensor.HeadTracker;
import org.deviceconnect.android.deviceplugin.theta.utils.MixedReplaceMediaServer;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.OmnidirectionalImageProfile;
import org.deviceconnect.message.DConnectMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Theta Omnidirectional Image Profile.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaOmnidirectionalImageProfile extends OmnidirectionalImageProfile
    implements MixedReplaceMediaServer.ServerEventListener {

    /**
     * The service ID of ROI Image Service.
     */
    public static final String SERVICE_ID = "roi";

    /**
     * The name of ROI Image Service.
     */
    public static final String SERVICE_NAME = "ROI Image Service";

    private static final List<ParamDefinition> ROI_PARAM_DEFINITIONS;

    private final Object mLockObj = new Object();

    private MixedReplaceMediaServer mServer;

    private Map<String, Viewer> mViewers = new HashMap<String, Viewer>();

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final HeadTracker mHeadTracker;

    static {
        List<ParamDefinition> def = new ArrayList<ParamDefinition>();
        def.add(new DoubleParamDefinition(PARAM_X, null));
        def.add(new DoubleParamDefinition(PARAM_Y, null));
        def.add(new DoubleParamDefinition(PARAM_Z, null));
        def.add(new DoubleParamDefinition(PARAM_ROLL, new DoubleParamRange() {
            @Override
            public boolean validate(final double v) {
                return 0.0 <= v && v < 360.0;
            }
        }));
        def.add(new DoubleParamDefinition(PARAM_YAW, new DoubleParamRange() {
            @Override
            public boolean validate(final double v) {
                return 0.0 <= v && v < 360.0;
            }
        }));
        def.add(new DoubleParamDefinition(PARAM_PITCH, new DoubleParamRange() {
            @Override
            public boolean validate(final double v) {
                return 0.0 <= v && v < 360.0;
            }
        }));
        def.add(new DoubleParamDefinition(PARAM_FOV, new DoubleParamRange() {
            @Override
            public boolean validate(final double v) {
                return 0.0 < v && v < 180.0;
            }
        }));
        def.add(new DoubleParamDefinition(PARAM_SPHERE_SIZE, new DoubleParamRange() {
            @Override
            public boolean validate(final double v) {
                return SphericalViewRenderer.Z_NEAR < v && v < SphericalViewRenderer.Z_FAR;
            }
        }));
        def.add(new DoubleParamDefinition(PARAM_WIDTH, new DoubleParamRange() {
            @Override
            public boolean validate(final double v) {
                return 0.0 < v;
            }
        }));
        def.add(new DoubleParamDefinition(PARAM_HEIGHT, new DoubleParamRange() {
            @Override
            public boolean validate(final double v) {
                return 0.0 < v;
            }
        }));
        def.add(new BooleanParamDefinition(PARAM_STEREO));
        def.add(new BooleanParamDefinition(PARAM_VR));
        ROI_PARAM_DEFINITIONS = def;
    }

    public ThetaOmnidirectionalImageProfile(final HeadTracker tracker) {
        mHeadTracker = tracker;
    }

    @Override
    protected boolean onGetView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        requestView(request, response, serviceId, source, true);
        return false;
    }

    @Override
    protected boolean onPutView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        requestView(request, response, serviceId, source, false);
        return false;
    }

    private String startMediaServer() {
        synchronized (mLockObj) {
            if (mServer == null) {
                mServer = new MixedReplaceMediaServer();
                mServer.setServerName("ThetaDevicePlugin Server");
                mServer.setContentType("image/jpeg");
                mServer.setServerEventListener(ThetaOmnidirectionalImageProfile.this);
                mServer.start();
            }
        }
        return mServer.getUrl();
    }

    private void requestView(final Intent request, final Intent response, final String serviceId,
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
                    final String serverUri = startMediaServer();
                    final String id = generateId();
                    final String resourceUri = serverUri + "/" + id;
                    final String[] outputs = parseOutputs(getOutput(request));

                    Projector projector;
                    if (isRequiredOverlay(outputs)) {
                        projector = new OverlayProjector(getContext());
                    } else if (isRequiredMJPEG(outputs)) {
                        projector = new DefaultProjector();
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response);
                        ((ThetaDeviceService) getContext()).sendResponse(response);
                        return;
                    }

                    SphericalViewRenderer renderer = new SphericalViewRenderer();
                    renderer.setFlipVertical(true);
                    renderer.setScreenSizeMutable(true);
                    renderer.setScreenSettings(600, 400, false);
                    projector.setRenderer(renderer);
                    if (isRequiredMJPEG(outputs)) {
                        projector.setScreen(new ProjectionScreen() {
                            @Override
                            public void onStart(final Projector projector) {
                            }

                            @Override
                            public void onProjected(final Projector projector, byte[] frame) {
                                mServer.offerMedia(id, frame);
                            }

                            @Override
                            public void onStop(final Projector projector) {
                            }
                        });
                    }

                    ImageViewer viewer = new ImageViewer(getContext());
                    viewer.setHeadTracker(mHeadTracker);
                    viewer.setImage(source);
                    viewer.setProjector(projector);
                    viewer.start();
                    mViewers.put(resourceUri, viewer);

                    setResult(response, DConnectMessage.RESULT_OK);
                    if (isGet) {
                        setURI(response, resourceUri + "?snapshot");
                    } else {
                        setURI(response, resourceUri);
                    }
                } catch (MalformedURLException e) {
                    MessageUtils.setInvalidRequestParameterError(response, "uri is malformed: " + source);
                } catch (FileNotFoundException e) {
                    MessageUtils.setInvalidRequestParameterError(response, "Image is not found: " + source);
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

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private String[] parseOutputs(final String outputParam) {
        if (outputParam == null) {
            return new String[]{"overlay", "mjpeg"};
        }
        return outputParam.split(",");
    }

    private boolean isRequiredOverlay(final String[] requiredOutputs) {
        for (String output : requiredOutputs) {
            if ("overlay".equals(output)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRequiredMJPEG(final String[] requiredOutputs) {
        for (String output : requiredOutputs) {
            if ("mjpeg".equals(output)) {
                return true;
            }
        }
        return false;
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
        Viewer viewer = mViewers.remove(omitParameters(uri));
        if (viewer != null) {
            viewer.stop();
            mServer.stopMedia(viewer.getId());
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onPutSettings(final Intent request, final Intent response, final String serviceId,
                                    final String uri) {
        if (!checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        if (uri == null) {
            MessageUtils.setInvalidRequestParameterError(response, "uri is not specified.");
            return true;
        }
        if (!validateRequest(request, response)) {
            return true;
        }
        final Viewer viewer = mViewers.get(omitParameters(uri));
        if (viewer == null) {
            MessageUtils.setInvalidRequestParameterError(response, "The specified media is not found.");
            return true;
        }
        viewer.setParameter(parseParam(request));
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    public byte[] onConnect(final MixedReplaceMediaServer.Request request) {
        return null;
    }

    @Override
    public void onDisconnect(final MixedReplaceMediaServer.Request request) {
        if (!request.isGet()) {
            Viewer viewer = mViewers.remove(request.getUri());
            if (viewer != null) {
                viewer.stop();
            }
        }
    }

    @Override
    public void onCloseServer() {
        mViewers.clear();
    }

    private boolean checkServiceId(final String serviceId) {
        if (TextUtils.isEmpty(serviceId)) {
            return false;
        }
        return serviceId.equals(SERVICE_ID);
    }

    private String omitParameters(final String uri) {
        if (uri == null) {
            return null;
        }
        int index = uri.indexOf("?");
        if (index >= 0) {
            return uri.substring(0, index);
        }
        return uri;
    }

    private SphericalViewParam parseParam(final Intent request) {
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

        SphericalViewParam param = new SphericalViewParam();
        if (x != null) {
            param.setCameraX(x);
        }
        if (y != null) {
            param.setCameraY(y);
        }
        if (z != null) {
            param.setCameraZ(z);
        }
        if (roll != null && !param.isVRMode()) {
            param.setCameraRoll(roll);
        }
        if (pitch != null && !param.isVRMode()) {
            param.setCameraPitch(pitch);
        }
        if (yaw != null && !param.isVRMode()) {
            param.setCameraYaw(yaw);
        }
        if (fov != null) {
            param.setFOV(fov);
        }
        if (sphereSize != null) {
            param.setSphereSize(sphereSize);
        }
        if (width != null) {
            param.setWidth(width);
        }
        if (height != null) {
            param.setHeight(height);
        }
        if (stereo != null) {
            param.setStereo(stereo);
        }
        if (vr != null) {
            param.setVRMode(vr);
        }
        return param;
    }

    private static boolean validateRequest(final Intent request, final Intent response) {
        Bundle extras = request.getExtras();
        if (extras == null) {
            MessageUtils.setUnknownError(response, "request has no parameter.");
            return false;
        }
        for (ParamDefinition definition : ROI_PARAM_DEFINITIONS) {
            if (!definition.validate(extras, response)) {
                return false;
            }
        }
        return true;
    }

    private static abstract class ParamDefinition {

        protected final String mName;

        protected final boolean mIsOptional;

        protected ParamDefinition(final String name, final boolean isOptional) {
            mName = name;
            mIsOptional = isOptional;
        }

        public abstract boolean validate(final Bundle extras, final Intent response);
    }

    private static class BooleanParamDefinition extends ParamDefinition {

        private static final String TRUE = "true";
        private static final String FALSE = "false";

        public BooleanParamDefinition(final String name, final boolean isOptional) {
            super(name, isOptional);
        }

        public BooleanParamDefinition(final String name) {
            this(name, true);
        }

        @Override
        public boolean validate(final Bundle extras, final Intent response) {
            Object value = extras.get(mName);
            if (value == null) {
                if (mIsOptional) {
                    return true;
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, mName + " is not specified.");
                    return false;
                }
            }
            if (value instanceof Boolean) {
                return true;
            } else if (value instanceof String) {
                String stringValue = (String) value;
                if (!TRUE.equals(stringValue) && !FALSE.equals(stringValue)) {
                    MessageUtils.setInvalidRequestParameterError(response, "Format of " + mName + " is invalid.");
                    return false;
                }
                try {
                    Boolean.parseBoolean(stringValue);
                    return true;
                } catch (NumberFormatException e) {
                    // Nothing to do.
                }
            }
            MessageUtils.setInvalidRequestParameterError(response, "Format of " + mName + " is invalid.");
            return false;
        }
    }

    private static class DoubleParamDefinition extends ParamDefinition {

        private DoubleParamRange mRange;

        public DoubleParamDefinition(final String name, final boolean isOptional,
                                     final DoubleParamRange range) {
            super(name, isOptional);
            mRange = range;
        }

        public DoubleParamDefinition(final String name, final DoubleParamRange range) {
            this(name, true, range);
        }

        @Override
        public boolean validate(final Bundle extras, final Intent response) {
            Object value = extras.get(mName);
            if (value == null) {
                if (mIsOptional) {
                    return true;
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, mName + " is not specified.");
                    return false;
                }
            }
            if (value instanceof Double) {
                if (validateRange((Double) value)) {
                    return true;
                } else {
                    MessageUtils.setInvalidRequestParameterError(response, mName + " is out of range.");
                    return false;
                }
            } else if (value instanceof String) {
                try {
                    double doubleValue = Double.parseDouble((String) value);
                    if (validateRange(doubleValue)) {
                        return true;
                    } else {
                        MessageUtils.setInvalidRequestParameterError(response, mName + " is out of range.");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // Nothing to do.
                }
            }
            MessageUtils.setInvalidRequestParameterError(response, "Format of " + mName + " is invalid.");
            return false;
        }

        private boolean validateRange(double value) {
            return mRange == null || mRange.validate(value);
        }
    }

    private interface DoubleParamRange {
        boolean validate(double value);
    }
}
