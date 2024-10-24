package com.portfolio.yieldcurve.controller;

import com.portfolio.yieldcurve.service.TreasuryXmlToJsonService;
import com.portfolio.yieldcurve.service.YieldCurveDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class Controller {

    private TreasuryXmlToJsonService xmlToJsonService;
    private YieldCurveDataService yieldCurveDataService;

    public Controller(TreasuryXmlToJsonService xmlToJsonService, YieldCurveDataService yieldCurveDataService) {
        this.xmlToJsonService = xmlToJsonService;
        this.yieldCurveDataService = yieldCurveDataService;
    }

    @GetMapping("/")
    public String healthCheck() {
        return "Server up!";
    }

    @GetMapping("/api/v1/test")
    public void loadYieldCurveIntoMemory() {
        return;
    }

    @GetMapping("/api/v1/yield-curve-data")
    public ResponseEntity<Map<String, Object>> getYieldCurveData() {
        System.out.println("request for yield curve data");
        // Create the response structure
        Map<String, Object> response = new HashMap<>();

        // Add maturities array
        response.put("maturities", new String[]{"1_month", "3_month", "6_month", "1_year", "5_year", "10_year", "30_year"});

        // Add dates array
        response.put("dates", new String[]{"2024-10-01", "2024-10-02", "2024-10-03"});

        // Add yields array (yields for each date)
        double[][] yields = {
                {0.02, 0.03, 0.04, 0.05, 0.1, 0.2, 0.3},  // yields for '2024-10-01'
                {0.02, 0.03, 0.04, 0.05, 0.1, 0.2, 0.3},  // yields for '2024-10-02'
                {0.03, 0.04, 0.05, 0.06, 0.11, 0.21, 0.31} // yields for '2024-10-03'
        };
        response.put("yields", yields);

        return ResponseEntity.ok(response);
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
