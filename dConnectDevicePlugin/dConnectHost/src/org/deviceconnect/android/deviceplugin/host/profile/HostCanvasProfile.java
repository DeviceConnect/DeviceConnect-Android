/*
 HostFileProfile.java
 Copyright (c) 2014 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.deviceconnect.android.deviceplugin.host.BuildConfig;
import org.deviceconnect.android.deviceplugin.host.activity.CanvasProfileActivity;
import org.deviceconnect.android.deviceplugin.host.camera.CameraActivity;
import org.deviceconnect.android.deviceplugin.host.camera.CameraConst;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;
import org.deviceconnect.message.intent.message.IntentDConnectMessage;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * Canvas Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {

    /** Debug Tag. */
    private static final String TAG = "HOST";

    /** SimpleDataFormat. */
    private SimpleDateFormat mDataFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * コンストラクタ.
     */
    public HostCanvasProfile() {
        super();
    }

    @Override
    protected boolean onPostDrawImage(Intent request, Intent response,
    		String deviceId, String mimeType, byte[] data, double x, double y,
    		String mode) {

        if (data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "data is not specied to update a file.");
            return true;
        }

        if (deviceId == null) {
            MessageUtils.setEmptyDeviceIdError(response);
            return true;
        }
        
        /* start CanvasProfileActivity */
        Context context = getContext();
        Intent intent = new Intent();
        intent.setClass(context, CanvasProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    
    	return true;
    }
    
//    private boolean isExistActivity(Class<CanvasProfileActivity> c) { 
//        ActivityInfo[] activities = null;
//        PackageManager.get
//        
//        this.get
//        PackageManager pm = getPackageManager();
//        try {
//            //当アプリの全アクティビティを取得
//            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
//            activities = packageInfo.activities;
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//        }    	
//	}
}
