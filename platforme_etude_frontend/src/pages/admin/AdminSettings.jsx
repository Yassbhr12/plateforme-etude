import { useEffect, useState } from 'react';
import { Save, ShieldCheck, UserRound } from 'lucide-react';
import { getProfile, updateProfile } from '../../api/adminService';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import StatCard from '../../components/ui/StatCard';
import { useAuth } from '../../context/AuthContext';
import './AdminCommon.css';

export default function AdminSettings() {
  const { user, updateStoredUser } = useAuth();
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', role: 'ROLE_ADMIN', actif: true });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  async function loadProfile() {
    setLoading(true);
    setError('');
    try {
      const profile = await getProfile();
      setForm({
        nom: profile.nom || '',
        prenom: profile.prenom || '',
        email: profile.email || '',
        role: profile.role || 'ROLE_ADMIN',
        actif: profile.actif ?? true,
      });
    } catch (err) {
      console.error('Failed to load admin profile:', err);
      setError('Impossible de charger le profil.');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => loadProfile(), 0);
    return () => window.clearTimeout(timer);
  }, []);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setMessage('');
    setError('');
    try {
      const updated = await updateProfile({
        nom: form.nom,
        prenom: form.prenom,
        email: form.email,
        role: form.role,
        actif: form.actif,
      });
      updateStoredUser({
        nom: updated.nom,
        prenom: updated.prenom,
        email: updated.email,
        role: updated.role,
      });
      setMessage('Profil mis a jour.');
    } catch (err) {
      console.error('Failed to update admin profile:', err);
      setError("Le profil n'a pas pu etre mis a jour.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="page flex justify-center" style={{ paddingTop: 80 }}>
        <LoadingSpinner size="lg" text="Chargement des parametres..." />
      </div>
    );
  }

  return (
    <div className="page">
      {message && <div className="alert alert-success">{message}</div>}
      {error && <div className="alert alert-error">{error}</div>}

      <div className="admin-kpi-grid">
        <StatCard icon={<ShieldCheck />} label="Role" value={form.role === 'ROLE_ADMIN' ? 'Admin' : 'User'} color="primary" />
        <StatCard icon={<UserRound />} label="Compte" value={form.actif ? 'Actif' : 'Inactif'} color={form.actif ? 'success' : 'warning'} />
        <StatCard icon={<ShieldCheck />} label="Session" value={user?.role === 'ROLE_ADMIN' ? 'Valide' : 'Limitee'} color="accent" />
        <StatCard icon={<UserRound />} label="ID" value={user?.id || '-'} color="primary" />
      </div>

      <div className="card">
        <div className="card-header">
          <h3>Profil administrateur</h3>
        </div>
        <div className="card-body">
          <form onSubmit={handleSubmit}>
            <div className="grid-2">
              <div className="form-group">
                <label className="form-label" htmlFor="admin-nom">Nom</label>
                <input
                  id="admin-nom"
                  className="form-input"
                  value={form.nom}
                  onChange={(e) => setForm((prev) => ({ ...prev, nom: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="admin-prenom">Prenom</label>
                <input
                  id="admin-prenom"
                  className="form-input"
                  value={form.prenom}
                  onChange={(e) => setForm((prev) => ({ ...prev, prenom: e.target.value }))}
                  required
                />
              </div>
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="admin-email">Email</label>
              <input
                id="admin-email"
                className="form-input"
                type="email"
                value={form.email}
                onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
                required
              />
            </div>
            <button className="btn btn-primary" type="submit" disabled={saving}>
              <Save />
              {saving ? 'Enregistrement...' : 'Enregistrer'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
