package org.deviceconnect.android.deviceplugin.theta.utils;

import android.os.AsyncTask;

/**
 * Download the data of Theta.
 */
public class DownloadThetaDataTask extends AsyncTask<DownloadThetaDataTask.ThetaDownloadListener,
                                        Void, DownloadThetaDataTask.ThetaDownloadListener> {
    /** Download Listener. */
    public interface ThetaDownloadListener {
        /** Download data. */
        void doInBackground();
        /** UI Update. */
        void onPostExecute();
    }
    @Override
    protected ThetaDownloadListener doInBackground(ThetaDownloadListener... voids) {
        if (voids[0] != null) {
            voids[0].doInBackground();
        }
        return voids[0];
    }

    @Override
    protected void onPostExecute(final ThetaDownloadListener result) {
        super.onPostExecute(result);
        if (result != null) {
            result.onPostExecute();
        }
    }

}

