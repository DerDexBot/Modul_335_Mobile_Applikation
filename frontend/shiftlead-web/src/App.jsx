import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import PlanningPage from './pages/PlanningPage';
import TimePage from './pages/TimePage';
import OrdersPage from './pages/OrdersPage';
import NotesPage from './pages/NotesPage';

function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  return token && role === 'SHIFT_LEAD' ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"     element={<LoginPage />} />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
        <Route path="/planning"  element={<ProtectedRoute><PlanningPage /></ProtectedRoute>} />
        <Route path="/time"      element={<ProtectedRoute><TimePage /></ProtectedRoute>} />
        <Route path="/orders"    element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
        <Route path="/notes"     element={<ProtectedRoute><NotesPage /></ProtectedRoute>} />
        <Route path="/"          element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
