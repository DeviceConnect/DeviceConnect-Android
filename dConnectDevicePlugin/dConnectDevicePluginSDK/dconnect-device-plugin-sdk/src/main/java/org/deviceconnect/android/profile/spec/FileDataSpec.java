package org.deviceconnect.android.profile.spec;


public class FileDataSpec extends DConnectDataSpec {

    FileDataSpec() {
        super(DataType.FILE);
    }

    @Override
    public boolean validate(final Object param) {
        return true;
    }

    public static class Builder {

        public FileDataSpec build() {
            return new FileDataSpec();
        }

    }

}
