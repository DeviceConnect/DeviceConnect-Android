package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class StringParameterSpecTest {

    @Test
    public void testValidate_Mandatory_Enum_Defined() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new String[] {"a", "b", "c"});
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("a"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Enum_Undefined() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setEnum(new String[] {"a", "b", "c"});
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("z"), is(equalTo(false)));
    }

    @Test
    public void testValidate_Optional_Enum_Null() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(false);
        builder.setEnum(new String[] {"a", "b", "c"});
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }
}
