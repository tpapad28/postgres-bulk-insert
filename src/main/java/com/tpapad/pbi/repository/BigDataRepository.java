package com.tpapad.pbi.repository;

import com.tpapad.pbi.model.BigData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface BigDataRepository extends JpaRepository<BigData, Long> {

    @Query(value = """
            INSERT INTO bigdata (id, sensor_id, event_ts, sensor_value)
            SELECT * FROM UNNEST(:ids, :sensors, :timestamps, :values)
            ON CONFLICT (id) DO UPDATE SET sensor_value = EXCLUDED.sensor_value
            """, nativeQuery = true)
    @Modifying
    int batchWithUnnest(Long[] ids, String[] sensors, Instant[] timestamps, Float[] values);

}
