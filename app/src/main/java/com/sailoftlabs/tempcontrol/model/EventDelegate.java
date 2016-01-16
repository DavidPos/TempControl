package com.sailoftlabs.tempcontrol.model;

import android.net.Uri;
import android.support.annotation.Nullable;
import static io.particle.android.sdk.utils.Py.truthy;

/**
 * Created by davidpos on 1/16/16.
 */
public class EventDelegate {

    private static class EventApiUris {

        private final String EVENTS = "events";

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

        Uri buildMyDevicesEventUri(@Nullable String eventNamePrefix) {
            if (truthy(eventNamePrefix)) {
                return myDevicesEventsUri.buildUpon().appendPath(eventNamePrefix).build();
            } else {
                return myDevicesEventsUri;
            }
        }

        Uri buildSingleDeviceEventUri(@Nullable String eventNamePrefix, String deviceId) {
            Uri.Builder builder = devicesBaseUri.buildUpon().appendPath(deviceId);
            if (truthy(eventNamePrefix)) {
                builder.appendPath(eventNamePrefix);
            }
            return builder.build();
        }
    }
}
