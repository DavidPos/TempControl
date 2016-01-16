package com.sailoftlabs.tempcontrol.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by davidpos on 1/15/16.
 */
public class Event implements Parcelable {
    public String deviceId;
    public String dataPayload;
    public Date publishedAt;
    public int timeToLive;

    protected Event(Parcel in) {
        this.deviceId = in.readString();
        this.dataPayload = in.readString();
        this.publishedAt = (Date) in.readValue(Date.class.getClassLoader());
        this.timeToLive = (Integer)in.readValue(Integer.class.getClassLoader());
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceId);
        dest.writeString(this.dataPayload);
        dest.writeValue(this.publishedAt);
        dest.writeValue(this.timeToLive);
    }
}
