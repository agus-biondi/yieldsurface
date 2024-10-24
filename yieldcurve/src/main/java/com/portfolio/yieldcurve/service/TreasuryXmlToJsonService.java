package com.portfolio.yieldcurve.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
public class TreasuryXmlToJsonService {

    private final RestTemplate restTemplate;

    public TreasuryXmlToJsonService() {
        this.restTemplate = new RestTemplate();
    }

    public void downloadHistoricalData() throws IOException {

        List<String> xmlUrls = getXmlUrls();
        XmlMapper xmlMapper = new XmlMapper();
        String baseFilePath = "src/main/resources/json/yieldcurvedata/";

        String lastUpdated = LocalDate.now().toString();
        String lastDateEntry = "";
        Map<String, Map<String, Map<String, Double>>> allEntries = new LinkedHashMap<>();

        int count = 0;
        for (String xmlUrl : xmlUrls) {
            if (count > 999999999) {
                break;
            }
            System.out.println(String.format("(%d/%d) urls. %s", count, xmlUrls.size(), xmlUrl));
            String xmlContent = restTemplate.getForObject(xmlUrl, String.class);

            try {
                JsonNode rootNode = xmlMapper.readTree(xmlContent);
                Iterator<JsonNode> entries = rootNode.path("entry").elements();
                while(entries.hasNext()) {
                    JsonNode entry = entries.next();
                    JsonNode properties = entry.path("content").path("properties");

                    String date = properties.findPath("NEW_DATE").get("").asText();
                    lastDateEntry = date;

                    String year = date.split("-")[0];

                    Map<String, Double> yields = new LinkedHashMap<>();
                    yields.put("1_month", getYield(properties, "BC_1MONTH"));
                    yields.put("2_month", getYield(properties, "BC_2MONTH"));
                    yields.put("3_month", getYield(properties, "BC_3MONTH"));
                    yields.put("4_month", getYield(properties, "BC_4MONTH"));
                    yields.put("5_month", getYield(properties, "BC_5MONTH"));
                    yields.put("6_month", getYield(properties, "BC_6MONTH"));
                    yields.put("1_year", getYield(properties, "BC_1YEAR"));
                    yields.put("2_year", getYield(properties, "BC_2YEAR"));
                    yields.put("3_year", getYield(properties, "BC_3YEAR"));
                    yields.put("5_year", getYield(properties, "BC_5YEAR"));
                    yields.put("7_year", getYield(properties, "BC_7YEAR"));
                    yields.put("10_year", getYield(properties, "BC_10YEAR"));
                    yields.put("30_year", getYield(properties, "BC_30YEAR"));

                    allEntries.computeIfAbsent(year, k -> new LinkedHashMap<>()).put(date, yields);
                }
                count++;
            } catch (JsonProcessingException e) {
                throw new IOException("Couldn't parse XML", e);
            }
        }

        // Save each year's data to a separate JSON file
        for (Map.Entry<String, Map<String, Map<String, Double>>> yearEntry : allEntries.entrySet()) {
            String year = yearEntry.getKey();
            Map<String, Map<String, Double>> yearData = yearEntry.getValue();

            // Prepare the output structure
            Map<String, Object> outputJson = new LinkedHashMap<>();
            outputJson.put("last_updated", lastUpdated);
            outputJson.put("last_date_entry", lastDateEntry);
            outputJson.putAll(yearData);

            // Save to file for each year
            saveJsonToFile(outputJson, baseFilePath + year + ".json");
        }


    }

    private List<Map<String, Object>> extractEntries(JsonNode rootNode) {
        List<Map<String, Object>> entriesList = new ArrayList<>();

        // Traverse the XML to find all 'entry' nodes
        Iterator<JsonNode> entries = rootNode.path("entry").elements();

        while (entries.hasNext()) {
            JsonNode entry = entries.next();
            JsonNode properties = entry.path("content").path("properties");

            // Extract relevant data (date and yield rates)
            String date = properties.path("NEW_DATE").asText();

            // Create a map to hold the extracted data
            Map<String, Object> entryData = new HashMap<>();
            entryData.put("date", date);

            // Extract the yield rates, handling cases where certain yields may not exist
            entryData.put("1_month", getYield(properties, "BC_1MONTH"));
            entryData.put("2_month", getYield(properties, "BC_2MONTH"));
            entryData.put("3_month", getYield(properties, "BC_3MONTH"));
            entryData.put("4_month", getYield(properties, "BC_4MONTH"));
            entryData.put("5_month", getYield(properties, "BC_5MONTH"));
            entryData.put("6_month", getYield(properties, "BC_6MONTH"));
            entryData.put("1_year", getYield(properties, "BC_1YEAR"));
            entryData.put("2_year", getYield(properties, "BC_2YEAR"));
            entryData.put("3_year", getYield(properties, "BC_3YEAR"));
            entryData.put("5_year", getYield(properties, "BC_5YEAR"));
            entryData.put("7_year", getYield(properties, "BC_7YEAR"));
            entryData.put("10_year", getYield(properties, "BC_10YEAR"));
            entryData.put("30_year", getYield(properties, "BC_30YEAR"));

            // Add the entry to the list
            entriesList.add(entryData);
        }

        return entriesList;
    }

    private Double getYield(JsonNode properties, String fieldName) {
        JsonNode yieldNode = properties.findPath(fieldName);
        return yieldNode.has("") ? yieldNode.get("").asDouble() : null;
    }

    private void saveJsonToFile(Map<String, Object> extractedData, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(extractedData);

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, jsonContent.getBytes());
    }

    private List<String> getXmlUrls() {

        List<String> xmlUrls = new ArrayList<>();
        String baseString = "https://home.treasury.gov/resource-center/data-chart-center/interest-rates/pages/xml?data=daily_treasury_yield_curve&field_tdr_date_value_month=";
        StringBuilder sb = new StringBuilder(baseString);

        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear();
        int firstYear = 1990;

        for (int year = firstYear; year <= currentYear; year++) {
            int lastMonth = year == currentYear ? currentMonth : 12;
            for (int month = 1; month <= lastMonth; month++) {

                sb.setLength(baseString.length());

                if (month <=9 ) {
                    xmlUrls.add(sb.append(year).append('0').append(month).toString());
                } else {
                    xmlUrls.add(sb.append(year).append(month).toString());
                }

            }
        }

        return xmlUrls;
    }
}
