package com.sailoftlabs.tempcontrol.model;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.LongSparseArray;

import com.google.gson.Gson;

import org.kaazing.net.sse.SseEventReader;
import org.kaazing.net.sse.SseEventSource;
import org.kaazing.net.sse.SseEventSourceFactory;
import org.kaazing.net.sse.SseEventType;
import org.kaazing.net.sse.impl.AuthenticatedEventSourceFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import io.particle.android.sdk.cloud.ApiDefs;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.cloud.ParticleEventVisibility;
import io.particle.android.sdk.utils.TLog;
import retrofit.RetrofitError;

import static io.particle.android.sdk.utils.Py.truthy;

/**
 * Created by davidpos on 1/16/16.
 */
public class EventDelegate {
    private static final TLog log = TLog.get(EventDelegate.class);

    private final ApiDefs.CloudApi cloudApi;
    private final EventApiUris uris;
    private final Gson gson;
    private final ExecutorService executor;
    private final SseEventSourceFactory eventSourceFactory;
    private String accessToken;

    private final AtomicLong subscriptionIdGenerator = new AtomicLong(1);
    private final LongSparseArray<EventReader> eventReaders = new LongSparseArray<>();

    EventDelegate(ApiDefs.CloudApi cloudApi, Uri baseApiUri, Gson gson, ExecutorService executor,
                   ParticleAltCloud cloud) {
        this.cloudApi = cloudApi;
        this.gson = gson;
        this.executor = executor;
        this.eventSourceFactory = new AuthenticatedEventSourceFactory(cloud);
        this.uris = new EventApiUris(baseApiUri);
    }

    @WorkerThread
    void publishEvent(String eventName, String event,
                      @ParticleEventVisibility int eventVisibility, int timeToLive)
            throws ParticleCloudException {

        boolean isPrivate = eventVisibility != ParticleEventVisibility.PUBLIC;
        try {
            cloudApi.publishEvent(eventName, event, isPrivate, timeToLive);
        } catch (RetrofitError error) {
            throw new ParticleCloudException(error);
        }
    }

    @WorkerThread
    long subscribeToAllEvents(@Nullable String eventNamePrefix,
                              ParticleEventHandler handler) throws IOException {
        return subscribeToEventWithUri(uris.buildAllEventsUri(eventNamePrefix), handler);
    }

    @WorkerThread
    long subscribeToMyDevicesEvents(@Nullable String eventNamePrefix,
                                    ParticleEventHandler handler) throws IOException {
        return subscribeToEventWithUri(uris.buildMyDevicesEventUri(eventNamePrefix, accessToken), handler);
    }

    @WorkerThread
    long subscribeToDeviceEvents(@Nullable String eventNamePrefix, String deviceID,
                                 ParticleEventHandler eventHandler) throws IOException {
        return subscribeToEventWithUri(
                uris.buildSingleDeviceEventUri(eventNamePrefix, accessToken, deviceID),
                eventHandler);
    }

    @WorkerThread
    void unsubscribeFromEventWithID(long eventListenerID) throws ParticleCloudException {
        synchronized (eventReaders) {
            EventReader reader = eventReaders.get(eventListenerID);
            if (reader == null) {
                log.w("No event listener subscription found for ID '" + eventListenerID + "'!");
                return;
            }
            eventReaders.remove(eventListenerID);
            reader.stopListening();
        }
    }


    private long subscribeToEventWithUri(Uri uri, ParticleEventHandler handler) throws IOException {
        synchronized (eventReaders) {

            long subscriptionId = subscriptionIdGenerator.getAndIncrement();
            EventReader reader = new EventReader(handler, executor, gson, uri, eventSourceFactory);
            eventReaders.put(subscriptionId, reader);

            log.d("Created event subscription with ID " + subscriptionId + " for URI " + uri);

            reader.startListening();

            return subscriptionId;
        }
    }


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
                        log.w("type null or not data: " + type);
                    }
                    type = sseEventReader.next();
                }
            } catch (IOException e) {
                handler.onEventError(e);
            }
        }
    }


    private static class EventApiUris {

        private final String EVENTS = "events";
        private String AUTH_KEY = "?access_token=";

        private final Uri allEventsUri;
        private final Uri devicesBaseUri;
        private final Uri myDevicesEventsUri;




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
                return myDevicesEventsUri.buildUpon().appendPath(eventNamePrefix + AUTH_KEY + accessToken).build();
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
