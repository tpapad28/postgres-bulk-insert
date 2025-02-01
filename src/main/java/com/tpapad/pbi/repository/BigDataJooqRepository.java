package com.tpapad.pbi.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertValuesStepN;
import org.jooq.Record;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.*;
import static org.jooq.impl.DSL.field;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BigDataJooqRepository {
    private final DSLContext dsl;

    public int batchWithUnnestAlternativeJooq(Long[] ids, String[] sensors, Instant[] timestamps, Float[] values, final int size) {
        // In case we need to time this extra step that prepares the queries...
        final long prepStart = System.nanoTime();

        String tableName = "bigdata";

        List<String> primaryKeyFieldNames = new ArrayList<>();
        primaryKeyFieldNames.add("id");

        Set<String> distinctVariableFieldNames = new HashSet<>();
        distinctVariableFieldNames.add("sensor_id");
        distinctVariableFieldNames.add("event_ts");
        distinctVariableFieldNames.add("sensor_value");

        var insertFields = Stream.concat(primaryKeyFieldNames.stream(), distinctVariableFieldNames.stream()).map(DSL::field).toArray(Field[]::new);
        var updatePrimaryKeys = primaryKeyFieldNames.stream().map(DSL::field).toArray(Field[]::new);

        InsertValuesStepN<Record> steps = dsl.insertInto(table(tableName), insertFields);
        steps.select(select(field("t1.value"), field("t2.value"), field("t3.value"), field("t4.value"))
                .from(unnest(ids).withOrdinality().as("t1", "value"))
                .join(unnest(sensors).withOrdinality().as("t2", "value"))
                .on(field("t1.ordinality").eq(field("t2.ordinality")))
                .join(unnest(timestamps).withOrdinality().as("t3", "value"))
                .on(field("t1.ordinality").eq(field("t3.ordinality")))
                .join(unnest(values).withOrdinality().as("t4", "value"))
                .on(field("t1.ordinality").eq(field("t4.ordinality"))));
        steps.onConflict(updatePrimaryKeys).doUpdate().set(field("sensor_value"), (Object) excluded(field("sensor_value")));
        final Duration prepDuration = Duration.ofNanos(System.nanoTime() - prepStart);
        log.info("[jOOQ] Query Preparation in {}ms", prepDuration);
        log.trace("[jOOQ] Query:\n{}", steps.getSQL(ParamType.INLINED));

        final long startTime = System.nanoTime();
        int rows = steps.execute();
        final Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("[jOOQ] Saved {} ({} in batch) records in {}ms (Speed: {} r/ms)", rows, size, duration.toMillis(), (float) size / duration.toMillis());

        return rows;
    }
}
