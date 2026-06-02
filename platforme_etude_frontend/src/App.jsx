import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminRoute from './components/AdminRoute';
import AppLayout from './components/layout/AppLayout';
import AdminLayout from './components/layout/AdminLayout';

import Login from './pages/Login';
import Signup from './pages/Signup';
import ForgotPassword from './pages/ForgotPassword';
import Dashboard from './pages/Dashboard';
import Matieres from './pages/Matieres';
import Sessions from './pages/Sessions';
import Objectifs from './pages/Objectifs';
import Groupes from './pages/Groupes';
import GroupeChat from './pages/GroupeChat';
import Disponibilites from './pages/Disponibilites';
import Notifications from './pages/Notifications';
import Profil from './pages/Profil';
import Unauthorized from './pages/Unauthorized';
import NotFound from './pages/NotFound';

import AdminDashboard from './pages/admin/AdminDashboard';
import AdminUsers from './pages/admin/AdminUsers';
import AdminStats from './pages/admin/AdminStats';
import AdminSessions from './pages/admin/AdminSessions';
import AdminGroupes from './pages/admin/AdminGroupes';
import AdminNotifications from './pages/admin/AdminNotifications';
import AdminData from './pages/admin/AdminData';
import AdminSettings from './pages/admin/AdminSettings';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* Protected routes with AppLayout (students) */}
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
          </Route>

          {/* Admin routes with AdminLayout */}
          <Route element={<AdminRoute><AdminLayout /></AdminRoute>}>
            <Route path="/admin/dashboard" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<AdminUsers />} />
            <Route path="/admin/stats" element={<AdminStats />} />
            <Route path="/admin/sessions" element={<AdminSessions />} />
            <Route path="/admin/groupes" element={<AdminGroupes />} />
            <Route path="/admin/notifications" element={<AdminNotifications />} />
            <Route path="/admin/data" element={<AdminData />} />
            <Route path="/admin/settings" element={<AdminSettings />} />
          </Route>

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
