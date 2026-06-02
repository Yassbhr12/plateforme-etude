import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { getProfile, updateProfile } from '../api/profilService';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { User, Mail, Shield, CheckCircle, AlertCircle } from 'lucide-react';
import './Profil.css';

export default function Profil() {
  const { user, updateStoredUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ nom: '', prenom: '', email: '' });
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { loadProfile(); }, []);

  const loadProfile = async () => {
    try {
      const data = await getProfile();
      setProfile(data);
      setForm({ nom: data.nom || '', prenom: data.prenom || '', email: data.email || '' });
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');
    try {
      const updated = await updateProfile({ nom: form.nom, prenom: form.prenom });
      setProfile(updated);
      setSuccess('Profil mis à jour avec succès');
      // Update React state + localStorage via AuthContext
      updateStoredUser({ nom: form.nom, prenom: form.prenom });
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Erreur lors de la mise à jour');
    } finally { setSaving(false); }
  };

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement..." /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <h1>Mon profil</h1>
        <p>Gérez vos informations personnelles</p>
      </div>

      <div className="profil__layout">
        <div className="profil__sidebar card">
          <div className="card-body">
            <div className="profil__avatar">
              {profile?.prenom?.[0]}{profile?.nom?.[0]}
            </div>
            <h3 className="profil__name">{profile?.prenom} {profile?.nom}</h3>
            <p className="profil__email">{profile?.email}</p>
            <div className="profil__role">
              <Shield style={{ width: 14, height: 14 }} />
              <span>{profile?.role === 'ROLE_ADMIN' ? 'Administrateur' : 'Étudiant'}</span>
            </div>
          </div>
        </div>

        <div className="profil__form-card card">
          <div className="card-header">
            <h3>Informations personnelles</h3>
          </div>
          <div className="card-body">
            {success && <div className="alert alert-success"><CheckCircle /><span>{success}</span></div>}
            {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}

            <form onSubmit={handleSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label className="form-label">Nom</label>
                  <div className="login-form__input-wrapper">
                    <User className="login-form__input-icon" />
                    <input className="form-input login-form__input-with-icon" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} required />
                  </div>
                </div>
                <div className="form-group">
                  <label className="form-label">Prénom</label>
                  <div className="login-form__input-wrapper">
                    <User className="login-form__input-icon" />
                    <input className="form-input login-form__input-with-icon" value={form.prenom} onChange={(e) => setForm({ ...form, prenom: e.target.value })} required />
                  </div>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Email</label>
                <div className="login-form__input-wrapper">
                  <Mail className="login-form__input-icon" />
                  <input type="email" className="form-input login-form__input-with-icon" value={form.email} disabled readOnly />
                </div>
                <p className="form-hint">L'adresse email est utilisée pour la connexion et la validation 2FA.</p>
              </div>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Enregistrement...' : 'Mettre à jour'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
