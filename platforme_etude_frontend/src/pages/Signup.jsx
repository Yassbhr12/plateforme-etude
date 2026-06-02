import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/authService';
import { useAuth } from '../context/AuthContext';
import { GraduationCap, User, Mail, Lock, AlertCircle, CheckCircle, ArrowRight, BookOpen, Target, Users } from 'lucide-react';
import './Signup.css';

const getRedirectPath = (role) => (role === 'ROLE_ADMIN' ? '/admin/dashboard' : '/dashboard');

export default function Signup() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', password: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (user) navigate(getRedirectPath(user.role), { replace: true });
  }, [user, navigate]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      return;
    }

    if (form.password.length < 8) {
      setError('Le mot de passe doit contenir au moins 8 caractères');
      return;
    }

    setLoading(true);
    try {
      await register({
        nom: form.nom,
        prenom: form.prenom,
        email: form.email,
        password: form.password,
      });
      setSuccess(true);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || "Erreur lors de l'inscription");
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div className="signup-page">
        <div className="signup-page__left">
          <div className="signup-page__form-wrapper">
            <div className="signup-success">
              <div className="signup-success__icon">
                <CheckCircle />
              </div>
              <h2>Compte créé avec succès</h2>
              <p>Votre compte a été créé. Vous pouvez maintenant vous connecter.</p>
              <Link to="/login" className="btn btn-primary btn-lg">
                Se connecter
                <ArrowRight />
              </Link>
            </div>
          </div>
        </div>
        <div className="signup-page__right">
          <div className="signup-page__right-content">
            <div className="signup-page__brand">
              <div className="signup-page__logo">
                <GraduationCap />
              </div>
              <h1 className="signup-page__brand-name">Platforme Étude</h1>
            </div>
            <h2 className="signup-page__headline">
              Créez votre espace<br />étudiant sécurisé
            </h2>
            <p className="signup-page__desc">
              Commencez à organiser vos études, progressez avec méthode.
            </p>
            <p className="signup-page__desc">
              Créez votre espace étudiant pour planifier vos sessions, suivre vos objectifs et collaborer avec votre groupe.
            </p>
            <div className="signup-page__features">
              <div className="signup-page__feature">
                <div className="signup-page__feature-icon"><BookOpen /></div>
                <div>
                  <strong>Sessions planifiées</strong>
                  <span>Organisez et suivez vos heures d'étude</span>
                </div>
              </div>
              <div className="signup-page__feature">
                <div className="signup-page__feature-icon"><Target /></div>
                <div>
                  <strong>Objectifs hebdomadaires</strong>
                  <span>Fixez et atteignez vos objectifs par matière</span>
                </div>
              </div>
              <div className="signup-page__feature">
                <div className="signup-page__feature-icon"><Users /></div>
                <div>
                  <strong>Groupes d'étude</strong>
                  <span>Collaborez avec vos camarades</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="signup-page">
      <div className="signup-page__left">
        <div className="signup-page__form-wrapper">
          <div className="signup-page__form-header">
            <h2>Créer un compte</h2>
            <p>Remplissez le formulaire pour créer votre espace</p>
          </div>

          {error && (
            <div className="alert alert-error">
              <AlertCircle />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="signup-form">
            <div className="signup-form__row">
              <div className="form-group">
                <label className="form-label" htmlFor="signup-nom">Nom</label>
                <div className="login-form__input-wrapper">
                  <User className="login-form__input-icon" />
                  <input
                    id="signup-nom"
                    name="nom"
                    type="text"
                    className="form-input login-form__input-with-icon"
                    placeholder="Votre nom"
                    value={form.nom}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="signup-prenom">Prénom</label>
                <div className="login-form__input-wrapper">
                  <User className="login-form__input-icon" />
                  <input
                    id="signup-prenom"
                    name="prenom"
                    type="text"
                    className="form-input login-form__input-with-icon"
                    placeholder="Votre prénom"
                    value={form.prenom}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="signup-email">Adresse email</label>
              <div className="login-form__input-wrapper">
                <Mail className="login-form__input-icon" />
                <input
                  id="signup-email"
                  name="email"
                  type="email"
                  className="form-input login-form__input-with-icon"
                  placeholder="votre@email.com"
                  value={form.email}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="signup-password">Mot de passe</label>
              <div className="login-form__input-wrapper">
                <Lock className="login-form__input-icon" />
                <input
                  id="signup-password"
                  name="password"
                  type="password"
                  className="form-input login-form__input-with-icon"
                  placeholder="Min. 8 caractères"
                  value={form.password}
                  onChange={handleChange}
                  required
                  minLength={8}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="signup-confirm">Confirmer le mot de passe</label>
              <div className="login-form__input-wrapper">
                <Lock className="login-form__input-icon" />
                <input
                  id="signup-confirm"
                  name="confirmPassword"
                  type="password"
                  className="form-input login-form__input-with-icon"
                  placeholder="Confirmez votre mot de passe"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  required
                  minLength={8}
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-lg w-full" disabled={loading}>
              {loading ? 'Création en cours...' : 'Créer mon compte'}
              {!loading && <ArrowRight />}
            </button>
          </form>

          <p className="login-page__signup-link">
            Déjà un compte ? <Link to="/login">Se connecter</Link>
          </p>
        </div>
      </div>

      <div className="signup-page__right">
        <div className="signup-page__right-content">
          <div className="signup-page__brand">
            <div className="signup-page__logo">
              <GraduationCap />
            </div>
            <h1 className="signup-page__brand-name">Platforme Étude</h1>
          </div>

          <h2 className="signup-page__headline">
            Créez votre espace<br />étudiant sécurisé
          </h2>
          <p className="signup-page__desc">
            Commencez à organiser vos études, progressez avec méthode.
          </p>
          <p className="signup-page__desc">
            Créez votre espace étudiant pour planifier vos sessions, suivre vos objectifs et collaborer avec votre groupe.
          </p>

          <div className="signup-page__features">
            <div className="signup-page__feature">
              <div className="signup-page__feature-icon"><BookOpen /></div>
              <div>
                <strong>Sessions planifiées</strong>
                <span>Organisez et suivez vos heures d'étude</span>
              </div>
            </div>
            <div className="signup-page__feature">
              <div className="signup-page__feature-icon"><Target /></div>
              <div>
                <strong>Objectifs hebdomadaires</strong>
                <span>Fixez et atteignez vos objectifs par matière</span>
              </div>
            </div>
            <div className="signup-page__feature">
              <div className="signup-page__feature-icon"><Users /></div>
              <div>
                <strong>Groupes d'étude</strong>
                <span>Collaborez avec vos camarades</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
