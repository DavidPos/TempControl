package com.sailoftlabs.tempcontrol.model;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.Date;

import io.particle.android.sdk.cloud.ApiDefs;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by davidpos on 1/22/16.
 */
public class AltApiFactory {


        // FIXME: this feels kind of lame... but maybe it's OK in practice. Need to think more about it.
        public interface TokenGetterDelegate {

            String getTokenValue();

        }


        public interface OauthBasicAuthCredentialsProvider {

            String getClientId();

            String getClientSecret();
        }


        private final Context ctx;
        private final TokenGetterDelegate tokenDelegate;
        private final OkHttpClient okHttpClient;
        private final OauthBasicAuthCredentialsProvider basicAuthCredentialsProvider;
        private final Gson gson;

        AltApiFactory(Context ctx, TokenGetterDelegate tokenGetterDelegate,
                   OauthBasicAuthCredentialsProvider basicAuthProvider) {
            this.ctx = ctx.getApplicationContext();
            this.tokenDelegate = tokenGetterDelegate;
            this.okHttpClient = new OkHttpClient();
            this.basicAuthCredentialsProvider = basicAuthProvider;
            this.gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, new StringlyTypedDateAdapter())
                    .create();
        }


        ApiDefs.CloudApi buildNewCloudApi() {
            RestAdapter restAdapter = buildCommonRestAdapterBuilder(gson)
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("Authorization", "Bearer " + tokenDelegate.getTokenValue());
                        }
                    })
                    .build();
            return restAdapter.create(ApiDefs.CloudApi.class);
        }

        ApiDefs.IdentityApi buildNewIdentityApi() {
            final String basicAuthValue = getBasicAuthValue();

            RestAdapter restAdapter = buildCommonRestAdapterBuilder(gson)
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("Authorization", basicAuthValue);
                        }
                    })
                    .build();
            return restAdapter.create(ApiDefs.IdentityApi.class);
        }

        Uri getApiUri() {
            return Uri.parse(ctx.getString(io.particle.android.sdk.cloud.R.string.api_base_uri));
        }

        Gson getGsonInstance() {
            return gson;
        }

        private String getBasicAuthValue() {
            String authString = String.format("%s:%s",
                    basicAuthCredentialsProvider.getClientId(),
                    basicAuthCredentialsProvider.getClientSecret());
            return "Basic " + Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
        }

        private RestAdapter.Builder buildCommonRestAdapterBuilder(Gson gson) {
            return new RestAdapter.Builder()
                    .setClient(new OkClient(okHttpClient))
                    .setConverter(new GsonConverter(gson))
                    .setEndpoint(getApiUri().toString())
                    .setLogLevel(RestAdapter.LogLevel.valueOf(ctx.getString(io.particle.android.sdk.cloud.R.string.http_log_level)));
        }


        public static class ResourceValueBasicAuthCredentialsProvider
                implements OauthBasicAuthCredentialsProvider {

            private final String clientId;
            private final String clientSecret;

            public ResourceValueBasicAuthCredentialsProvider(
                    Context ctx, @StringRes int clientIdResId, @StringRes int clientSecretResId) {
                this.clientId = ctx.getString(clientIdResId);
                this.clientSecret = ctx.getString(clientSecretResId);
            }


            @Override
            public String getClientId() {
                return clientId;
            }

            @Override
            public String getClientSecret() {
                return clientSecret;
            }
        }


        private static class StringlyTypedDateAdapter implements JsonDeserializer<Date> {

            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
                String asStr = json.getAsString();
                return new DateTime(asStr).toDate();
            }
        }

    }


