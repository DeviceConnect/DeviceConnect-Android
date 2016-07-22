package org.deviceconnect.android.profile.spec;


public class FileParameterSpec extends DConnectParameterSpec<FileDataSpec> {

    FileParameterSpec() {
        super(new FileDataSpec());
    }

    public static class Builder extends BaseBuilder<Builder> {

        public FileParameterSpec build() {
            FileParameterSpec spec = new FileParameterSpec();
            spec.setName(mName);
            spec.setRequired(mIsRequired);
            return spec;
        }

        @Override
        protected Builder getThis() {
            return this;
        }
    }
}
