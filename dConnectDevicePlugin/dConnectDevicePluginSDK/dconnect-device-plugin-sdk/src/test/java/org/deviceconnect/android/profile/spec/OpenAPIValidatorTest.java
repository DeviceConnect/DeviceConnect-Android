package org.deviceconnect.android.profile.spec;

import org.deviceconnect.android.PluginSDKTestRunner;
import org.deviceconnect.android.profile.spec.models.DataType;
import org.deviceconnect.android.profile.spec.models.parameters.AbstractParameter;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(PluginSDKTestRunner.class)
public class OpenAPIValidatorTest {

    @Test
    public void test() {
        AbstractParameter p = new AbstractParameter();

        p.setType(DataType.STRING);
        p.setMinLength(1);
        p.setMaxLength(10);

        boolean result = OpenAPIValidator.validate(p, "012345678");
        assertThat(result, is(true));
    }

    @Test
    public void testNumber() {
        AbstractParameter p = new AbstractParameter();

        p.setType(DataType.INTEGER);
        p.setMinimum(1);
        p.setMaximum(10);
        p.setExclusiveMaximum(false);

        boolean result = OpenAPIValidator.validate(p, 10);
        assertThat(result, is(true));
    }


    @Test
    public void testEnum() {
        List<Object> enums = new ArrayList<>();
        enums.add(1.0f);
        enums.add(2.0f);
        enums.add(3.0f);

        AbstractParameter p = new AbstractParameter();
        p.setType(DataType.NUMBER);
        p.setEnum(enums);

        boolean result = OpenAPIValidator.validate(p, 3f);
        assertThat(result, is(true));
    }
}
