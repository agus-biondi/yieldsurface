package com.portfolio.yieldcurve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ConvertJsonStorage {

    private String yieldCurveDataFolderPath = "json/yieldcurvedata/";

    public void convertAllFiles() {
        for (int year = 1990; year <= 2024; year++) {
            convertOneFile(year, yieldCurveDataFolderPath + year + ".json");
        }
    }

    private void convertOneFile(int year, String filePath) {
        Resource resource = new ClassPathResource(filePath);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> oldJsonMap;

        try {
            oldJsonMap = mapper.readValue(resource.getInputStream(), Map.class);
            System.out.println(oldJsonMap.toString());
        } catch (IOException e) {
            System.out.println(e);
            return;
        }

        Map<String, Object> finalOutput = new HashMap<>();

        List<String> dates = new ArrayList<>();
        List<List<Float>> yields = new ArrayList<>();

        finalOutput.put("maturities", new String[]{"1M", "2M", "3M", "4M", "6M", "1Y", "5Y", "10Y", "30Y"});
        finalOutput.put("dates", dates);
        finalOutput.put("yields", yields);

        for (Map.Entry<String, Object> entry : oldJsonMap.entrySet()) {
            if (entry.getKey().equals("last_updated")) {
                finalOutput.put(entry.getKey(), entry.getValue());
                continue;
            } else if (entry.getKey().equals("last_date_entry")) {
                continue;
            }
            String date = entry.getKey();
            dates.add(date);

            Map<String, Double> maturityYieldMap = (Map<String, Double>)entry.getValue();
            List<Float> yieldsForDate = new ArrayList<>();
            for (Map.Entry<String, Double> yieldEntry : maturityYieldMap.entrySet()) {
                if (yieldEntry.getKey().equals("5_month")) {
                    continue;
                }
                Double yield = yieldEntry.getValue();
                yieldsForDate.add(yield != null ? yield.floatValue() : null);

            }
            yields.add(yieldsForDate);

        }

        saveJsonToFile(finalOutput, yieldCurveDataFolderPath + year + ".json");
    }

    private void saveJsonToFile(Map<String, Object> extractedData, String filePath){
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(extractedData);
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, jsonContent.getBytes());
        } catch (IOException e) {

        }

    }


}
