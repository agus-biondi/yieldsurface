import React from "react";
import { formatDate } from "../utils/utils";
import { appStyles } from "../styles/styles";


const MIN_DATE = new Date("1990-01-01");
const MAX_DATE = new Date();

const DatePickers = ({ startDate, endDate, onEndDateChange, timeWindow }) => {


  const handleEndDateChange = (e) => {
  	const newEndDate = new Date(e.target.value);

  	if (newEndDate > MAX_DATE) {
  		onEndDateChange(MAX_DATE);
  		return;
  	}

	onEndDateChange(newEndDate);
  };

  return (
    <div style={appStyles.datePickerContainer}>
      <div>
        <label style={appStyles.datePickerLabel}>Start Date</label>
        <input
          type="date"
          value={formatDate(startDate)}
          title="Start date is calculated based on the End Date and Time Window."
		  readOnly
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
		  min={formatDate(MIN_DATE)}
          max={formatDate(MAX_DATE)}
          onBlur={handleEndDateChange}
          style={appStyles.datePickerInput}
        />
      </div>
    </div>
  );
};

export default DatePickers;
