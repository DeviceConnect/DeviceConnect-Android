package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class IntegerParameterSpecTest {

    @Test
    public void testValidate_Mandatory_Enum_Defined() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Long[] {1L});
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(1L), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Undefined() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new Long[] {1L});
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Optional_Enum_Null() {
        IntegerParameterSpec.Builder builder = new IntegerParameterSpec.Builder();
        builder.setRequired(false);
        builder.setEnum(new Long[] {1L});
        IntegerParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }
}
