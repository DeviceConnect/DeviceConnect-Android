package org.deviceconnect.android.profile.spec;


public interface DConnectApiSpecFilter extends DConnectApiSpecConstants {

    boolean filter(String path, Method method);

}
