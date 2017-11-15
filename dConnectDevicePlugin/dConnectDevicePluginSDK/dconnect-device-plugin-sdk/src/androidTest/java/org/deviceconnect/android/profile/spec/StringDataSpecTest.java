package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StringDataSpecTest {

    @Test
    public void testValidate_Enum_Defined() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setEnum(new String[] {"a", "b", "c"});
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("a"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Enum_Undefined() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setEnum(new String[] {"a", "b", "c"});
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("z"), is(equalTo(false)));
    }
}
