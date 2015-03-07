package org.deviceconnect.android.deviceplugin.hvc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class HvcDebugUtils {
    
    public static void stackTraceLog(final String tag, final String title) {
        Log.d(tag, "trace: (" + title + ")");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            Log.d(tag, "-----------------------------");
            Log.d(tag, "ClassName  : " + stackTraceElement.getClassName());
            Log.d(tag, "FileName   : " + stackTraceElement.getFileName());
            Log.d(tag, "MethodName : " + stackTraceElement.getMethodName());
            Log.d(tag, "LineNumber : " + stackTraceElement.getLineNumber());
            Log.d(tag, "-----------------------------");
        }
    }
    
    public static void intentDump(final String tag, final Intent intent, final String title) {
        Log.d(tag, "trace: (" + title + ")");
        Bundle bundle = intent.getExtras();
        Log.d(tag, "-----------------------------");
        Log.d(tag, "action: " + intent.getAction());
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.d(tag, String.format("key:%s value:%s (%s)", key,  
                value.toString(), value.getClass().getName()));
        }        
        Log.d(tag, "-----------------------------");
        
        
    }
}
