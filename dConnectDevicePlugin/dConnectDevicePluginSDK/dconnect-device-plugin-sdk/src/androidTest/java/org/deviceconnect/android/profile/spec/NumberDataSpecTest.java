package org.deviceconnect.android.profile.spec;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class NumberDataSpecTest {

    @Test
    public void testValidate_Default() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Enum_Defined() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setEnum(new Double[] {0.5d});
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(0.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Enum_Undefined() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setEnum(new Double[] {0.5d});
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(1.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Inclusive_Max_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMaximum(10.5d);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Inclusive_Max_TooLarge() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMaximum(10.5d);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMaximum(10.5d);
        builder.setExclusiveMaximum(true);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_TooLarge() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMaximum(10.5d);
        builder.setExclusiveMaximum(true);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Inclusive_Min_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Inclusive_Min_TooSmall() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(9L), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Exclusive_Min_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        builder.setExclusiveMinimum(true);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(11.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Min_TooSmall() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        builder.setExclusiveMinimum(true);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(10.5d), is(equalTo(false)));
    }

    @Test
    public void testValidate_Range_Inclusive_Max_Inclusive_Min_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Inclusive_Max_Exclusive_Min_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        builder.setExclusiveMinimum(true);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_Inclusive_Min_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        builder.setExclusiveMaximum(true);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }

    @Test
    public void testValidate_Range_Exclusive_Max_Exclusive_Min_OK() {
        NumberDataSpec.Builder builder = new NumberDataSpec.Builder();
        builder.setMinimum(10.5d);
        builder.setMinimum(1.5d);
        builder.setExclusiveMaximum(true);
        builder.setExclusiveMinimum(true);
        NumberDataSpec dataSpec = builder.build();

        assertThat(dataSpec.validate(5.5d), is(equalTo(true)));
    }
}
