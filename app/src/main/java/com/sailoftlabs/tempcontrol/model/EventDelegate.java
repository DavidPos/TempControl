package com.sailoftlabs.tempcontrol.model;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.kaazing.net.sse.SseEventReader;
import org.kaazing.net.sse.SseEventSource;
import org.kaazing.net.sse.SseEventSourceFactory;
import org.kaazing.net.sse.SseEventType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;

import static io.particle.android.sdk.utils.Py.truthy;

/**
 * Created by davidpos on 1/16/16.
 */
public class EventDelegate {


    private static class EventApiUris {

        private final String EVENTS = "events";
        private String AUTH_KEY = "?access_token=";

        private final Uri allEventsUri;
        private final Uri devicesBaseUri;
        private final Uri myDevicesEventsUri;

        private static class EventReader {

            final ParticleEventHandler handler;
            final SseEventSource sseEventSource;
            final ExecutorService executor;
            final Gson gson;

            volatile Future<?> future;

            private EventReader(ParticleEventHandler handler, ExecutorService executor, Gson gson,
                                Uri uri, SseEventSourceFactory factory) {
                this.handler = handler;
                this.executor = executor;
                this.gson = gson;
                try {
                    sseEventSource = factory.createEventSource(URI.create(uri.toString()));
                } catch (URISyntaxException e) {
                    // I don't like throwing exceptions in constructors, but this URI shouldn't be in
                    // the wrong format...
                    throw new RuntimeException(e);
                }
            }

            void startListening() throws IOException {
                sseEventSource.connect();
                final SseEventReader sseEventReader = sseEventSource.getEventReader();

                future = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        startHandlingEvents(sseEventReader);
                    }
                });
            }

            void stopListening() {
                future.cancel(false);
            }


            private void startHandlingEvents(SseEventReader sseEventReader) {
                SseEventType type;
                try {
                    type = sseEventReader.next();
                    while (type != SseEventType.EOS) {

                        if (type != null && type.equals(SseEventType.DATA)) {
                            CharSequence data = sseEventReader.getData();
                            String asStr = data.toString();

                            ParticleEvent event = gson.fromJson(asStr, ParticleEvent.class);

                            handler.onEvent(sseEventReader.getName(), event);

                        } else {
                            Log.e("Event Delegate: ", "type null or not data: " + type);
                        }
                        type = sseEventReader.next();
                    }
                } catch (IOException e) {
                    handler.onEventError(e);
                }
            }
        }

        EventApiUris(Uri baseUri) {
            allEventsUri = baseUri.buildUpon().path("/v1/" + EVENTS).build();
            devicesBaseUri = baseUri.buildUpon().path("/v1/devices").build();
            myDevicesEventsUri = devicesBaseUri.buildUpon().appendPath(EVENTS).build();

        }

        Uri buildAllEventsUri(@Nullable String eventNamePrefix) {
            if (truthy(eventNamePrefix)) {
                return allEventsUri.buildUpon().appendPath(eventNamePrefix).build();
            } else {
                return allEventsUri;
            }
        }

        Uri buildMyDevicesEventUri(@Nullable String eventNamePrefix, String accessToken) {
            if (truthy(eventNamePrefix)) {
                return myDevicesEventsUri.buildUpon().appendPath(eventNamePrefix).build();
            } else {
                return myDevicesEventsUri;
            }
        }

        Uri buildSingleDeviceEventUri(@Nullable String eventNamePrefix, String deviceId, String accessToken) {
            Uri.Builder builder = devicesBaseUri.buildUpon().appendPath(deviceId);
            if (truthy(eventNamePrefix)) {
                builder.appendPath(eventNamePrefix + AUTH_KEY + accessToken);
            }
            return builder.build();
        }
    }
}
