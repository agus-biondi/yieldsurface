package com.portfolio.yieldcurve.controller;

import com.portfolio.yieldcurve.service.ConvertJsonStorage;
import com.portfolio.yieldcurve.service.TreasuryXmlToJsonService;
import com.portfolio.yieldcurve.service.YieldCurveDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {

    @Autowired
    private TreasuryXmlToJsonService xmlToJsonService;
    @Autowired
    private YieldCurveDataService yieldCurveDataService;
    @Autowired
    private ConvertJsonStorage convertJsonStorage;

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
    public ResponseEntity<Map<String, Object>> getYieldCurveData(
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate,
            @RequestParam("group_by") String groupBy
    ) {
        Map<String, Object> yieldCurveData = yieldCurveDataService.getYieldCurveData(startDate, endDate, groupBy);
        return ResponseEntity.ok(yieldCurveData);
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
