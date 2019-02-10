package org.deviceconnect.android.deviceplugin.host.demo;

import android.content.Context;
import android.os.Handler;

import java.io.IOException;

/**
 * ファイル操作タスク.
 */
public class FileTask implements Runnable {

    private final Context mContext;

    private final Handler mCallbackHandler;

    FileTask(final Context context, final Handler handler) {
        mContext = context;
        mCallbackHandler = handler;
    }

    Context getContext() {
        return mContext;
    }

    @Override
    public void run() {
        try {
            post(new Runnable() {
                @Override
                public void run() {
                    onBeforeTask();
                }
            });
            execute();
            post(new Runnable() {
                @Override
                public void run() {
                    onAfterTask();
                }
            });
        } catch (final IOException e) {
            post(new Runnable() {
                @Override
                public void run() {
                    onFileError(e);
                }
            });
        } catch (final Throwable e) {
            post(new Runnable() {
                @Override
                public void run() {
                    onUnexpectedError(e);
                }
            });
        }
    }

    private void post(final Runnable r) {
        mCallbackHandler.post(r);
    }

    protected void onBeforeTask() {}

    protected void execute() throws IOException {}

    protected void onAfterTask() {}

    protected void onFileError(final IOException e) {}

    protected void onUnexpectedError(final Throwable e) {}

}
