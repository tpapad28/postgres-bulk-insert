package com.tpapad.pbi.model;

import java.time.Instant;

public class BigDataArrays {
    public Long[] ids;
    public String[] sensorIds;
    public Instant[] eventTimes;
    public Float[] sensorValues;

    public BigDataArrays(final int size) {
        ids = new Long[size];
        sensorIds = new String[size];
        eventTimes = new Instant[size];
        sensorValues = new Float[size];
    }
}
