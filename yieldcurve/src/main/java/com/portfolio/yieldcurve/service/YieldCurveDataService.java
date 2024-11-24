package com.portfolio.yieldcurve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class YieldCurveDataService {

    private String yieldCurveDataFolderPath = "json/yieldcurvedata2/";
    private Map<String, Object> yieldCurveData;


    public YieldCurveDataService() {

        ObjectMapper mapper = new ObjectMapper();
        yieldCurveData = new HashMap<>();
        int currentYear = Year.now().getValue();

        for (int year = 2023; year <= currentYear; year++) {
            Resource resource = new ClassPathResource(yieldCurveDataFolderPath + year + ".json");
            try {
                Map<String, Object> fileData = mapper.readValue(resource.getInputStream(), Map.class);
                mergeData(fileData);
            } catch (IOException e) {
                System.out.println("Error loading file: " + year + "\n" + e);
            }
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

    public Map<String, Object> getYieldCurveData(String startDate, String endDate, String groupBy) {

        List<String> allDates = (List<String>) yieldCurveData.get("dates");
        List<String> maturities = (List<String>) yieldCurveData.get("maturities");
        List<List<Float>> yields = (List<List<Float>>) yieldCurveData.get("yields");

        //TODO binary search
        List<LocalDate> filteredDates = filterDates(allDates, startDate, endDate);

        List<Integer> filteredIndices = filteredDates.stream()
                .map(allDates::indexOf)
                .collect(Collectors.toList());

        List<List<Float>> filteredYields = filteredIndices.stream()
                .map(yields::get)
                .collect(Collectors.toList());

        List<LocalDate> groupedDates = groupDates(filteredDates, groupBy);
        List<List<Float>> groupedYields = averageYields(filteredYields, groupedDates, filteredDates);

        return Map.of(
                "dates", groupedDates,
                "maturities", maturities,
                "yields", groupedYields
        );
    }

    private List<List<Float>> averageYields(List<List<Float>> filteredYields, List<LocalDate> groupedDates, List<LocalDate> filteredDates) {
        List<List<Float>> groupedYields = new ArrayList<>();
        /*  yields        filteredDates   groupedDates
            3,4,5,6,7     01              01
            3,4,5,6,7     02              03
            ---------
            4,5,6,7,8     03
            4,5,6,7,8     04

            Grouped Dates
            01, 03
         */

        int currentDateIndex = 0;
        int currentGroupedDateIndex = 1;
        LocalDate currentDate = filteredDates.get(currentDateIndex);

        while(currentDateIndex < filteredDates.size() - 1) {
            float[] currentRangeCount = new float[filteredYields.size()];
            float[] currentRangeSum = new float[filteredYields.size()];

            LocalDate nextDate = groupedDates.get(currentDateIndex + 1);
            List<Float> currentYieldForDate = filteredYields.get(currentDateIndex);
            while(currentDate.isBefore(nextDate)) {
                for (int i = 0; i < currentRangeSum.length; i++) {

                }

                currentDateIndex++;
                currentDate = groupedDates.get(currentDateIndex);

            }

            List<Float> currentRangeYields = new ArrayList<>();

            // add avg to currentRangeYields
            groupedYields.add(currentRangeYields);

        }

        return groupedYields;
    }
    private List<LocalDate> groupDates(List<LocalDate> filteredDates, String groupBy) {

        if (groupBy.equals("Day")) {
            return filteredDates;
        }
        TemporalUnit unit;
        List<LocalDate> groupedDates = new ArrayList<>();

        //2024-01-01, 2024-01-02, 2024-01-5, 2024-01-08, 2024-01-10
        if (groupBy.equals("Week")) {
            unit = ChronoUnit.WEEKS;
        } else if (groupBy.equals("Month")) {
            unit = ChronoUnit.MONTHS;
        } else if (groupBy.equals("Year")) {
            unit = ChronoUnit.YEARS;
        }

        LocalDate currentDate = filteredDates.getFirst();
        LocalDate lastDate =  filteredDates.getLast();

        groupedDates.add(currentDate);
        while(currentDate.isBefore(lastDate) || currentDate.isEqual(lastDate)) {
            currentDate = currentDate.plus(1, unit);
            groupedDates.add(currentDate);
        }

        return groupedDates;
    }
    private List<LocalDate> filterDates(List<String> allDates, String startDate, String endDate) {
        return allDates.stream()
                .filter(date -> date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0)
                .map(LocalDate::parse)
                .collect(Collectors.toList());
    }

}
