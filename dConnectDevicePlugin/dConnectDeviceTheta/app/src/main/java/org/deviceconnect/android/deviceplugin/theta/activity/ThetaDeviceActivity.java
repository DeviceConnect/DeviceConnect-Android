package org.deviceconnect.android.deviceplugin.theta.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import org.deviceconnect.android.deviceplugin.theta.fragment.ThetaGalleryFragment;

/**
 * The Sample Application of THETA device plug-in.
 *
 * @author NTT DOCOMO, INC.
 */
public class ThetaDeviceActivity extends FragmentActivity {
    private static final String TAG_LIST = "list";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_LIST);
        if (fragment == null) {
            fragment = ThetaGalleryFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, fragment, TAG_LIST);
            ft.commit();
        }
    }

}
