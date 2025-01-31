package com.tpapad.pbi.service;

import com.tpapad.pbi.model.BigData;
import com.tpapad.pbi.model.BigDataArrays;
import com.tpapad.pbi.repository.BigDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoaderService {

    private final BigDataRepository repository;
    private final Random r = new Random();
    private static final int BATCH_SIZE = 20_000;

    @Scheduled(initialDelay = 5, fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void testWithSaveAll() {
        log.info("[SAVEALL] Starting...");
        final List<BigData> batch = generateBatch(BATCH_SIZE);
        final long startTime = System.nanoTime();
        repository.saveAll(batch);
        final Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("[SAVEALL] Saved {} records in {}ms (Speed: {} r/ms)", BATCH_SIZE, duration.toMillis(), (float) BATCH_SIZE / duration.toMillis());
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void testWithUnnest() {
        log.info("[UNNEST] Starting...");
        final List<BigData> batch = generateBatch(BATCH_SIZE);
        final BigDataArrays arrays = generateArrays(batch, "UNNEST");

        final long startTime = System.nanoTime();
        int rowsInserted = repository.batchWithUnnest(arrays.ids, arrays.sensorIds, arrays.eventTimes, arrays.sensorValues);
        final Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("[UNNEST] Saved {} ({} in batch) records in {}ms (Speed: {} r/ms)", rowsInserted, BATCH_SIZE, duration.toMillis(), (float) BATCH_SIZE / duration.toMillis());
    }

    @Scheduled(initialDelay = 2, fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    @Transactional
    public void testWithUnnestAlternative() {
        log.info("[Multi-UNNEST] Starting...");
        final List<BigData> batch = generateBatch(BATCH_SIZE);
        final BigDataArrays arrays = generateArrays(batch, "Multi-UNNEST");

        final long startTime = System.nanoTime();
        int rowsInserted = repository.batchWithUnnestAlternative(arrays.ids, arrays.sensorIds, arrays.eventTimes, arrays.sensorValues);
        final Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
        log.info("[Multi-UNNEST] Saved {} ({} in batch) records in {}ms (Speed: {} r/ms)", rowsInserted, BATCH_SIZE, duration.toMillis(), (float) BATCH_SIZE / duration.toMillis());
    }

    private List<BigData> generateBatch(final int size) {
        final List<BigData> batch = new ArrayList<>(size);
        final long batchStartingId = r.nextLong();
        for (int i = 0; i < size; i++) {
            batch.add(new BigData(batchStartingId + i, String.format("Sensor-%s", r.nextInt(10)), Instant.now(), r.nextFloat()));
        }
        return batch;
    }

    private BigDataArrays generateArrays(List<BigData> batch, String step) {
        // In case we need to time this extra step that prepares the arrays for `unnest`...
        final long prepStart = System.nanoTime();
        final int size = batch.size();
        final BigDataArrays arrays = new BigDataArrays(size);
        final AtomicInteger index = new AtomicInteger(0);
        batch.stream().parallel().forEach(bigData -> {
            final int i = index.getAndIncrement();
            arrays.ids[i] = bigData.getId();
            arrays.sensorIds[i] = bigData.getSensorId();
            arrays.eventTimes[i] = bigData.getEventTime();
            arrays.sensorValues[i] = bigData.getSensorValue();
        });
        final Duration prepDuration = Duration.ofNanos(System.nanoTime() - prepStart);
        log.trace("[{}] Preparation of {} records in {} (Speed: {} r/ms)", step, BATCH_SIZE, prepDuration, (float) BATCH_SIZE / prepDuration.toMillis());
        return arrays;
    }
}
