import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../api/authService';
import { useAuth } from '../context/AuthContext';
import { GraduationCap, User, Mail, Lock, AlertCircle, CheckCircle, ArrowRight } from 'lucide-react';
import './Signup.css';

export default function Signup() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [form, setForm] = useState({ nom: '', prenom: '', email: '', password: '', confirmPassword: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (user) navigate('/dashboard', { replace: true });
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
        <div className="signup-page__container">
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
    );
  }

  return (
    <div className="signup-page">
      <div className="signup-page__container">
        <div className="signup-page__form-wrapper">
          <div className="signup-page__header">
            <Link to="/login" className="signup-page__brand">
              <div className="signup-page__logo">
                <GraduationCap />
              </div>
              <span>Platforme Étude</span>
            </Link>
            <h2>Créer un compte</h2>
            <p>Rejoignez la communauté d'apprenants</p>
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
    </div>
  );
}
