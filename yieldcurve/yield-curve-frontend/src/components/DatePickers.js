import React from "react";
import { formatDate } from "../utils/utils";

import { appStyles } from "../styles/styles";

const DatePickers = ({ startDate, endDate, setEndDate }) => {

  return (
    <div style={appStyles.datePickerContainer}>
      <div>
        <label style={appStyles.datePickerLabel}>Start Date</label>
        <input
          type="date"
          value={formatDate(startDate)}
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
          value={formatDate(endDate)}
          onChange={(e) => setEndDate(new Date(e.target.value))}
          style={appStyles.datePickerInput}
        />
      </div>
    </div>
  );
};

export default DatePickers;
