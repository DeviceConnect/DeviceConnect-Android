package org.deviceconnect.android.profile.spec.parser;


public abstract class DConnectProfileSpecJsonParserFactory {

    abstract public DConnectProfileSpecJsonParser createParser();

    public static DConnectProfileSpecJsonParserFactory getDefaultFactory() {
        return new SwaggerJsonParserFactory();
    }

}
