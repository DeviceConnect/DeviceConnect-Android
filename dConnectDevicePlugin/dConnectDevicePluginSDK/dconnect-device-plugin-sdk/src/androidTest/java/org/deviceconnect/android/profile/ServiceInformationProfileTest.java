package org.deviceconnect.android.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;

import org.deviceconnect.android.profile.api.DConnectApi;
import org.deviceconnect.android.profile.api.GetApi;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectPluginSpec;
import org.deviceconnect.android.service.DConnectService;
import org.deviceconnect.android.service.DConnectServiceManager;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;


@RunWith(AndroidJUnit4.class)
public class ServiceInformationProfileTest extends AndroidTestCase {

    @Test
    public void testOnRequest() throws Exception {
        final String profileName = "testProfile";
        final String specName = profileName + ".json";

        DConnectServiceManager serviceManager = new DConnectServiceManager();
        DConnectPluginSpec pluginSpec = new DConnectPluginSpec();
        pluginSpec.addProfileSpec(profileName.toLowerCase(), getResource(specName));
        serviceManager.setContext(InstrumentationRegistry.getTargetContext());
        serviceManager.setPluginSpec(pluginSpec);

        // サービスを作成
        for (int i = 0; i < 2; i++) {
            final String serviceId = "s" + i;
            final String attribute = "a" + i;
            DConnectService service = new DConnectService(serviceId);
            service.addProfile(new DConnectProfile() {
                {
                    addApi(new GetApi() {
                        @Override
                        public String getAttribute() {
                            return attribute;
                        }
                        @Override
                        public boolean onRequest(final Intent request, final Intent response) {
                            return true;
                        }
                    });
                }
                @Override
                public String getProfileName() {
                    return profileName;
                }
            });
            serviceManager.addService(service);
        }

        for (DConnectService service : serviceManager.getServiceList()) {
            DConnectProfile profile = service.getProfile("serviceInformation");

            Intent request = new Intent(IntentDConnectMessage.ACTION_GET);
            request.putExtra("profile", "serviceInformation");
            Intent response = new Intent();

            boolean isSync = profile.onRequest(request, response);
            assertThat(isSync, is(true));
            assertThat(response.getIntExtra("result", -1), is(0));
            Bundle supportApis = response.getBundleExtra("supportApis");
            assertThat(supportApis, is(notNullValue()));
            Bundle swagger = supportApis.getBundle(profileName);
            assertThat(swagger, is(notNullValue()));
            assertThat(swagger.getString("swagger"), is(notNullValue()));
            assertThat(swagger.getString("basePath"), is(notNullValue()));
            Bundle info = swagger.getBundle("info");
            assertThat(info, is(notNullValue()));
            assertThat(info.getString("title"), is(notNullValue()));
            assertThat(info.getString("version"), is(notNullValue()));
            assertThat(info.containsKey("description"), is(false));
            assertThat(swagger.getStringArray("consumes"), is(notNullValue()));
            Bundle paths = swagger.getBundle("paths");
            assertThat(paths, is(notNullValue()));
            for (DConnectApi api : service.getProfile(profileName).getApiList()) {
                DConnectApiSpec apiSpec = api.getApiSpec();
                assertThat("Definition for " + createApiTitle(api) + " is null.", apiSpec, is(notNullValue()));
                Bundle foundPath = null;
                for (String path : paths.keySet()) {
                    if (path.equalsIgnoreCase(createPath(apiSpec))) {
                        foundPath = paths.getBundle(path);
                    }
                }
                assertThat(foundPath, is(notNullValue()));
                Bundle op = foundPath.getBundle("get");
                assertThat(op, is(notNullValue()));
                assertThat(op.getString("x-type"), is(notNullValue()));
                assertThat(op.containsKey("summary"), is(false));
                assertThat(op.containsKey("description"), is(false));
                Bundle[] parameters = (Bundle[]) op.getParcelableArray("parameters");
                assertThat(parameters, is(notNullValue()));

                for (Bundle parameter : parameters) {
                    assertThat(parameter.getString("name"), is(notNullValue()));
                    assertThat(parameter.containsKey("description"), is(false));
                    assertThat(parameter.getString("in"), is(notNullValue()));
                    assertThat(parameter.getBoolean("required"), is(true));
                    assertThat(parameter.getString("type"), is(notNullValue()));
                }
                assertThat(op.containsKey("responses"), is(false));
            }
            assertThat(swagger.containsKey("definitions"), is(false));
        }
    }

    private String createApiTitle(final DConnectApi api) {
        return api.getMethod().name() + " " + createPath(api);
    }

    private String createPath(final DConnectApi api) {
        return createPath(api.getInterface(), api.getAttribute());
    }

    private String createPath(final DConnectApiSpec apiSpec) {
        return createPath(apiSpec.getInterfaceName(), apiSpec.getAttributeName());
    }

    private String createPath(final String interfaceName, final String attrName) {
        String path = "/";
        if (interfaceName != null) {
            path += interfaceName;
            path += "/";
        }
        if (attrName != null) {
            path += attrName;
        }
        return path;
    }

    private InputStream getResource(final String resName) {
        return getClass().getClassLoader().getResourceAsStream(resName);
    }
}
