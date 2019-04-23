package org.deviceconnect.android.profile.spec.parser;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.profile.spec.models.Swagger;
import org.deviceconnect.android.utils.FileLoader;
import org.deviceconnect.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PluginSDKTestRunner.class)
public class OpenAPIParserTest {

    @Test
    public void test() throws JSONException {
        String jsonString = new String(FileLoader.readFile("testProfile.json"));
        Swagger swagger = OpenAPIParser.parse(jsonString);

        JSONObject o = JSONUtils.convertBundleToJSON(swagger.toBundle());
        System.out.println();
        System.out.println();
        System.out.println(" " + o.toString(2));
        System.out.println();
        System.out.println();
    }

}
