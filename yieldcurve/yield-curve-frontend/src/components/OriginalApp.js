import React, { useState, useEffect } from "react";
import { formatDate, getTimeWindowDuration } from "../utils/utils.js";
import { appStyles } from "../styles/styles.js";

import ControlGroup from "./ControlGroup";
import DatePickers from "./DatePickers";
import YieldSurfacePlotComponent from "./YieldSurfacePlotComponent";

const OriginalApp = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false); // Track loading state
  const [error, setError] = useState(null);

  const [groupBy, setGroupBy] = useState("Week");
  const [timeWindow, setTimeWindow] = useState("1Y");
  const [currentDate, setCurrentDate] = useState(new Date());

  const fetchData = () => {
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
    fetchData();
  }, [groupBy, timeWindow, currentDate]);

  if (error) return <div>Error: {error}</div>;

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
          options={["1W", "1M", "3M", "1Y", "3Y", "5Y"]}
          selected={timeWindow}
          onSelect={setTimeWindow}
        />
        <DatePickers
          startDate={new Date(currentDate - getTimeWindowDuration(timeWindow))}
          endDate={currentDate}
          setEndDate={setCurrentDate}
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
