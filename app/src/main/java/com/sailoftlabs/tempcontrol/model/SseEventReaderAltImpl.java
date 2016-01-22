package com.sailoftlabs.tempcontrol.model;

import org.kaazing.net.impl.util.BlockingQueueImpl;
import org.kaazing.net.sse.SseEventReader;
import org.kaazing.net.sse.SseEventType;
import org.kaazing.net.sse.SseException;
import org.kaazing.net.sse.impl.SseEventReaderImpl;
import org.kaazing.net.sse.impl.SseEventSourceImpl;
import org.kaazing.net.sse.impl.SsePayload;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by davidpos on 1/22/16.
 */
public class SseEventReaderAltImpl extends SseEventReader{
    //
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


        private static final String _CLASS_NAME = SseEventReaderImpl.class.getName();
        private static final Logger _LOG;
        private final BlockingQueueImpl<Object> _sharedQueue;
        private final SseEventSourceImpl _eventSource;
        private SsePayload _payload;
        private SseEventType _eventType;
        private String _eventName;
        private String _data;

        public SseEventReaderAltImpl(SseEventSourceImpl eventSource, BlockingQueueImpl<Object> sharedQueue) {
            this._eventSource = eventSource;
            this._sharedQueue = sharedQueue;
            this._payload = null;
            this._eventType = null;
            this._eventName = null;
            this._data = null;
        }

        public CharSequence getData() throws IOException {
            if(this._payload == null) {
                return null;
            } else if(this._eventType != SseEventType.DATA) {
                String s = "readData() can only be used to read events of type SseEventType.DATA";
                throw new SseException(s);
            } else {
                return this._data;
            }
        }

        public String getName() {
            return this._eventName;
        }

        public SseEventType getType() {
            return this._eventType;
        }

        public SseEventType next() throws IOException {
            if(this._sharedQueue.isDone()) {
                this._eventType = SseEventType.EOS;
                return this._eventType;
            } else {
                synchronized(this) {
                    if(!this._eventSource.isConnected()) {
                        this._eventType = SseEventType.EOS;
                        return this._eventType;
                    }

                    try {
                        this._payload = null;
                        this._payload = (SsePayload)this._sharedQueue.take();
                    } catch (InterruptedException var4) {
                        _LOG.log(Level.FINE, var4.getMessage());
                    }

                    if(this._payload == null) {
                        String s = "Reader has been interrupted maybe the connection is closed";
                        _LOG.log(Level.FINE, _CLASS_NAME, s);
                        this._eventType = SseEventType.EOS;
                        return this._eventType;
                    }

                    this._data = this._payload.getData();
                    this._eventName = this._payload.getEventName();
                    this._eventType = this._payload.getData() == null?SseEventType.EMPTY:SseEventType.DATA;
                }

                return this._eventType;
            }
        }

        void close() throws IOException {
            this._sharedQueue.done();
            this._payload = null;
            this._eventType = null;
            this._data = null;
            this._eventName = null;
        }

        void reset() throws IOException {
            this._sharedQueue.reset();
            this._payload = null;
            this._eventType = null;
            this._data = null;
            this._eventName = null;
        }

        private CharSequence readData() throws IOException {
            if(!this._eventSource.isConnected()) {
                String s = "Can\'t read using the MessageReader if the event source is not connected";
                throw new SseException(s);
            } else {
                synchronized(this) {
                    if(this._payload != null) {
                        if(this._eventType != SseEventType.DATA) {
                            String type1 = "readData() can only be used to read events of type SseEventType.DATA";
                            throw new SseException(type1);
                        } else {
                            this._payload = null;
                            return this._data;
                        }
                    } else {
                        SseEventType type = this.next();
                        if(type != SseEventType.DATA) {
                            String s1 = "readData() can only be used to read events of type SseEventType.DATA";
                            throw new SseException(s1);
                        } else {
                            this._data = this._payload.getData();
                            this._eventName = this._payload.getEventName();
                            this._payload = null;
                            return this._data;
                        }
                    }
                }
            }
        }

        static {
            _LOG = Logger.getLogger(_CLASS_NAME);
        }
    }


