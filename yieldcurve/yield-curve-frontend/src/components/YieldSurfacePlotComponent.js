import React from "react";
import Plot from "react-plotly.js";

const YieldSurfacePlotComponent = ({ maturities, dates, yields }) => {
  return (
    <div style={{ flex: 1, margin: "20px" }}>
      <Plot
        data={[
          {
            x: maturities,
            y: dates,
            z: yields,
            type: "surface",
            opacity: 0.5,
            hoverinfo: "none",
            showscale: false,
            contours: {
              x: { highlight: false },
              y: { highlight: false },
              z: { highlight: false },
            },
          },
        ]}
        layout={{
          scene: {
            xaxis: { title: "Maturity", showspikes: false },
            yaxis: { title: "Date", tickformat: "%Y-%m-%d", showspikes: false },
            zaxis: { title: "Yield", showspikes: false },
            camera: {
              eye: { x: 1.5, y: 1.5, z: 1.5 },
            },
          },
          autosize: true,
          margin: { l: 0, r: 0, b: 0, t: 0 },
        }}
        useResizeHandler={true}
        style={{ width: "100%", height: "100%" }}
      />
    </div>
  );
};

export default YieldSurfacePlotComponent;
