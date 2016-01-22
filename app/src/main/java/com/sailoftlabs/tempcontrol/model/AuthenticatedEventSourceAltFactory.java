package com.sailoftlabs.tempcontrol.model;

import org.kaazing.net.sse.SseEventSource;
import org.kaazing.net.sse.impl.DefaultEventSourceFactory;
import org.kaazing.net.sse.impl.SseEventSourceImpl;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by davidpos on 1/22/16.
 */
public class AuthenticatedEventSourceAltFactory extends DefaultEventSourceFactory {
    /**
     * Portions of this file copyright (c) 2007-2014 Kaazing Corporation.
     * All rights reserved.
     *
     * Licensed to the Apache Software Foundation (ASF) under one
     * or more contributor license agreements.  See the NOTICE file
     * distributed with this work for additional information
     * regarding copyright ownership.  The ASF licenses this file
     * to you under the Apache License, Version 2.0 (the
     * "License"); you may not use this file except in compliance
     * with the License.  You may obtain a copy of the License at
     *
     *   http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing,
     * software distributed under the License is distributed on an
     * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     * KIND, either express or implied.  See the License for the
     * specific language governing permissions and limitations
     * under the License.
     */



        private final ParticleAltCloud cloud;

        public AuthenticatedEventSourceAltFactory(ParticleAltCloud cloud) {
            this.cloud = cloud;
        }

        @Override
        public SseEventSource createEventSource(URI location) throws URISyntaxException {

            String scheme = location.getScheme();
            if (!scheme.toLowerCase().equals("sse")  &&
                    !scheme.toLowerCase().equals("http") &&
                    !scheme.toLowerCase().equals("https")) {
                String s = String.format("Incorrect scheme or protocol '%s'", scheme);
                throw new URISyntaxException(location.toString(), s);
            }

            SseEventSourceImpl eventSource = new AuthenticatedSseEventAltSourceImpl(location, cloud);

            // Set up the defaults from the factory.
            eventSource.setFollowRedirect(getDefaultFollowRedirect());
            eventSource.setRetryTimeout(getDefaultRetryTimeout());

            return eventSource;
        }

    }


