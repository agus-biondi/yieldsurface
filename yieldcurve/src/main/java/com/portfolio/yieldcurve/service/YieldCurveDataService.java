package com.portfolio.yieldcurve.service;

import com.portfolio.yieldcurve.repository.YieldDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class YieldCurveDataService {

    @Autowired
    private YieldDataRepository yieldDataRepository;

    /**
     * Retrieves yield curve data grouped by the specified time interval.
     *
     * @param startDate Start date in YYYY-MM-DD format.
     * @param endDate   End date in YYYY-MM-DD format.
     * @param groupBy   Grouping period: day, week, month, or year.
     * @return A map containing dates, maturities, and yields.
     */
    public Map<String, Object> getYieldCurveData(String startDate, String endDate, String groupBy) {
        Map<String, Object> yieldCurveData = new HashMap<>();

        List<String> maturities = List.of("1M", "2M", "3M", "4M", "6M", "1Y", "2Y", "3Y", "5Y", "7Y", "10Y", "20Y", "30Y");

        Map<String, List<Float>> yieldDataByDate = yieldDataRepository.getYieldCurveData(startDate, endDate, groupBy);
        List<String> dates = new ArrayList<>(yieldDataByDate.keySet());
        List<List<Float>> yields = new ArrayList<>(yieldDataByDate.values());

        yieldCurveData.put("dates", dates);
        yieldCurveData.put("maturities", maturities);
        yieldCurveData.put("yields", yields);
        return yieldCurveData;
    }
}
