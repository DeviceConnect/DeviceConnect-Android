package org.deviceconnect.android.profile.spec.parser;


import android.support.test.runner.AndroidJUnit4;

import org.deviceconnect.android.profile.spec.BooleanParameterSpec;
import org.deviceconnect.android.profile.spec.DConnectApiSpec;
import org.deviceconnect.android.profile.spec.DConnectParameterSpec;
import org.deviceconnect.android.profile.spec.DConnectProfileSpec;
import org.deviceconnect.android.profile.spec.DConnectSpecConstants;
import org.deviceconnect.android.profile.spec.IntegerParameterSpec;
import org.deviceconnect.android.profile.spec.NumberParameterSpec;
import org.deviceconnect.android.profile.spec.StringParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SwaggerJsonParserTest implements DConnectSpecConstants {

    @Test
    public void testParseJson_MinimumJson() throws Exception {
        SwaggerJsonParser parser = createParser();
        DConnectProfileSpec spec = parser.parseJson(loadJson("parser/testMinimumJson.json"));

        assertThat(spec.getApiName(), is(equalTo("myApi")));
        assertThat(spec.getProfileName(), is(equalTo("testMinimumJson")));

        List<DConnectApiSpec> specList = spec.getApiSpecList();
        assertThat(specList, is(notNullValue()));
        assertThat(specList.size(), is(equalTo(0)));
    }

    @Test
    public void testParseJson_Parameters() throws Exception {
        SwaggerJsonParser parser = createParser();
        DConnectProfileSpec spec = parser.parseJson(loadJson("parser/testParameterSpec.json"));

        List<DConnectApiSpec> specList = spec.getApiSpecList();
        assertThat(specList, is(notNullValue()));
        assertThat(specList.size(), is(equalTo(1)));
        DConnectApiSpec apiSpec = specList.get(0);
        DConnectParameterSpec[] paramSpecList = apiSpec.getRequestParamList();
        Map<String, DConnectParameterSpec> paramSpecMap = new HashMap<>();
        for (DConnectParameterSpec paramSpec : paramSpecList) {
            paramSpecMap.put(paramSpec.getName(), paramSpec);
        }

        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("booleanParam");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.BOOLEAN)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("booleanParamWithEnum");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.BOOLEAN)));
            BooleanParameterSpec booleanDataSpec = (BooleanParameterSpec) paramSpec;
            Boolean[] enumList = booleanDataSpec.getEnum();
            assertThat(enumList, is(notNullValue()));
            assertArrayEquals(enumList, new Boolean[] {true, false});
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("integerParam");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.INTEGER)));
            IntegerParameterSpec integerDataSpec = (IntegerParameterSpec) paramSpec;
            assertThat(integerDataSpec.getFormat(), is(equalTo(DataFormat.INT32)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("integerParamWithEnum");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.INTEGER)));
            IntegerParameterSpec integerDataSpec = (IntegerParameterSpec) paramSpec;
            Long[] enumList = integerDataSpec.getEnum();
            assertArrayEquals(enumList, new Long[] {0L, 1L});
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("integerParamWithRange");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.INTEGER)));
            IntegerParameterSpec integerDataSpec = (IntegerParameterSpec) paramSpec;
            assertThat(integerDataSpec.getMaximum(), is(equalTo(1L)));
            assertThat(integerDataSpec.getMinimum(), is(equalTo(0L)));
            assertThat(integerDataSpec.isExclusiveMaximum(), is(equalTo(true)));
            assertThat(integerDataSpec.isExclusiveMinimum(), is(equalTo(true)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("integerParamFormatInt32");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.INTEGER)));
            IntegerParameterSpec integerDataSpec = (IntegerParameterSpec) paramSpec;
            assertThat(integerDataSpec.getFormat(), is(equalTo(DataFormat.INT32)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("integerParamFormatInt64");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.INTEGER)));
            IntegerParameterSpec integerDataSpec = (IntegerParameterSpec) paramSpec;
            assertThat(integerDataSpec.getFormat(), is(equalTo(DataFormat.INT64)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("numberParam");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.NUMBER)));
            NumberParameterSpec numberParameterSpec = (NumberParameterSpec) paramSpec;
            assertThat(numberParameterSpec.getFormat(), is(equalTo(DataFormat.FLOAT)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("numberParamWithEnum");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.NUMBER)));
            NumberParameterSpec numberParameterSpec = (NumberParameterSpec) paramSpec;
            Double[] enumList = numberParameterSpec.getEnum();
            assertArrayEquals(enumList, new Double[] {0.5d, 1.5d});
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("numberParamWithRange");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.NUMBER)));
            NumberParameterSpec numberParameterSpec = (NumberParameterSpec) paramSpec;
            assertThat(numberParameterSpec.getMaximum(), is(equalTo(1.5d)));
            assertThat(numberParameterSpec.getMinimum(), is(equalTo(0.5d)));
            assertThat(numberParameterSpec.isExclusiveMaximum(), is(equalTo(true)));
            assertThat(numberParameterSpec.isExclusiveMinimum(), is(equalTo(true)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("numberParamFormatFloat");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.NUMBER)));
            NumberParameterSpec numberParameterSpec = (NumberParameterSpec) paramSpec;
            assertThat(numberParameterSpec.getFormat(), is(equalTo(DataFormat.FLOAT)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("numberParamFormatDouble");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.NUMBER)));
            NumberParameterSpec numberParameterSpec = (NumberParameterSpec) paramSpec;
            assertThat(numberParameterSpec.getFormat(), is(equalTo(DataFormat.DOUBLE)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParam");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamWithEnum");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
            String[] enumList = stringDataSpec.getEnum();
            assertArrayEquals(enumList, new String[] {"a", "b", "c"});
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamWithRange");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
            assertThat(stringDataSpec.getMaxLength(), is(equalTo(1)));
            assertThat(stringDataSpec.getMinLength(), is(equalTo(0)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamFormatText");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.TEXT)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamFormatByte");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.BYTE)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamFormatBinary");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.BINARY)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamFormatDate");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.DATE)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamFormatDateTime");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.DATE_TIME)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamFormatPassword");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.PASSWORD)));
        }
        {
            DConnectParameterSpec paramSpec = paramSpecMap.get("stringParamFormatRGB");
            assertThat(paramSpec.getDataType(), is(equalTo(DataType.STRING)));
            StringParameterSpec stringDataSpec = (StringParameterSpec) paramSpec;
            assertThat(stringDataSpec.getFormat(), is(equalTo(DataFormat.RGB)));
        }
    }

    private SwaggerJsonParser createParser() {
        return new SwaggerJsonParser();
    }

    private JSONObject loadJson(final String jsonFileName) throws IOException, JSONException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFileName);
        if (is == null) {
            throw new IOException("Failed to get resource: " + jsonFileName);
        }
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[1024];
        while ((len = is.read(buf)) > 0) {
            data.write(buf, 0, len);
        }
        return new JSONObject(new String(data.toByteArray(), "UTF-8"));
    }
}
