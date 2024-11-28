import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import OriginalApp from "./components/OriginalApp";

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<OriginalApp/>} />
      </Routes>
    </Router>
  );
};

export default App;

