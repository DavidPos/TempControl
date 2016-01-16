package com.sailoftlabs.tempcontrol.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by davidpos on 1/15/16.
 */
public class Event implements Parcelable {


    protected Event(Parcel in) {
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
    }
}
