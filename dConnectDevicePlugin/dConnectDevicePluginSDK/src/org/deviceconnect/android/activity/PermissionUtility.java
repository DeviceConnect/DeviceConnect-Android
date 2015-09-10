package org.deviceconnect.android.activity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

/**
 * A utility class to make a permission request.
 *
 * @author NTT DOCOMO, INC.
 */
public class PermissionUtility {

    public static void requestPermissions(@NonNull final Context context, @NonNull final Handler handler,
            @NonNull final String[] permissions, @NonNull final PermissionRequestCallback callback) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callback.onSuccess();
        } else {
            Set<String> mPermissionSet = new HashSet<>(Arrays.asList(permissions));

            if (mPermissionSet.size() == 0) {
                callback.onSuccess();
                return;
            }
            Set<String> tmp = new HashSet<>(mPermissionSet);
            for (final String permission : tmp) {
                if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
                    mPermissionSet.remove(permission);
                }
            }
            if (mPermissionSet.size() == 0) {
                callback.onSuccess();
            } else {
                String[] missingPermissions = mPermissionSet.toArray(new String[mPermissionSet.size()]);
                PermissionRequestActivity.requestPermissions(context, missingPermissions, new ResultReceiver(handler) {
                    @Override
                    protected void onReceiveResult(final int resultCode, final Bundle resultData) {
                        String[] retPermissions = resultData.getStringArray(PermissionRequestActivity.EXTRA_PERMISSIONS);
                        int[] retGrantResults = resultData.getIntArray(PermissionRequestActivity.EXTRA_GRANT_RESULTS);
                        if (retPermissions == null || retGrantResults == null) {
                            callback.onFail(null);
                            return;
                        }
                        for (int i = 0; i < retPermissions.length; ++i) {
                            if (retGrantResults[i] == PackageManager.PERMISSION_DENIED) {
                                callback.onFail(retPermissions[i]);
                                return;
                            }
                        }
                        callback.onSuccess();
                    }
                });
            }
        }
    }

    public interface PermissionRequestCallback {
        void onSuccess();

        void onFail(@NonNull String deniedPermission);
    }
}
