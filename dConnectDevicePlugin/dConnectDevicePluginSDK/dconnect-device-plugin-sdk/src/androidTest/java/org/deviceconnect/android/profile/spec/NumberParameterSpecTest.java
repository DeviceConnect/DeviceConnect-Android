package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class NumberParameterSpecTest {

    @Test
    public void testValidate_Mandatory_Enum_Defined() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Double[] {0.5d});
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Undefined() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Double[] {0.5d});
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(-0.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Optional_Enum_Null() {
        NumberParameterSpec.Builder builder = new NumberParameterSpec.Builder();
        builder.setRequired(false);
        builder.setEnum(new Double[] {0.5d});
        NumberParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }
}
