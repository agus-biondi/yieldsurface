package com.portfolio.yieldcurve.controller;

import com.portfolio.yieldcurve.service.YieldCurveDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class Controller {


    @Autowired
    private YieldCurveDataService yieldCurveDataService;

    @GetMapping("/")
    public String healthCheck() {
        return "Server up!";
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


}
