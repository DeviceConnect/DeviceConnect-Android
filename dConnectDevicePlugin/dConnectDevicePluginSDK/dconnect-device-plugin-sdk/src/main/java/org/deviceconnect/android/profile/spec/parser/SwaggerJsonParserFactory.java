package org.deviceconnect.android.profile.spec.parser;


class SwaggerJsonParserFactory extends DConnectProfileSpecJsonParserFactory {

    @Override
    public DConnectProfileSpecJsonParser createParser() {
        return new SwaggerJsonParser();
    }

}
