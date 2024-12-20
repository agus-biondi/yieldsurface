package com.portfolio.yieldcurve.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.portfolio.yieldcurve.repository.YieldDataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TreasuryDataUpdaterService {

    @Autowired
    private YieldDataRepository yieldDataRepository;

    //First date available from treasury
    private static final LocalDate DEFAULT_START_DATE = LocalDate.of(1990, 1, 1);
    private static final String BASE_URL = "https://home.treasury.gov/resource-center/data-chart-center/interest-rates/pages/xml?data=daily_treasury_yield_curve&field_tdr_date_value_month=";

    private static final List<String> YIELD_KEYS = List.of(
            "BC_1MONTH", "BC_2MONTH", "BC_3MONTH", "BC_4MONTH", "BC_6MONTH",
            "BC_1YEAR", "BC_2YEAR", "BC_3YEAR", "BC_5YEAR", "BC_7YEAR", "BC_10YEAR", "BC_20YEAR", "BC_30YEAR"
    );

    private static final Map<String, String> XML_YIELD_KEYS_TO_HUMAN_READABLE_NAMES = Map.ofEntries(
            Map.entry("BC_1MONTH", "1M"),
            Map.entry("BC_2MONTH", "2M"),
            Map.entry("BC_3MONTH", "3M"),
            Map.entry("BC_4MONTH", "4M"),
            Map.entry("BC_6MONTH", "6M"),
            Map.entry("BC_1YEAR", "1Y"),
            Map.entry("BC_2YEAR", "2Y"),
            Map.entry("BC_3YEAR", "3Y"),
            Map.entry("BC_5YEAR", "5Y"),
            Map.entry("BC_7YEAR", "7Y"),
            Map.entry("BC_10YEAR", "10Y"),
            Map.entry("BC_20YEAR", "20Y"),
            Map.entry("BC_30YEAR", "30Y")
    );

    public void updateDataFromTreasury() {
        LocalDate mostRecentUpdate = yieldDataRepository.getMostRecentUpdateDate().orElse(DEFAULT_START_DATE);
        List<String> requestUrls = getRequestUrls(mostRecentUpdate);

        RestTemplate restTemplate = new RestTemplate();
        XmlMapper xmlMapper = new XmlMapper();

        List<Object[]> currentBatch = new ArrayList<>();

        int currentXMLInProcess = 1;
        int softBatchSizeLimit = 3000;

        for (String requestUrl : requestUrls) {
            System.out.println(String.format("Processing URL %d/%d. %s", currentXMLInProcess, requestUrls.size(), requestUrl));
            String xmlContent = restTemplate.getForObject(requestUrl, String.class);

            currentBatch.addAll(getNewRowsFromXMLContent(xmlMapper, xmlContent, requestUrl, mostRecentUpdate));

            if (currentBatch.size() > softBatchSizeLimit) {
                System.out.println("Saving batch of size: " + currentBatch.size());
                yieldDataRepository.saveYieldCurveData(currentBatch);
                currentBatch.clear();
            }
            currentXMLInProcess++;
        }
        if (!currentBatch.isEmpty()) {
            yieldDataRepository.saveYieldCurveData(currentBatch);
        }
    }

    private List<Object[]> getNewRowsFromXMLContent(XmlMapper xmlMapper, String xmlContent, String requestUrl, LocalDate mostRecentUpdate) {
        List<Object[]> newRows = new ArrayList<>();
        try {
            JsonNode rootNode = xmlMapper.readTree(xmlContent);
            JsonNode entries = rootNode.path("entry");

            entries.forEach(entry -> {
                JsonNode properties = entry.path("content").path("properties");

                String dateString = properties.path("NEW_DATE").path("").asText();
                LocalDate date = LocalDate.parse(dateString.substring(0,10));

                if (date.isBefore(mostRecentUpdate) || date.isEqual(mostRecentUpdate)) return;

                YIELD_KEYS.forEach(maturity -> {
                    Float yield = getYield(properties, maturity);
                    newRows.add(new Object[]{date, XML_YIELD_KEYS_TO_HUMAN_READABLE_NAMES.get(maturity), yield});
                });
            });
        } catch (IOException e) {
            System.err.println("Error processing URL: " + requestUrl);
            e.printStackTrace();
        }
        return newRows;
    }
    private Float getYield(JsonNode properties, String fieldName) {
        JsonNode fieldNode = properties.path(fieldName).path("");
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return null;
        }
        try {
            return (float)fieldNode.asDouble();
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format for " + fieldNode.toPrettyString());
            return null;
        }
    }

    private List<String> getRequestUrls(LocalDate mostRecentUpdate) {

        List<String> requestUrls = new ArrayList<>();
        LocalDate todaysDate = LocalDate.now();
        LocalDate currentDate = mostRecentUpdate;

        while(currentDate.getYear() < todaysDate.getYear() ||
                (currentDate.getYear() == todaysDate.getYear() && currentDate.getMonthValue() <= todaysDate.getMonthValue())) {
            requestUrls.add(buildRequestUrl(currentDate));
            currentDate = currentDate.plusMonths(1);
        }

        return requestUrls;
    }

    private String buildRequestUrl(LocalDate currentDate) {
        String year = String.valueOf(currentDate.getYear());
        String month = String.format("%02d", currentDate.getMonthValue());
        return new StringBuilder(BASE_URL)
                .append(year)
                .append(month)
                .toString();
    }
}
