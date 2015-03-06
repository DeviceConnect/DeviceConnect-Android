package org.deviceconnect.android.deviceplugin.hvc;

import android.util.Log;

public class HvcDebugUtils {
    
    public static void stackTraceLog(String title) {
        Log.d("AAA", "trace: (" + title + ")");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            Log.d("AAA", "-----------------------------");
            Log.d("AAA", "ClassName  : " + stackTraceElement.getClassName());
            Log.d("AAA", "FileName   : " + stackTraceElement.getFileName());
            Log.d("AAA", "MethodName : " + stackTraceElement.getMethodName());
            Log.d("AAA", "LineNumber : " + stackTraceElement.getLineNumber());
            Log.d("AAA", "-----------------------------");
        }
    }
}
