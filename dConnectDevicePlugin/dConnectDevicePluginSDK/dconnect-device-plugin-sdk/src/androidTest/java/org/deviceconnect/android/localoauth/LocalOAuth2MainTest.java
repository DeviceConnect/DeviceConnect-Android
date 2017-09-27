/*
 LocalOAuth2MainTest.java
 Copyright (c) 2017 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.localoauth;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import org.deviceconnect.android.localoauth.exception.AuthorizationException;
import org.deviceconnect.android.localoauth.oauthserver.db.SQLiteToken;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.restlet.ext.oauth.PackageInfoOAuth;
import org.restlet.ext.oauth.internal.Client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import static org.deviceconnect.android.localoauth.LocalOAuth2Main.createClient;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * LocalOAuth2Mainの単体テスト.
 *
 * @author NTT DOCOMO, INC.
 */
@RunWith(AndroidJUnit4.class)
public class LocalOAuth2MainTest {

    private static Context mContext;

    @BeforeClass
    public static void execBeforeClass() {
        mContext = new RenamingDelegatingContext(InstrumentationRegistry.getInstrumentation().getTargetContext(), "test_");
        LocalOAuth2Main.setUseAutoTestMode(true);
        LocalOAuth2Main.initialize(mContext);
    }

    @AfterClass
    public static void execAfterClass() {
        LocalOAuth2Main.destroy();
        mContext = null;
    }

