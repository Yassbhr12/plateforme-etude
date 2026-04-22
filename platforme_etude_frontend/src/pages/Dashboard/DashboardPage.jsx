import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import '../../App.css';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <div className="auth-layout" style={{ justifyContent: 'center', alignItems: 'center' }}>
      <div className="auth-bg-orb auth-bg-orb--1" />
      <div className="auth-bg-orb auth-bg-orb--2" />
      <div className="auth-bg-orb auth-bg-orb--3" />

      <div className="auth-card" style={{ maxWidth: '520px', zIndex: 1 }}>
        <div className="auth-card__header">
          <h2 className="auth-card__title">Bienvenue, {user?.prenom} ! 🎉</h2>
          <p className="auth-card__description">
            Vous êtes connecté(e) en tant que{' '}
            <strong style={{ color: 'var(--focus-400)' }}>{user?.email}</strong>
          </p>
        </div>

        <div className="auth-alert auth-alert--success" style={{ marginBottom: '1.5rem' }}>
          <span className="auth-alert__icon">✅</span>
          Authentification réussie avec 2FA
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0', borderBottom: '1px solid var(--border-subtle)' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Nom complet</span>
            <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>{user?.prenom} {user?.nom}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0.6rem 0', borderBottom: '1px solid var(--border-subtle)' }}>
            <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Rôle</span>
            <span style={{
              padding: '2px 12px',
              borderRadius: 'var(--radius-full)',
              fontSize: '0.8rem',
              fontWeight: 600,
              background: user?.role === 'ROLE_ADMIN' ? 'rgba(240,168,48,0.15)' : 'rgba(46,196,182,0.15)',
              color: user?.role === 'ROLE_ADMIN' ? 'var(--motive-400)' : 'var(--focus-400)',
            }}>
              {user?.role === 'ROLE_ADMIN' ? '👑 Admin' : '📖 Étudiant'}
            </span>
          </div>
        </div>

        <button
          className="btn btn--outline btn--full"
          onClick={handleLogout}
        >
          Se déconnecter 🚪
        </button>
      </div>
    </div>
  );
};

export default DashboardPage;
