import { useLocation } from 'react-router-dom';
import { Bell, Menu } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import './Header.css';

const PAGE_TITLES = {
  '/dashboard': 'Dashboard',
  '/matieres': 'Matières',
  '/sessions': 'Sessions d\'étude',
  '/objectifs': 'Objectifs hebdomadaires',
  '/groupes': 'Groupes d\'étude',
  '/disponibilites': 'Disponibilités',
  '/notifications': 'Notifications',
  '/profil': 'Mon profil',
  '/admin': 'Administration',
};

export default function Header({ onMenuToggle }) {
  const location = useLocation();
  const { user } = useAuth();

  const basePath = '/' + location.pathname.split('/')[1];
  const title = PAGE_TITLES[basePath] || 'Platforme Étude';

  return (
    <header className="header">
      <div className="header__left">
        <button className="header__menu-btn btn btn-ghost btn-icon" onClick={onMenuToggle}>
          <Menu />
        </button>
        <h1 className="header__title">{title}</h1>
      </div>

      <div className="header__right">
        <div className="header__greeting">
          Bonjour, <strong>{user?.prenom || 'Utilisateur'}</strong>
        </div>
      </div>
    </header>
  );
}
