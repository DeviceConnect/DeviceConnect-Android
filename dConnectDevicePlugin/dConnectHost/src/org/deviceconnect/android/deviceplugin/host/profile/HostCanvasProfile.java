/*
 HostCanvasProfile.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */

package org.deviceconnect.android.deviceplugin.host.profile;

import org.deviceconnect.android.deviceplugin.host.activity.CanvasProfileActivity;
import org.deviceconnect.android.deviceplugin.host.canvas.CanvasDrawImageObject;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.CanvasProfile;
import org.deviceconnect.message.DConnectMessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


/**
 * Canvas Profile.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HostCanvasProfile extends CanvasProfile {

    /**
     * draw object for send to activity.
     */
    private CanvasDrawImageObject mSendDrawObject;
    
    /**
     * constructor.
     */
    public HostCanvasProfile() {
        super();
    }

    @Override
    protected boolean onPostDrawImage(final Intent request, final Intent response,
            final String deviceId, final String mimeType, final byte[] data, final double x, final double y,
            final String mode) {
        
        if (data == null) {
            MessageUtils.setInvalidRequestParameterError(response, "data is not specied to update a file.");
            return true;
        }

        if (deviceId == null) {
            MessageUtils.setEmptyServiceIdError(response);
            return true;
        }
        
        // convert mode (if null, invalid value)
        CanvasDrawImageObject.Mode enumMode = CanvasDrawImageObject.convertMode(mode);
        if (enumMode == null) {
            MessageUtils.setInvalidRequestParameterError(response);
            return true;
        }
        
        // initialize ready receive draw request receiver.
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                
                if (intent.getAction().equals(CanvasProfileActivity.ACTION_READY_RECEIVE_DRAW_REQUEST)) {
                    
                    // send draw request broadcast to CanvasProfileActivity.
                    Intent broadcastIntent = new Intent();
                    mSendDrawObject.setValueToIntent(broadcastIntent);
                    broadcastIntent.setAction(CanvasProfileActivity.ACTION_DRAW_TO_CANVAS);
                    getContext().sendBroadcast(broadcastIntent);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CanvasProfileActivity.ACTION_READY_RECEIVE_DRAW_REQUEST);
        getContext().registerReceiver(receiver, intentFilter);
        
        // storing parameter to draw object.
        mSendDrawObject = new CanvasDrawImageObject(data, enumMode, x, y);
        
        // start CanvasProfileActivity
        Context context = getContext();
        Intent intent = new Intent();
        intent.setClass(context, CanvasProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mSendDrawObject.setValueToIntent(intent);
        context.startActivity(intent);
        
        // return result.
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }
}
