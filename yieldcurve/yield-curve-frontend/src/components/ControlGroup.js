import React from "react";
import SelectableButton from "./SelectableButton";

const ControlGroup = ({ label, options, selected, onSelect }) => (
  <div>
    <label
      style={{
        fontWeight: "bold",
        marginBottom: "10px",
        display: "block",
        textAlign: "center",
      }}
    >
      {label}
    </label>
    <div
      style={{
        backgroundColor: "#E6F4EA",
        padding: "10px",
        borderRadius: "8px",
        display: "flex",
        gap: "10px",
        justifyContent: "center",
      }}
    >
      {options.map((option) => (
        <SelectableButton
          key={option}
          isSelected={selected === option}
          onClick={() => onSelect(option)}
          label={option}
          selectedColor="#28A745"
          hoverBackground="#D4E8D9"
          hoverColor="#1E7B34"
        />
      ))}
    </div>
  </div>
);

export default ControlGroup;
