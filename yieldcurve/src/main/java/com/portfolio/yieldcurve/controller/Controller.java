package com.portfolio.yieldcurve.controller;

import com.portfolio.yieldcurve.service.ConvertJsonStorage;
import com.portfolio.yieldcurve.service.TreasuryXmlToJsonService;
import com.portfolio.yieldcurve.service.YieldCurveDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {

    private TreasuryXmlToJsonService xmlToJsonService;
    private YieldCurveDataService yieldCurveDataService;
    private ConvertJsonStorage convertJsonStorage;

    public Controller(TreasuryXmlToJsonService xmlToJsonService, YieldCurveDataService yieldCurveDataService, ConvertJsonStorage convertJsonStorage) {
        this.xmlToJsonService = xmlToJsonService;
        this.yieldCurveDataService = yieldCurveDataService;
        this.convertJsonStorage = convertJsonStorage;
    }

    @GetMapping("/")
    public String healthCheck() {
        return "Server up!";
    }

    @GetMapping("/api/v1/test")
    public void loadYieldCurveIntoMemory() {
        return;
    }

    @GetMapping("/api/v1/convertJsonStorage")
    public void convert() {
        convertJsonStorage.convertAllFiles();
    }

    @GetMapping("/api/v1/yield-curve-data")
    public ResponseEntity<Map<String, Object>> getYieldCurveData() {

        return ResponseEntity.ok(yieldCurveDataService.getYieldCurveData());
/*
        System.out.println("request for yield curve data");
        // Create the response structure
        Map<String, Object> response = new HashMap<>();

        // Add maturities array
        response.put("maturities", new String[]{"1M", "3M", "6M", "1Y", "5Y", "10Y", "30Y"});

        // Add dates array
        response.put("dates", new String[]{"2024-10-01", "2024-10-02", "2024-10-03"});

        // Add yields array (yields for each date)
        List<List<Double>> yields = List.of(
                List.of(4.94, 4.75, 4.44, 4.21, 4.06, 4.27, 4.55),  // yields for '2024-10-01'
                List.of(4.92, 4.73, 4.42, 4.05, 4.01, 4.35, 4.58),  // yields for '2024-10-02'
                List.of(4.89, 4.72, 4.41, 4.03, 4.02, 4.39, 4.61) // yields for '2024-10-03'
        );
        response.put("yields", yields);

        return ResponseEntity.ok(response);*/
    }


    @PostMapping("/download_historical")
    public String downloadHistorical() {
        try {
            xmlToJsonService.downloadHistoricalData();
            return "downloaded data";
        } catch (IOException e) {
            System.out.println(e);
            return e.toString();
        }
    }

}
