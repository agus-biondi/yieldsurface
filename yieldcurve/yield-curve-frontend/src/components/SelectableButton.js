const SelectableButton = ({
  isSelected,
  onClick,
  label,
  selectedColor,
  hoverBackground,
  hoverColor,
}) => (
  <button
    onClick={onClick}
    style={{
      margin: "0 5px",
      backgroundColor: isSelected ? selectedColor : "transparent",
      color: isSelected ? "#FFFFFF" : selectedColor,
      border: "none",
      borderRadius: "5px",
      padding: "5px 10px",
      cursor: "pointer",
      transition: "background-color 0.3s ease, color 0.3s ease",
    }}
    onMouseEnter={(e) => {
      if (!isSelected) {
        e.target.style.backgroundColor = hoverBackground;
        e.target.style.color = hoverColor;
      }
    }}
    onMouseLeave={(e) => {
      if (!isSelected) {
        e.target.style.backgroundColor = "transparent";
        e.target.style.color = selectedColor;
      }
    }}
  >
    {label}
  </button>
);

export default SelectableButton;