    @Test
    public void LocalOAuth2Main_createClient() {
        final String origin = "test";
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_createClient_packageInfo_null() {
        try {
            createClient(null);
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_createClient_packageName_null() {
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(null);
        try {
            LocalOAuth2Main.createClient(packageInfo);
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_createClient_packageName_empty() {
        PackageInfoOAuth packageInfo = new PackageInfoOAuth("");
        try {
            createClient(packageInfo);
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test
    public void LocalOAuth2Main_confirmPublishAccessToken() {
        final String origin = "test";
        final String serviceId = "test_service_id";
        final String[] scopes = {
                "serviceDiscovery"
        };
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<AccessTokenData> accessToken = new AtomicReference<>();
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                    .clientId(clientData.getClientId()).scopes(scopes).applicationName("JUnit")
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();
            LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                    accessToken.set(accessTokenData);
                    mLatch.countDown();
                }

                @Override
                public void onReceiveException(final Exception exception) {
                    mLatch.countDown();
                }
            });

            mLatch.await(30, TimeUnit.SECONDS);

            AccessTokenData data = accessToken.get();
            assertThat(data, is(notNullValue()));
            assertThat(data.getAccessToken(), is(notNullValue()));
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        } catch (InterruptedException e) {
            fail("timeout");
        }
    }

    @Test
    public void LocalOAuth2Main_confirmPublishAccessToken_multiple() {
        final int count = 10;
        final CountDownLatch mLatch = new CountDownLatch(count);
        final AtomicReferenceArray<Boolean> results = new AtomicReferenceArray<>(count);
        for (int i = 0; i < count; i++) {
            final String origin = "test" + i;
            final String serviceId = "test_service_id" + i;
            final String[] scopes = {
                    "serviceDiscovery"
            };
            final int index = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AccessTokenData data = createAccessToken(origin, serviceId, scopes);
                        CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(data.getAccessToken(), scopes[0], null);
                        if (result != null) {
                            results.set(index, result.checkResult());
                        } else {
                            results.set(index, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mLatch.countDown();
                }
            }).start();
        }

        try {
            mLatch.await(180, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("timeout");
        }

        for (int i = 0; i < count; i++) {
            assertThat(results.get(i), is(true));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_confirmPublishAccessToken_params_null() {
        final String origin = "test";
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            LocalOAuth2Main.confirmPublishAccessToken(null, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                }

                @Override
                public void onReceiveException(final Exception exception) {
                }
            });
            fail();
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_confirmPublishAccessToken_listener_null() {
        final String origin = "test";
        final String serviceId = "test_service_id";
        final String[] scopes = {
                "serviceDiscovery"
        };
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                    .clientId(clientData.getClientId()).scopes(scopes).applicationName("JUnit")
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();
            LocalOAuth2Main.confirmPublishAccessToken(params, null);
            fail();
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_confirmPublishAccessToken_context_null() {
        final String origin = "test";
        final String serviceId = "test_service_id";
        final String[] scopes = {
                "serviceDiscovery"
        };
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<AccessTokenData> accessToken = new AtomicReference<>();
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(null).serviceId(serviceId)
                    .clientId(clientData.getClientId()).scopes(scopes).applicationName("JUnit")
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();
            LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                    accessToken.set(accessTokenData);
                    mLatch.countDown();
                }

                @Override
                public void onReceiveException(final Exception exception) {
                    mLatch.countDown();
                }
            });
            fail();
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_confirmPublishAccessToken_application_name_null() {
        final String origin = "test";
        final String serviceId = "test_service_id";
        final String[] scopes = {
                "serviceDiscovery"
        };
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<AccessTokenData> accessToken = new AtomicReference<>();
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                    .clientId(clientData.getClientId()).scopes(scopes).applicationName(null)
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();
            LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                    accessToken.set(accessTokenData);
                    mLatch.countDown();
                }

                @Override
                public void onReceiveException(final Exception exception) {
                    mLatch.countDown();
                }
            });
            fail();
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_confirmPublishAccessToken_client_null() {
        final String origin = "test";
        final String serviceId = "test_service_id";
        final String[] scopes = {
                "serviceDiscovery"
        };
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<AccessTokenData> accessToken = new AtomicReference<>();
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                    .clientId(null).scopes(scopes).applicationName("JUnit")
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();
            LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                    accessToken.set(accessTokenData);
                    mLatch.countDown();
                }

                @Override
                public void onReceiveException(final Exception exception) {
                    mLatch.countDown();
                }
            });
            fail();
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void LocalOAuth2Main_confirmPublishAccessToken_scopes_null() {
        final String origin = "test";
        final String serviceId = "test_service_id";
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<AccessTokenData> accessToken = new AtomicReference<>();
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                    .clientId("TEST").scopes(null).applicationName("JUnit")
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();
            LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                    accessToken.set(accessTokenData);
                    mLatch.countDown();
                }

                @Override
                public void onReceiveException(final Exception exception) {
                    mLatch.countDown();
                }
            });
            fail();
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        }
    }

    @Test
    public void LocalOAuth2Main_checkAccessToken() {
        final String origin = "test_check";
        final String serviceId = "test_service_id_check";
        final String[] scopes = {
                "serviceDiscovery"
        };
        AccessTokenData data = createAccessToken(origin, serviceId, scopes);

        CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(data.getAccessToken(), scopes[0], null);
        assertThat(result, is(notNullValue()));
        assertThat(result.checkResult(), is(true));
        assertThat(result.isExistAccessToken(), is(true));
        assertThat(result.isExistClientId(), is(true));
        assertThat(result.isExistScope(), is(true));
        assertThat(result.isNotExpired(), is(true));
    }

    @Test
    public void LocalOAuth2Main_checkAccessToken_out_scope() {
        final String origin = "test_check_scope";
        final String serviceId = "test_service_id_check_scope";
        final String[] scopes = {
                "serviceDiscovery"
        };
        AccessTokenData data = createAccessToken(origin, serviceId, scopes);

        CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(data.getAccessToken(), "battery", null);
        assertThat(result, is(notNullValue()));
        assertThat(result.checkResult(), is(false));
        assertThat(result.isExistAccessToken(), is(true));
        assertThat(result.isExistClientId(), is(true));
        assertThat(result.isExistScope(), is(false));
        assertThat(result.isNotExpired(), is(false));
    }

    @Test
    public void LocalOAuth2Main_checkAccessToken_illegal_access_token() {
        CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken("test", "battery", null);
        assertThat(result, is(notNullValue()));
        assertThat(result.checkResult(), is(false));
        assertThat(result.isExistAccessToken(), is(false));
        assertThat(result.isExistClientId(), is(false));
        assertThat(result.isExistScope(), is(false));
        assertThat(result.isNotExpired(), is(false));
    }

    @Test
    public void LocalOAuth2Main_findClientPackageInfoByAccessToken() {
        final String origin = "test_find_client";
        final String serviceId = "test_service_id_find_client";
        final String[] scopes = {
                "serviceDiscovery"
        };
        AccessTokenData data = createAccessToken(origin, serviceId, scopes);

        ClientPackageInfo info = LocalOAuth2Main.findClientPackageInfoByAccessToken(data.getAccessToken());
        assertThat(info, is(notNullValue()));
        assertThat(info.getPackageInfo(), is(notNullValue()));
        assertThat(info.getPackageInfo().getPackageName(), is(origin));
//        assertThat(info.getPackageInfo().getServiceId(), is(serviceId));
    }

    @Test
    public void LocalOAuth2Main_findClientPackageInfoByAccessToken_illegal_access_token() {
        ClientPackageInfo info = LocalOAuth2Main.findClientPackageInfoByAccessToken("test");
        assertThat(info, is(nullValue()));
    }

    @Test
    public void LocalOAuth2Main_getAccessTokens() {
        final String origin = "test_access_token";
        final String serviceId = "test_service_id_access_token";
        final String[] scopes = {
                "serviceDiscovery"
        };
        AccessTokenData data = createAccessToken(origin, serviceId, scopes);
        assertThat(data, is(notNullValue()));
        assertThat(data.getAccessToken(), is(notNullValue()));

        SQLiteToken[] tokens = LocalOAuth2Main.getAccessTokens();
        assertThat(tokens, is(notNullValue()));
    }

    @Test
    public void LocalOAuth2Main_getAccessToken() {
        final String origin = "test";
        final String serviceId = "test_service_id";
        final String[] scopes = {
                "serviceDiscovery"
        };
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<AccessTokenData> accessToken = new AtomicReference<>();
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            assertThat(clientData, is(notNullValue()));

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                    .clientId(clientData.getClientId()).scopes(scopes).applicationName("JUnit")
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();
            LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                    accessToken.set(accessTokenData);
                    mLatch.countDown();
                }

                @Override
                public void onReceiveException(final Exception exception) {
                    mLatch.countDown();
                }
            });

            mLatch.await(30, TimeUnit.SECONDS);

            AccessTokenData data = accessToken.get();
            assertThat(data, is(notNullValue()));
            assertThat(data.getAccessToken(), is(notNullValue()));

            Client client = LocalOAuth2Main.findClientByClientId(clientData.getClientId());
            assertThat(client, is(notNullValue()));

            SQLiteToken token = LocalOAuth2Main.getAccessToken(client);
            assertThat(token, is(notNullValue()));
            assertThat(token.getAccessToken(), is(data.getAccessToken()));
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        } catch (InterruptedException e) {
            fail("timeout");
        }
    }

    @Test
    public void LocalOAuth2Main_destroyAccessToken() {
        final String origin = "test_delete_access_token";
        final String serviceId = "test_service_id_access_token";
        final String[] scopes = {
                "serviceDiscovery"
        };
        AccessTokenData data = createAccessToken(origin, serviceId, scopes);

        CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(data.getAccessToken(), scopes[0], null);
        assertThat(result, is(notNullValue()));
        assertThat(result.checkResult(), is(true));
        assertThat(result.isExistAccessToken(), is(true));
        assertThat(result.isExistClientId(), is(true));
        assertThat(result.isExistScope(), is(true));
        assertThat(result.isNotExpired(), is(true));

        SQLiteToken[] tokens = LocalOAuth2Main.getAccessTokens();
        assertThat(tokens, is(notNullValue()));

        for (SQLiteToken token : tokens) {
            if (token.getAccessToken() != null && token.getAccessToken().equals(data.getAccessToken())) {
                LocalOAuth2Main.destroyAccessToken(token.getId());
                break;
            }
        }

        result = LocalOAuth2Main.checkAccessToken(data.getAccessToken(), scopes[0], null);
        assertThat(result, is(notNullValue()));
        assertThat(result.checkResult(), is(false));
        assertThat(result.isExistAccessToken(), is(false));
        assertThat(result.isExistClientId(), is(false));
        assertThat(result.isExistScope(), is(false));
        assertThat(result.isNotExpired(), is(false));
    }

    @Test
    public void LocalOAuth2Main_destroyAllAccessToken() {
        final String origin = "test_delete_all_access_token";
        final String serviceId = "test_service_id_access_token";
        final String[] scopes = {
                "serviceDiscovery"
        };
        AccessTokenData data = createAccessToken(origin, serviceId, scopes);

        CheckAccessTokenResult result = LocalOAuth2Main.checkAccessToken(data.getAccessToken(), scopes[0], null);
        assertThat(result, is(notNullValue()));
        assertThat(result.checkResult(), is(true));
        assertThat(result.isExistAccessToken(), is(true));
        assertThat(result.isExistClientId(), is(true));
        assertThat(result.isExistScope(), is(true));
        assertThat(result.isNotExpired(), is(true));

        ClientPackageInfo clientPackageInfo = LocalOAuth2Main.findClientPackageInfoByAccessToken(data.getAccessToken());
        assertThat(clientPackageInfo, is(notNullValue()));

        LocalOAuth2Main.destroyAllAccessToken();

        result = LocalOAuth2Main.checkAccessToken(data.getAccessToken(), scopes[0], null);
        assertThat(result, is(notNullValue()));
        assertThat(result.checkResult(), is(false));
        assertThat(result.isExistAccessToken(), is(false));
        assertThat(result.isExistClientId(), is(false));
        assertThat(result.isExistScope(), is(false));
        assertThat(result.isNotExpired(), is(false));

        Client client = LocalOAuth2Main.findClientByClientId(clientPackageInfo.getClientId());
        assertThat(client, is(notNullValue()));
    }

    private AccessTokenData createAccessToken(final String origin, final String serviceId, final String[] scopes) {
        final CountDownLatch mLatch = new CountDownLatch(1);
        final AtomicReference<AccessTokenData> accessToken = new AtomicReference<>();
        PackageInfoOAuth packageInfo = new PackageInfoOAuth(origin);
        try {
            ClientData clientData = createClient(packageInfo);
            if (clientData == null) {
                return null;
            }

            ConfirmAuthParams params = new ConfirmAuthParams.Builder().context(mContext).serviceId(serviceId)
                    .clientId(clientData.getClientId()).scopes(scopes).applicationName("JUnit")
                    .isForDevicePlugin(false)
                    .keyword("Keyword")
                    .build();

            LocalOAuth2Main.confirmPublishAccessToken(params, new PublishAccessTokenListener() {
                @Override
                public void onReceiveAccessToken(final AccessTokenData accessTokenData) {
                    accessToken.set(accessTokenData);
                    mLatch.countDown();
                }
                @Override
                public void onReceiveException(final Exception exception) {
                    mLatch.countDown();
                }
            });

            mLatch.await(180, TimeUnit.SECONDS);

            return accessToken.get();
        } catch (AuthorizationException e) {
            fail("Failed to create client.");
        } catch (InterruptedException e) {
            fail("timeout");
        }
        return null;
    }
}
