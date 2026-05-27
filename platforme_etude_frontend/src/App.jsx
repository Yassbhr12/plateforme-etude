import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AppLayout from './components/layout/AppLayout';

import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import Matieres from './pages/Matieres';
import Sessions from './pages/Sessions';
import Objectifs from './pages/Objectifs';
import Groupes from './pages/Groupes';
import GroupeChat from './pages/GroupeChat';
import Disponibilites from './pages/Disponibilites';
import Notifications from './pages/Notifications';
import Profil from './pages/Profil';
import Admin from './pages/Admin';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          {/* Protected routes with AppLayout */}
          <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/matieres" element={<Matieres />} />
            <Route path="/sessions" element={<Sessions />} />
            <Route path="/objectifs" element={<Objectifs />} />
            <Route path="/groupes" element={<Groupes />} />
            <Route path="/groupes/:id/chat" element={<GroupeChat />} />
            <Route path="/disponibilites" element={<Disponibilites />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/profil" element={<Profil />} />
            <Route
              path="/admin"
              element={
                <ProtectedRoute requireAdmin>
                  <Admin />
                </ProtectedRoute>
              }
            />
          </Route>

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
