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

    @Query(value = """
            INSERT INTO bigdata (id, sensor_id, event_ts, sensor_value)
            SELECT t1.value, t2.value, t3.value, t4.value
            FROM UNNEST(:ids) WITH ORDINALITY as t1 (value)
                 JOIN UNNEST(:sensors) WITH ORDINALITY as t2 (value)
                      ON t1.ordinality = t2.ordinality
                 JOIN UNNEST(:timestamps) WITH ORDINALITY as t3 (value)
                      ON t1.ordinality = t3.ordinality
                 JOIN UNNEST(:values) WITH ORDINALITY as t4 (value)
                      ON t1.ordinality = t4.ordinality
            ON CONFLICT (id) DO UPDATE SET sensor_value = EXCLUDED.sensor_value
            """, nativeQuery = true)
    @Modifying
    int batchWithUnnestAlternative(Long[] ids, String[] sensors, Instant[] timestamps, Float[] values);

}
