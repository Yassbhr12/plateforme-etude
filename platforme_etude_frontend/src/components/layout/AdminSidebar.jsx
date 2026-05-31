import { NavLink, useNavigate } from 'react-router-dom';
import { createElement } from 'react';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  BarChart3,
  CalendarClock,
  UsersRound,
  Bell,
  Database,
  Settings,
  LogOut,
  GraduationCap,
  ChevronLeft,
  ChevronRight,
  ShieldCheck,
} from 'lucide-react';
import './AdminSidebar.css';

const NAV_ITEMS = [
  { to: '/admin/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/admin/users', icon: Users, label: 'Utilisateurs' },
  { to: '/admin/stats', icon: BarChart3, label: 'Statistiques' },
  { to: '/admin/sessions', icon: CalendarClock, label: 'Sessions' },
  { to: '/admin/groupes', icon: UsersRound, label: "Groupes d'étude" },
  { to: '/admin/notifications', icon: Bell, label: 'Notifications' },
  { to: '/admin/data', icon: Database, label: 'Donnees' },
  { to: '/admin/settings', icon: Settings, label: 'Paramètres' },
];

export default function AdminSidebar({ collapsed, mobileOpen, onNavigate, onToggle }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <aside className={`admin-sidebar ${collapsed ? 'admin-sidebar--collapsed' : ''} ${mobileOpen ? 'admin-sidebar--mobile-open' : ''}`}>
      <div className="admin-sidebar__brand">
        <div className="admin-sidebar__logo">
          <GraduationCap />
        </div>
        {!collapsed && (
          <div className="admin-sidebar__brand-text">
            <span className="admin-sidebar__brand-name">Platforme Étude</span>
            <span className="admin-sidebar__brand-badge">
              <ShieldCheck />
              Admin Panel
            </span>
          </div>
        )}
      </div>

      <button
        className="admin-sidebar__toggle"
        onClick={onToggle}
        aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
      >
        {collapsed ? <ChevronRight /> : <ChevronLeft />}
      </button>

      <nav className="admin-sidebar__nav">
        <ul className="admin-sidebar__list">
          {NAV_ITEMS.map(({ to, icon, label }) => (
            <li key={to}>
              <NavLink
                to={to}
                onClick={onNavigate}
                className={({ isActive }) =>
                  `admin-sidebar__link ${isActive ? 'admin-sidebar__link--active' : ''}`
                }
                title={collapsed ? label : undefined}
              >
                {createElement(icon, { className: 'admin-sidebar__link-icon' })}
                {!collapsed && <span className="admin-sidebar__link-label">{label}</span>}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <div className="admin-sidebar__footer">
        {!collapsed && user && (
          <div className="admin-sidebar__user">
            <div className="admin-sidebar__avatar">
              {user.prenom?.[0]}{user.nom?.[0]}
            </div>
            <div className="admin-sidebar__user-info">
              <span className="admin-sidebar__user-name">{user.prenom} {user.nom}</span>
              <span className="admin-sidebar__user-role">Administrateur</span>
            </div>
          </div>
        )}
        <button className="admin-sidebar__logout" onClick={handleLogout} title="Se déconnecter">
          <LogOut />
          {!collapsed && <span>Déconnexion</span>}
        </button>
      </div>
    </aside>
  );
}
