package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class BooleanParameterSpecTest {

    @Test
    public void testValidate_Mandatory_Default() {
        BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
        builder.setRequired(true);
        BooleanParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(true), is(equalTo(true)));
    }

    @Test
    public void testValidate_Optional_Default() {
        BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
        builder.setRequired(false);
        BooleanParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Defined() {
        BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Boolean[] {true});
        BooleanParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(true), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Undefined() {
        BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Boolean[] {true});
        BooleanParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(false), is(equalTo(false)));
    }

    @Test
    public void testValidate_Optional_Enum_Null() {
        BooleanParameterSpec.Builder builder = new BooleanParameterSpec.Builder();
        builder.setRequired(false);
        builder.setEnum(new Boolean[] {true});
        BooleanParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }
}
