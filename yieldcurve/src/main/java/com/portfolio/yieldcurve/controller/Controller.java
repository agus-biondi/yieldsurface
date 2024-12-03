package com.portfolio.yieldcurve.controller;

import com.portfolio.yieldcurve.service.YieldCurveDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
public class Controller {

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    private static final List<String> VALID_GROUP_BY_OPTIONS = List.of("day", "week", "month", "year");

    @Autowired
    private YieldCurveDataService yieldCurveDataService;

    @RequestMapping(value = "/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }

    /**
     * Endpoint to retrieve yield curve data.
     *
     * @param startDate Start date in YYYY-MM-DD format.
     * @param endDate   End date in YYYY-MM-DD format.
     * @param groupBy   Grouping period: day, week, month, or year.
     * @return A response entity containing the yield curve data or an error message.
     */
    @GetMapping("/api/v1/yield-curve-data")
    public ResponseEntity<Map<String, Object>> getYieldCurveData(
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate,
            @RequestParam("group_by") String groupBy
    ) {
        LOGGER.info("Yield curve data request received: start_date={}, end_date={}, group_by={}", startDate, endDate, groupBy);

        if (!validateGroupBySelection(groupBy)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid 'group_by' parameter",
                    "valid_options", VALID_GROUP_BY_OPTIONS
            ));
        }

        if (!validateDateSelection(startDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid 'startDate' parameter",
                    "valid_options", "YYYY-MM-DD formatted date"
            ));
        }
        if (!validateDateSelection(endDate)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid 'endDate' parameter",
                    "valid_options", "YYYY-MM-DD formatted date"
            ));
        }

        Map<String, Object> yieldCurveData = yieldCurveDataService.getYieldCurveData(startDate, endDate, groupBy);

        LOGGER.info("Returning records to user");
        return ResponseEntity.ok(yieldCurveData);
    }

    /**
     * Validates the 'group_by' parameter.
     *
     * @param groupBy The group_by parameter to validate.
     * @return True if valid, false otherwise.
     */
    private boolean validateGroupBySelection(String groupBy) {
        if (!VALID_GROUP_BY_OPTIONS.contains(groupBy.toLowerCase())) {
            LOGGER.warn("Invalid group_by parameter: {}", groupBy);
            return false;
        }
        return true;
    }

    /**
     * Validates a date string in the format YYYY-MM-DD.
     *
     * @param date The date string to validate.
     * @return True if valid, false otherwise.
     */
    private boolean validateDateSelection(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            LOGGER.warn("Invalid date parameter: {}", date, e);
            return false;
        }
    }


}
