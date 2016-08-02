/*
 SwaggerJsonParserFactory.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.profile.spec.parser;


class SwaggerJsonParserFactory extends DConnectProfileSpecJsonParserFactory {

    @Override
    public DConnectProfileSpecJsonParser createParser() {
        return new SwaggerJsonParser();
    }

}
