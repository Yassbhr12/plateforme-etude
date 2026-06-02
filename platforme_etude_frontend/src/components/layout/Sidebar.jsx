import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { createElement } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  BookOpen,
  CalendarClock,
  Target,
  Users,
  Clock,
  Bell,
  User,
  LogOut,
  ShieldCheck,
  GraduationCap,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';
import { useState } from 'react';
import './Sidebar.css';

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/matieres', icon: BookOpen, label: 'Matières' },
  { to: '/sessions', icon: CalendarClock, label: 'Sessions' },
  { to: '/objectifs', icon: Target, label: 'Objectifs' },
  { to: '/groupes', icon: Users, label: "Groupes d'étude" },
  { to: '/disponibilites', icon: Clock, label: 'Disponibilités' },
  { to: '/notifications', icon: Bell, label: 'Notifications' },
  { to: '/profil', icon: User, label: 'Profil' },
];

export default function Sidebar({ mobileOpen = false, onNavigate = () => {} }) {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const isOnAdminPage = location.pathname.startsWith('/admin');

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <aside className={`sidebar ${collapsed ? 'sidebar--collapsed' : ''} ${mobileOpen ? 'sidebar--mobile-open' : ''}`}>
      <div className="sidebar__brand">
        <div className="sidebar__logo">
          <GraduationCap />
        </div>
        {!collapsed && (
          <div className="sidebar__brand-text">
            <span className="sidebar__brand-name">Platforme Étude</span>
            <span className="sidebar__brand-sub">Espace collaboratif</span>
          </div>
        )}
      </div>

      <button
        className="sidebar__toggle"
        onClick={() => setCollapsed(!collapsed)}
        aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
      >
        {collapsed ? <ChevronRight /> : <ChevronLeft />}
      </button>

      <nav className="sidebar__nav">
        <ul className="sidebar__list">
          {NAV_ITEMS.map(({ to, icon, label }) => (
            <li key={to}>
              <NavLink
                to={to}
                onClick={onNavigate}
                className={({ isActive }) => `sidebar__link ${isActive ? 'sidebar__link--active' : ''}`}
                title={collapsed ? label : undefined}
              >
                {createElement(icon, { className: 'sidebar__link-icon' })}
                {!collapsed && <span className="sidebar__link-label">{label}</span>}
              </NavLink>
            </li>
          ))}

          {isAdmin && (
            <li>
              <NavLink
                to="/admin/dashboard"
                onClick={onNavigate}
                className={`sidebar__link sidebar__link--admin ${isOnAdminPage ? 'sidebar__link--active' : ''}`}
                title={collapsed ? 'Administration' : undefined}
              >
                <ShieldCheck className="sidebar__link-icon" />
                {!collapsed && <span className="sidebar__link-label">Administration</span>}
              </NavLink>
            </li>
          )}
        </ul>
      </nav>

      <div className="sidebar__footer">
        {!collapsed && user && (
          <div className="sidebar__user">
            <div className="sidebar__avatar">
              {user.prenom?.[0]}{user.nom?.[0]}
            </div>
            <div className="sidebar__user-info">
              <span className="sidebar__user-name">{user.prenom} {user.nom}</span>
              <span className="sidebar__user-email">{user.email}</span>
            </div>
          </div>
        )}
        <button className="sidebar__logout" onClick={handleLogout} title="Se déconnecter">
          <LogOut />
          {!collapsed && <span>Déconnexion</span>}
        </button>
      </div>
    </aside>
  );
}
