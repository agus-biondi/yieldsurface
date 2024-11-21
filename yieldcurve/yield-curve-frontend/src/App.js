import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import OriginalApp from "./components/OriginalApp";
import AnimatedApp from "./components/AnimatedApp";

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/animated" element={<AnimatedApp />} />
        <Route path="/" element={<OriginalApp/>} />
      </Routes>
    </Router>
  );
};

export default App;

