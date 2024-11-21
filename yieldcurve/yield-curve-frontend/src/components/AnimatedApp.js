import React, { useEffect, useState } from "react";
import Plot from "react-plotly.js";

const AnimatedApp = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0); // Start index for the sliding window
  const windowSize = 30; // Number of days to show

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

  useEffect(() => {
    // Automatically slide the window of data every 2 seconds
    const interval = setInterval(() => {
      setCurrentIndex((prevIndex) => {
        // Slide forward until the end of the data
        const nextIndex = prevIndex + 1;
        if (nextIndex + windowSize >= data.dates.length) {
          return 0; // Loop back to the start
        }
        return nextIndex;
      });
    }, 1);

    return () => clearInterval(interval); // Cleanup the interval on unmount
  }, [data]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!data) {
    return <div>No data available</div>;
  }

  const { maturities, dates, yields } = data;

  // Get the current sliding window of dates and yields
  const windowDates = dates.slice(currentIndex, currentIndex + windowSize);
  const windowYields = yields.slice(currentIndex, currentIndex + windowSize);

  return (
    <div style={{ width: "100vw", height: "100vh", overflow: "hidden" }}>
      <h1>3D Yield Curve</h1>
      <Plot
        data={[
          {
            x: maturities, // Maturity periods for x-axis
            y: windowDates, // Sliding window of dates for y-axis (time axis)
            z: windowYields, // Sliding window of yield values for z-axis
            type: "surface",
            contours: {
              z: {
                show: true,
                usecolormap: true,
                highlightcolor: "#42f462",
                project: { z: true },
              },
            }, // 3D surface type
          },
        ]}
        layout={{
          title: "Yield Curve Over Time",
          scene: {
            xaxis: { title: "Maturity" },
            yaxis: {
              title: "Date",
              tickformat: "%Y-%m-%d",
            },
            zaxis: {
              title: "Yield",
            },
          },
          autosize: true, // Automatically resize the chart
          margin: { l: 65, r: 50, b: 65, t: 90 },
          transition: {
            duration: 0, // Smooth animation
            easing: "linear",
          },
        }}
        useResizeHandler={true}
        style={{ width: "100%", height: "100%" }}
      />
    </div>
  );
};

export default AnimatedApp;
