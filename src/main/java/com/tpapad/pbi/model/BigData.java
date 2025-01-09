package com.tpapad.pbi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "bigdata")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BigData {

    @Id
    @Column(name = "id")
    Long id;
    @Column(name = "sensor_id")
    String sensorId;
    @Column(name = "event_ts")
    Instant eventTime;
    @Column(name = "sensor_value")
    float sensorValue;
}
