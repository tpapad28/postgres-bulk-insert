package com.tpapad.pbi.service;

import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@Service
@Slf4j
public class StatisticsService {

    @Value("${pbi.statistics.filepath}")
    String statisticsFilePath;

    public void exportAndLog(String process, long duration, int rows) {
        float speed = (float) rows / duration;
        try (CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter(statisticsFilePath, true)))) {
            writer.writeNext(new String[]{
                    process, Integer.toString(rows), Long.toString(duration), Float.toString(speed)

            });
        } catch (IOException e) {
            log.error("Error writing results to CSV file", e);
        }
        log.info("[{}] Saved {} records in {}ms (Speed: {} r/ms)", process, rows, duration, (float) rows / duration);
    }
}
