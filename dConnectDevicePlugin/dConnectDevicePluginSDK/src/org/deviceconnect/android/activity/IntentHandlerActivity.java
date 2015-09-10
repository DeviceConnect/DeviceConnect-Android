package org.deviceconnect.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

/**
 * An Activity with interfaces to send an Intent and return the result.
 *
 * @author NTT DOCOMO, INC.
 */
public class IntentHandlerActivity extends Activity {

    private static final String EXTRA_INTENT_DATA = "EXTRA_INTENT_DATA";
    private static final String EXTRA_INTENT = "EXTRA_INTENT";
    private static final String EXTRA_CALLBACK = "EXTRA_CALLBACK";
    private static final int REQUEST_CODE = 123456789;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_transparent);

        if (savedInstanceState == null) {
            Intent callingIntent = getIntent();
            if (callingIntent == null || callingIntent.getExtras() == null || !callingIntent.hasExtra(EXTRA_INTENT)
                    || !callingIntent.hasExtra(EXTRA_CALLBACK)) {
                finish();
                return;
            }

            Bundle extras = callingIntent.getExtras();
            Intent intent = extras.getParcelable(EXTRA_INTENT);
            if (intent == null) {
                finish();
                return;
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            Intent callingIntent = getIntent();
            if (callingIntent == null || callingIntent.getExtras() == null || !callingIntent.hasExtra(EXTRA_CALLBACK)) {
                finish();
                return;
            }

            Bundle extras = callingIntent.getExtras();
            ResultReceiver callback = extras.getParcelable(EXTRA_CALLBACK);
            if (callback == null) {
                finish();
                return;
            }

            Bundle dataBundle = new Bundle();
            dataBundle.putParcelable(EXTRA_INTENT_DATA, data);
            callback.send(resultCode, dataBundle);
            finish();
        }
    }

    public static void startActivityForResult(@NonNull final Context context, @NonNull Intent intent,
            @NonNull ResultReceiver resultReceiver) {
        final Intent callIntent = new Intent(context, IntentHandlerActivity.class);
        callIntent.putExtra(EXTRA_INTENT, intent);
        callIntent.putExtra(EXTRA_CALLBACK, resultReceiver);
        // NOTE: FLAG_ACTIVITY_SINGLE_TASK causes Activity#onActivityResult()
        // being called prematurely. FLAG_ACTIVITY_SINGLE_TOP, on the other
        // hand, does not cause that.
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(callIntent);
    }
}
