package org.deviceconnect.android.profile.spec;


public class FileDataSpec extends DConnectDataSpec {

    FileDataSpec() {
        super(DataType.FILE);
    }

    public static class Builder {

        public FileDataSpec build() {
            return new FileDataSpec();
        }

    }

}
