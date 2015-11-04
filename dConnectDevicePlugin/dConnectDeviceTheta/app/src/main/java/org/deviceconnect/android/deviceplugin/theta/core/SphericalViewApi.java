package org.deviceconnect.android.deviceplugin.theta.core;


/**
 * Spherical View API.
 *
 * <h2>Usage</h2>
 * <code>
 *     // Obtain Spherical View API.
 *     ThetaDeviceApplication app = (ThetaDeviceApplication) getApplication();
 *     SphericalViewApi api = app.getSphericalViewApi();
 *
 *     // Start Image View.
 *     api.startImageView(picture, param, renderer);
 *
 *     // Change Image View Settings
 *     api.updateImageView(newParam);
 *
 *     // Stop Image View.
 *     api.stopImageView();
 * </code>
 */
public class SphericalViewApi {

    public synchronized void startImageView(final String imageUri,
                                            final SphericalViewParam param,
                                            final SphericalViewRenderer renderer) {
        // TODO Implement.
    }

    public synchronized void startImageView(final ThetaObject picture,
                                            final SphericalViewParam param,
                                            final SphericalViewRenderer renderer) {
        // TODO Implement.
    }

    public synchronized void updateImageView(final SphericalViewParam param) {
        // TODO Implement.
    }

    public synchronized void stopImageView() {
        // TODO Implement.
    }

    public boolean isStarted() {
        // TODO Implement.
        return false;
    }

    public byte[] takeSnapshot() {
        return null;
    }

}
