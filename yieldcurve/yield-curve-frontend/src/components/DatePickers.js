import React, { useState } from "react";
import { formatDate, isValidDate, getTimeWindowDuration } from "../utils/utils";
import { appStyles } from "../styles/styles";

const DatePickers = ({ startDate, endDate, setEndDate, timeWindow }) => {
  const minDate = new Date("1990-01-01");
  const maxDate = new Date();

  const minDateF = formatDate(minDate);
  const maxDateF = formatDate(maxDate);

  const handleEndDateBlur = (e) => {
  	const newEndDate = new Date(e.target.value);
  	if (isValidDate(newEndDate) && newEndDate >= minDate && newEndDate <= maxDate) {
      const newStartDate = new Date(newEndDate - getTimeWindowDuration(timeWindow));

      if (isValidDate(newStartDate) && newStartDate >= minDate) {
        setEndDate(newEndDate);
      } else {
        console.warn("Invalid start date calculated from the end date.");
      }
    } else {
      console.warn("Selected end date is outside the valid range.");
    }
  }
  return (
    <div style={appStyles.datePickerContainer}>
      <div>
        <label style={appStyles.datePickerLabel}>Start Date</label>
        <input
          type="date"
          defaultValue={formatDate(startDate)}
          readOnly
          title="Start date is calculated based on the End Date and Time Window."
          style={{
            ...appStyles.datePickerInput,
            backgroundColor: "#D3D3D3",
            cursor: "not-allowed",
          }}
        />
      </div>
      <div>
        <label style={appStyles.datePickerLabel}>End Date</label>
        <input
          type="date"
          defaultValue={formatDate(endDate)}
		  min={minDateF}
          max={maxDateF}
          onBlur={handleEndDateBlur}
          style={appStyles.datePickerInput}
        />
      </div>
    </div>
  );
};

export default DatePickers;
