import { useLocation, useNavigate } from 'react-router-dom';
import { Bell, Menu } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import './AdminHeader.css';

const PAGE_META = {
  '/admin/dashboard': { title: 'Dashboard', desc: 'Vue d\'ensemble de la plateforme' },
  '/admin/users': { title: 'Utilisateurs', desc: 'Gestion des comptes utilisateurs' },
  '/admin/stats': { title: 'Statistiques', desc: 'Analyses et métriques détaillées' },
  '/admin/sessions': { title: 'Sessions', desc: 'Supervision des sessions d\'étude' },
  '/admin/groupes': { title: 'Groupes d\'étude', desc: 'Gestion des groupes et membres' },
  '/admin/notifications': { title: 'Notifications', desc: 'Historique des notifications' },
  '/admin/data': { title: 'Donnees', desc: 'Supervision des contenus et interactions' },
  '/admin/settings': { title: 'Paramètres', desc: 'Configuration de la plateforme' },
};

export default function AdminHeader({ onMenuToggle }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();

  const meta = PAGE_META[location.pathname] || { title: 'Administration', desc: '' };

  return (
    <header className="admin-header">
      <div className="admin-header__left">
        <button className="admin-header__menu-btn btn btn-ghost btn-icon" onClick={onMenuToggle}>
          <Menu />
        </button>
        <div className="admin-header__title-group">
          <h1 className="admin-header__title">{meta.title}</h1>
          {meta.desc && <p className="admin-header__desc">{meta.desc}</p>}
        </div>
      </div>

      <div className="admin-header__right">
        <button
          className="admin-header__notif-btn btn btn-ghost btn-icon"
          title="Notifications"
          onClick={() => navigate('/admin/notifications')}
        >
          <Bell />
          <span className="admin-header__notif-dot" />
        </button>

        <div className="admin-header__avatar" title={`${user?.prenom} ${user?.nom}`}>
          {user?.prenom?.[0]}{user?.nom?.[0]}
        </div>
      </div>
    </header>
  );
}
