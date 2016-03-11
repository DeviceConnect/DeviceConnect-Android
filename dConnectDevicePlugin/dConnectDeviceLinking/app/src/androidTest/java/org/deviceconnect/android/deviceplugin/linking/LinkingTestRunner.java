/*
 LinkingTestRunner.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;

public class LinkingTestRunner extends AndroidJUnitRunner {

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return Instrumentation.newApplication(LinkingApplicationForTest.class, context);
    }

}
