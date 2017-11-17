package org.deviceconnect.android.profile.spec.parser;


import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SwaggerJsonParserFactoryTest {

    @Test
    public void testCreateParser() {
        DConnectProfileSpecJsonParser parser = new SwaggerJsonParserFactory().createParser();
        assertNotNull(parser);
        assertTrue(parser instanceof SwaggerJsonParser);
    }

}
