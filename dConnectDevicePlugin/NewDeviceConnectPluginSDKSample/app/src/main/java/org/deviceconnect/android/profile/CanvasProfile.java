package org.deviceconnect.android.profile;


import android.content.Intent;

public class CanvasProfile extends DConnectProfile implements CanvasProfileConstants {

    @Override
    protected boolean onRequest(final Intent request, final Intent response) {
        return super.onRequest(request, response);
    }

    /**
     * @deprecated
     */
    protected boolean onPostDrawImage(final Intent request, final Intent response,
                                      final String serviceId, final Integer x, final Integer y) {
        return true;
    }

    /**
     * @deprecated
     */
    protected boolean onDeleteDrawImage(final Intent request, final Intent response,
                                        final String serviceId) {
        return true;
    }

    public abstract static class DrawImage extends Api {

        private static final RequestParam[] REQUEST_PARAMS = {
            new RequestIntegerParam(PARAM_X, false),
            new RequestIntegerParam(PARAM_Y, false)
        };

        @Override
        public String getName() {
            return "Canvas Draw Image API";
        }

        @Override
        public Method getMethod() {
            return Method.POST;
        }

        @Override
        public String getPath() {
            return "/gotapi/canvas/drawImage";
        }

        @Override
        public RequestParam[] getDefinedRequestParams() {
            return REQUEST_PARAMS;
        }

        protected boolean onRequest(final DConnectServiceEndPoint service,
                                    final Intent request, final Intent response,
                                    final String serviceId, final Integer x, final Integer y) {
            return true;
        }

    }

    public abstract static class DeleteImage extends Api {

        private static final RequestParam[] REQUEST_PARAMS = {

        };

        @Override
        public String getName() {
            return "Canvas Delete Image API";
        }

        @Override
        public Method getMethod() {
            return Method.DELETE;
        }

        @Override
        public String getPath() {
            return "/gotapi/canvas/drawImage";
        }

        @Override
        public RequestParam[] getDefinedRequestParams() {
            return REQUEST_PARAMS;
        }

        protected boolean onRequest(final DConnectServiceEndPoint service,
                                    final Intent request, final Intent response,
                                    final String serviceId) {
            return true;
        }

    }
}
