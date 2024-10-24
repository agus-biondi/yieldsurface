import React, { useEffect, useState } from "react";
import Plot from "react-plotly.js";

const App = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Fetch yield curve data from the API endpoint
    fetch("http://localhost:8080/api/v1/yield-curve-data")
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("Failed to fetch data");
        }
      })
      .then((data) => {
        setData(data);
        setLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching data:", error);
        setLoading(false);
      });
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!data) {
    return <div>No data available</div>;
  }

  const { maturities, dates, yields } = data;

  return (
    <div>
      <h1>3D Yield Curve</h1>
      <Plot
        data={[
          {
            x: maturities, // Maturity periods for x-axis
            y: dates, // Dates for y-axis (time axis)
            z: yields, // Yield values for z-axis
            type: "surface", // 3D surface type
          },
        ]}
        layout={{
          title: "Yield Curve Over Time",
          scene: {
            xaxis: { title: "Maturity" },
            yaxis: { title: "Date" },
            zaxis: { title: "Yield" },
          },
        }}
        style={{ width: "100%", height: "600px" }}
      />
    </div>
  );
};

export default App;

