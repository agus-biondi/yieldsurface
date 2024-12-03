import React, { useState, useEffect, useMemo } from "react";
import { formatDate, getTimeWindowDuration  } from "../utils/utils.js";
import { appStyles } from "../styles/styles.js";

import ControlGroup from "./ControlGroup";
import DatePickers from "./DatePickers";
import YieldSurfacePlotComponent from "./YieldSurfacePlotComponent";


const OriginalApp = () => {

  const urlParams = new URLSearchParams(window.location.search);

  const [state, setState] = useState({
  	groupBy: urlParams.get("groupBy") || "Week",
  	timeWindow: urlParams.get("timeWindow") || "1Y",
  	currentDate: (() => {
  		const urlEndDate = urlParams.get('endDate');
  		return urlEndDate && !isNaN(new Date(urlEndDate))
  			? new Date(urlEndDate)
  			: new Date();
  	})(),
  	data: null,
  	loading: false,
  	error: null
  });

  const { groupBy, timeWindow, currentDate, data, loading } = state;

  const startDate = useMemo(() => {
  	const calculatedStartDate = new Date(currentDate - getTimeWindowDuration(timeWindow));
	return calculatedStartDate < new Date('1990-01-01')
	  ? new Date('1990-01-01')
	  : calculatedStartDate;
  }, [currentDate, timeWindow]);

  const updateURL = () => {
    const params = new URLSearchParams({
      groupBy,
      timeWindow,
      endDate: formatDate(currentDate)
    });
    window.history.replaceState({}, "", `?${params.toString()}`);
  };

  const fetchData = async () => {

    if (!currentDate) return;

    setState(prev => ({ ...prev, loading: true, error: null }));
	let API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

	if (API_BASE_URL === undefined) API_BASE_URL = "http://localhost:8080";
    try {
    	const response = await fetch(
		  //`http://localhost:8080/api/v1/yield-curve-data?start_date=${formatDate(startDate)}&end_date=${formatDate(currentDate)}&group_by=${groupBy}`
	      `${API_BASE_URL}/api/v1/yield-curve-data?start_date=${formatDate(startDate)}&end_date=${formatDate(currentDate)}&group_by=${groupBy}`
	    );

	    if (!response.ok) throw new Error('Failed to fetch data');

	    const responseData = await response.json();
		setState(prev => ({ ...prev, data: responseData, loading: false }));
	  } catch (err) {
	    setState(prev => ({ ...prev, error: err.message, loading: false }));
	  }
    }

    useEffect(() => {
	  updateURL();
	  fetchData();
    }, [groupBy, timeWindow, currentDate]);

    const handleStateUpdate = (updates) => {
    	setState(prev => ({...prev, ...updates}));
    }

  return (
    <div style={appStyles.container}>
      <header style={appStyles.header}>Treasury Yield Curve Surface</header>

      {/* Controls Section */}
      <div style={appStyles.controlSection}>
        <ControlGroup
          label="Group By"
          options={["Day", "Week", "Month", "Year"]}
          selected={groupBy}
          onSelect={(value) => handleStateUpdate({ groupBy: value })}
        />
        <ControlGroup
          label="Time Window"
          options={["1W", "1M", "3M", "1Y", "3Y", "5Y", "10Y"]}
          selected={timeWindow}
          onSelect={(value) => handleStateUpdate({ timeWindow: value })}
        />
        <DatePickers
          startDate={startDate}
          endDate={currentDate}
          timeWindow={timeWindow}
          onEndDateChange={(newEndDate) => handleStateUpdate({ currentDate: newEndDate })}
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
            maturities={data?.maturities}
            dates={data?.dates}
            yields={data?.yields}
          />
        )}
      </div>
    </div>
  );
};

export default OriginalApp;
