package com.sailoftlabs.tempcontrol.model;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.Executors;

import io.particle.android.sdk.cloud.ApiDefs;
import io.particle.android.sdk.cloud.ApiFactory;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.SDKGlobals;

/**
 * Created by davidpos on 1/22/16.
 */
public class AltSDKProvider {


        private final Context ctx;
        private final ApiDefs.CloudApi cloudApi;
        private final ApiDefs.IdentityApi identityApi;
        private final ParticleAltCloud particleCloud;
        private final TokenGetterDelegateImpl tokenGetter;

        AltSDKProvider(Context context,
                    @Nullable AltApiFactory.OauthBasicAuthCredentialsProvider oAuthCredentialsProvider) {

            this.ctx = context.getApplicationContext();

            if (oAuthCredentialsProvider == null) {
                oAuthCredentialsProvider = new AltApiFactory.ResourceValueBasicAuthCredentialsProvider(
                        ctx, io.particle.android.sdk.cloud.R.string.oauth_client_id, io.particle.android.sdk.cloud.R.string.oauth_client_secret);
            }

            tokenGetter = new TokenGetterDelegateImpl();

            AltApiFactory apiFactory = new AltApiFactory(ctx, tokenGetter, oAuthCredentialsProvider);
            cloudApi = apiFactory.buildNewCloudApi();
            identityApi = apiFactory.buildNewIdentityApi();
            particleCloud = buildCloud(apiFactory);
        }


        ApiDefs.CloudApi getCloudApi() {
            return cloudApi;
        }

        ApiDefs.IdentityApi getIdentityApi() {
            return identityApi;
        }

        ParticleCloud getParticleCloud() {
            return particleCloud;
        }


        private ParticleCloud buildCloud(ApiFactory apiFactory) {
            SDKGlobals.init(ctx);

            // FIXME: see if this TokenGetterDelegate setter issue can be resolved reasonably
            ParticleCloud cloud = new ParticleCloud(apiFactory.getApiUri(), cloudApi, identityApi,
                    SDKGlobals.getAppDataStorage(), LocalBroadcastManager.getInstance(ctx),
                    apiFactory.getGsonInstance(), Executors.newCachedThreadPool());
            // FIXME: gross circular dependency
            tokenGetter.cloud = cloud;

            return cloud;
        }


        private static class TokenGetterDelegateImpl implements ApiFactory.TokenGetterDelegate {

            private volatile ParticleCloud cloud;

            @Override
            public String getTokenValue() {
                return cloud.getAccessToken();
            }
        }
    }
}
