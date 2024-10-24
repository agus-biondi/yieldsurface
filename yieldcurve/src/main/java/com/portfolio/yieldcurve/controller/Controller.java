package com.portfolio.yieldcurve.controller;

import com.portfolio.yieldcurve.service.TreasuryXmlToJsonService;
import com.portfolio.yieldcurve.service.YieldCurveDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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

    @GetMapping("/")
    public void loadYieldCurveIntoMemory() {

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
