import React, { useState, useEffect } from "react";
import { formatDate, isValidDate, getTimeWindowDuration  } from "../utils/utils.js";
import { appStyles } from "../styles/styles.js";

import ControlGroup from "./ControlGroup";
import DatePickers from "./DatePickers";
import YieldSurfacePlotComponent from "./YieldSurfacePlotComponent";

const OriginalApp = () => {
  const urlParams = new URLSearchParams(window.location.search);
  const [groupBy, setGroupBy] = useState(urlParams.get("groupBy") || "Week");
  const [timeWindow, setTimeWindow] = useState(urlParams.get("timeWindow") || "1Y");
  const [currentDate, setCurrentDate] = useState(
  	isValidDate(new Date(urlParams.get("endDate"))) ?  new Date(urlParams.get("endDate")) : new Date()
  );

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const updateURL = () => {
    const params = new URLSearchParams();
    params.set("groupBy", groupBy);
    params.set("timeWindow", timeWindow);
    params.set("endDate", formatDate(currentDate));
    window.history.replaceState({}, "", `?${params.toString()}`);
  };

  const fetchData = () => {

    if (!isValidDate(currentDate)) {
    	return;
    }

    setLoading(true);
    setError(null);

    const endDate = formatDate(currentDate);
    const startDate = formatDate(new Date(currentDate - getTimeWindowDuration(timeWindow)));

    fetch(
      `http://localhost:8080/api/v1/yield-curve-data?start_date=${startDate}&end_date=${endDate}&group_by=${groupBy}`
    )
      .then((response) => {
        if (!response.ok) throw new Error("Failed to fetch data");
        return response.json();
      })
      .then((responseData) => {
        setData(responseData);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message);
        setLoading(false);
      });
  };

  useEffect(() => {
    updateURL();
    fetchData();
  }, [groupBy, timeWindow, currentDate]);

  const { maturities, dates, yields } = data || {};

  return (
    <div style={appStyles.container}>
      <header style={appStyles.header}>Treasury Yield Curve Surface</header>

      {/* Controls Section */}
      <div style={appStyles.controlSection}>
        <ControlGroup
          label="Group By"
          options={["Day", "Week", "Month", "Year"]}
          selected={groupBy}
          onSelect={setGroupBy}
        />
        <ControlGroup
          label="Time Window"
          options={["1W", "1M", "3M", "1Y", "3Y", "5Y", "10Y"]}
          selected={timeWindow}
          onSelect={setTimeWindow}
        />
        <DatePickers
          startDate={new Date(currentDate - getTimeWindowDuration(timeWindow))}
          endDate={currentDate}
          setEndDate={(newEndDate) => {
			  if (isValidDate(newEndDate)) {
				setCurrentDate(newEndDate);
			  } else {
				console.warn("Invalid end date selected.");
			  }
			}}
			timeWindow={timeWindow}
        />
      </div>

      {/* Plot Section */}
      <div style={appStyles.plotSection}>
        {loading ? (
          <div style={appStyles.spinnerOverlay}>
            <div style={appStyles.spinner}></div>
          </div>
        ) : (
          <YieldSurfacePlotComponent
            maturities={maturities}
            dates={dates}
            yields={yields}
          />
        )}
      </div>
    </div>
  );
};

export default OriginalApp;
