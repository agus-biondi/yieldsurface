export const formatDate = (date) => date.toISOString().split("T")[0];

export const getTimeWindowDuration = (timeWindow) => {
  const durations = {
    "1W": 7 * 24 * 60 * 60 * 1000,
    "1M": 30 * 24 * 60 * 60 * 1000,
    "3M": 90 * 24 * 60 * 60 * 1000,
    "1Y": 365 * 24 * 60 * 60 * 1000,
    "3Y": 3 * 365 * 24 * 60 * 60 * 1000,
    "5Y": 5 * 365 * 24 * 60 * 60 * 1000,
  };
  return durations[timeWindow] || 0;
};
