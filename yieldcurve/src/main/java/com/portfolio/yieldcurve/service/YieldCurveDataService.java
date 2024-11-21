package com.portfolio.yieldcurve.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class YieldCurveDataService {

    private String yieldCurveDataFolderPath = "json/yieldcurvedata2/";

    private List<String> maturities;
    private List<String> dates;
    private List<Float> yields;

    private Map<String, Object> yieldCurveData;

    public YieldCurveDataService() {
        maturities = new ArrayList<>();
        dates = new ArrayList<>();
        yields  = new ArrayList<>();

        Resource resource = new ClassPathResource(yieldCurveDataFolderPath + "2024.json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            yieldCurveData = mapper.readValue(resource.getInputStream(), Map.class);
            System.out.println(yieldCurveData.toString());
        } catch (IOException e) {
            System.out.println(e);
        }


    }

    public Map<String, Object> getYieldCurveData() {
        return yieldCurveData;
    }

}
