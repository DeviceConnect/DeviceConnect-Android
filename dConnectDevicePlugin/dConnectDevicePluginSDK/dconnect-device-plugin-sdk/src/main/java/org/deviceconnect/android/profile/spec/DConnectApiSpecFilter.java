package org.deviceconnect.android.profile.spec;


public interface DConnectApiSpecFilter extends DConnectSpecConstants {

    boolean filter(String path, Method method);

}
