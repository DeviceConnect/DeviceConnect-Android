package org.deviceconnect.android.deviceplugin.test.profile.unique;

import android.content.Intent;

import org.deviceconnect.android.deviceplugin.test.profile.TestServiceDiscoveryProfile;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;


public class TestOmnidirectionalImageProfile extends DConnectProfile {

    /**
     * Profile name: {@value} .
     */
    public static final String PROFILE_NAME = "omnidirectionalImage";

    /**
     * Interface name: {@value} .
     */
    public static final String INTERFACE_ROI = "roi";

    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_ROI = "roi";

    /**
     * Attribute: {@value} .
     */
    public static final String ATTRIBUTE_SETTINGS = "settings";

    /**
     * Parameter: {@value} .
     */
    public static final String PARAM_SOURCE = "source";


    @Override
    public String getProfileName() {
        return PROFILE_NAME;
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        String interfaceName = getInterface(request);
        String attributeName = getAttribute(request);
        if (interfaceName == null && ATTRIBUTE_ROI.equals(attributeName)) {
            return onGetView(request, response, getServiceID(request), getSource(request));
        }
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    @Override
    protected boolean onPutRequest(final Intent request, final Intent response) {
        String interfaceName = getInterface(request);
        String attributeName = getAttribute(request);
        if (interfaceName == null && ATTRIBUTE_ROI.equals(attributeName)) {
            return onPutView(request, response, getServiceID(request), getSource(request));
        } else if (INTERFACE_ROI.equals(interfaceName) && ATTRIBUTE_SETTINGS.equals(attributeName)) {
            return onPutSettings(request, response, getServiceID(request), getURI(request));
        }
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    @Override
    protected boolean onDeleteRequest(final Intent request, final Intent response) {
        String interfaceName = getInterface(request);
        String attributeName = getAttribute(request);
        if (interfaceName == null && ATTRIBUTE_ROI.equals(attributeName)) {
            return onDeleteView(request, response, getServiceID(request), getURI(request));
        }
        MessageUtils.setUnknownAttributeError(response);
        return true;
    }

    protected boolean onGetView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        if (!checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    protected boolean onPutView(final Intent request, final Intent response, final String serviceId,
                                final String source) {
        if (!checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return false;
    }

    protected boolean onDeleteView(final Intent request, final Intent response, final String serviceId,
                                   final String uri) {
        if (!checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    protected boolean onPutSettings(final Intent request, final Intent response, final String serviceId,
                                    final String uri) {
        if (!checkServiceId(serviceId)) {
            MessageUtils.setNotFoundServiceError(response);
            return true;
        }
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    private boolean checkServiceId(final String serviceId) {
        return TestServiceDiscoveryProfile.SERVICE_ID.equals(serviceId);
    }

    public static String getSource(final Intent request) {
        return request.getStringExtra(PARAM_SOURCE);
    }

    public static String getURI(final Intent request) {
        return request.getStringExtra(PARAM_URI);
    }

    public static void setURI(final Intent response, final String uri) {
        response.putExtra(PARAM_URI, uri);
    }
}
