package com.portfolio.yieldcurve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


@Service
public class JsonToPostgres {

    private String yieldCurveDataFolderPath = "json/yieldcurvedata2/";
    private Map<String, Object> yieldCurveData;

    @Autowired
    private DataSource dataSource;


    @EventListener(ApplicationReadyEvent.class)
    private void convertJsonToPostgres() {
        ObjectMapper mapper = new ObjectMapper();
        yieldCurveData = new HashMap<>();
        int currentYear = 2022;

        for (int year = 1990; year <= currentYear; year++) {
            Resource resource = new ClassPathResource(yieldCurveDataFolderPath + year + ".json");
            try {
                Map<String, Object> fileData = mapper.readValue(resource.getInputStream(), Map.class);
                mergeData(fileData);
            } catch (IOException e) {
                System.out.println("Error loading file: " + year + "\n" + e);
            }
        }


        List<String> allDates = (List<String>) yieldCurveData.get("dates");
        List<String> maturities = List.of("1M", "2M", "3M", "4M", "6M", "1Y", "5Y", "10Y", "30Y" );
        List<List<Double>> yields = (List<List<Double>>) yieldCurveData.get("yields");

        String insertSQL = "INSERT INTO public.\"YieldData\" (date, maturity, yield) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {

            connection.setAutoCommit(false);

            for (int i = 0; i < allDates.size(); i++) {

                LocalDate date = LocalDate.parse(allDates.get(i).substring(0,10));
                List<Double> yieldsForDate = yields.get(i);

                for (int j = 0; j < maturities.size(); j++) {
                    String maturity = maturities.get(j);
                    Double yieldAsDouble = yieldsForDate.get(j);
                    if (yieldAsDouble == null) continue;
                    Float yield = yieldAsDouble.floatValue();

                    pstmt.setDate(1, java.sql.Date.valueOf(date));
                    pstmt.setString(2, maturity);
                    pstmt.setFloat(3, yield);

                    pstmt.addBatch();
                }

                if (i % 1000 == 0) {
                    pstmt.executeBatch();
                    connection.commit();
                }
            }
            pstmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void mergeData(Map<String, Object> fileData) {
        List<String> currentDates = (List<String>) yieldCurveData.getOrDefault("dates", new ArrayList<>());
        List<String> newDates = (List<String>) fileData.get("dates");
        currentDates.addAll(newDates);
        yieldCurveData.put("dates", currentDates);

        List<List<Float>> currentYields = (List<List<Float>>) yieldCurveData.getOrDefault("yields", new ArrayList<>());
        List<List<Float>> newYields = (List<List<Float>>) fileData.get("yields");
        currentYields.addAll(newYields);
        yieldCurveData.put("yields", currentYields);
    }
}
