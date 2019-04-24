package org.deviceconnect.android.profile.spec.parser;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.profile.spec.models.Info;
import org.deviceconnect.android.profile.spec.models.Path;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.profile.spec.models.parameters.Parameter;
import org.deviceconnect.android.utils.FileLoader;
import org.deviceconnect.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(PluginSDKTestRunner.class)
public class OpenAPIParserTest {

    @Test
    public void test() throws JSONException {
        String jsonString = FileLoader.readString("testProfile.json");

        Swagger swagger = OpenAPIParser.parse(jsonString);

        JSONObject o = JSONUtils.convertBundleToJSON(swagger.toBundle());
        System.out.println();
        System.out.println();
        System.out.println(" " + o.toString(2));
        System.out.println();
        System.out.println();

        assertThat(swagger, is(notNullValue()));
        assertThat(swagger.getSwagger(), is("2.0"));
        assertThat(swagger.getBasePath(), is("/gotapi/testProfile"));
        assertThat(swagger.getPaths(), is(notNullValue()));

        Info info = swagger.getInfo();
        assertThat(info.getTitle(), is("Test Profile"));
        assertThat(info.getVersion(), is("1.0"));
        assertThat(info.getDescription(), is("Test Description"));

        Path a0 = swagger.getPaths().getPath("/a0");
        assertThat(a0, is(notNullValue()));
        assertThat(a0.getGet().getSummary(), is("test1"));
        assertThat(a0.getGet().getDescription(), is("test1"));
        assertThat(a0.getGet().getOperationId(), is("a0Get"));

        List<Parameter> parameters = a0.getGet().getParameters();
        assertThat(parameters, is(notNullValue()));
        assertThat(parameters.get(0).getName(), is("serviceId"));
        assertThat(parameters.get(0).getDescription(), is("serviceId"));
        assertThat(parameters.get(0).getIn(), is("query"));
    }
}
