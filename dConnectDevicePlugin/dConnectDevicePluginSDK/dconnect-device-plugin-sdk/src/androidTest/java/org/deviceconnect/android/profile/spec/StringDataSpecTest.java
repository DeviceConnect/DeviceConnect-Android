package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StringDataSpecTest {

    @Test
    public void testValidate_Default() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("a"), is(equalTo(true)));
    }

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

    @Test
    public void testValidate_Length_Max_OK() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMaxLength(5);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Length_Max_TooLong() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMaxLength(5);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345x"), is(equalTo(false)));
    }

    @Test
    public void testValidate_Length_Min_OK() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMinLength(5);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Length_Min_TooShort() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMinLength(5);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("1234"), is(equalTo(false)));
    }

    @Test
    public void testValidate_Length_MaxMin_OK() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMaxLength(5);
        builder.setMinLength(1);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Length_MaxMin_TooLong() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMaxLength(5);
        builder.setMinLength(1);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345x"), is(equalTo(false)));
    }

    @Test
    public void testValidate_Length_MaxMin_TooShort() {
        StringDataSpec.Builder builder = new StringDataSpec.Builder();
        builder.setMaxLength(5);
        builder.setMinLength(1);
        StringDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(""), is(equalTo(false)));
    }
}
