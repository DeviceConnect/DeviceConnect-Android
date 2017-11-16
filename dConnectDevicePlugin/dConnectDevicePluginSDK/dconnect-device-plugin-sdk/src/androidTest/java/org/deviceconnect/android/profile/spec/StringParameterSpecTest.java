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
    public void testValidate_Mandatory_Default() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("a"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Optional_Default() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(false);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }

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

    @Test
    public void testValidate_Mandatory_Length_Max_OK() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaxLength(5);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Length_Max_TooLong() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaxLength(5);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345x"), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Length_Min_OK() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinLength(5);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Length_Min_TooShort() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMinLength(5);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("1234"), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Length_MaxMin_OK() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaxLength(5);
        builder.setMinLength(1);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345"), is(equalTo(true)));
    }

    @Test
    public void testValidate_Mandatory_Length_MaxMin_TooLong() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaxLength(5);
        builder.setMinLength(1);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate("12345x"), is(equalTo(false)));
    }

    @Test
    public void testValidate_Mandatory_Length_MaxMin_TooShort() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(true);
        builder.setMaxLength(5);
        builder.setMinLength(1);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(""), is(equalTo(false)));
    }

    @Test
    public void testValidate_Optional_Length_MaxMin_TooShort() {
        StringParameterSpec.Builder builder = new StringParameterSpec.Builder();
        builder.setRequired(false);
        builder.setMaxLength(5);
        builder.setMinLength(1);
        StringParameterSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(null), is(equalTo(true)));
    }
}
