import { useState, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../../api/authService';
import '../../App.css';

const SignupPage = () => {
  const navigate = useNavigate();

  // ── State ──
  const [formData, setFormData] = useState({
    nom: '',
    prenom: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  // ── Handlers ──
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    // Clear field error on change
    if (fieldErrors[name]) {
      setFieldErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  // ── Password Strength ──
  const passwordStrength = useMemo(() => {
    const pw = formData.password;
    if (!pw) return 0;
    let score = 0;
    if (pw.length >= 8) score++;
    if (/[A-Z]/.test(pw)) score++;
    if (/[0-9]/.test(pw)) score++;
    if (/[^A-Za-z0-9]/.test(pw)) score++;
    return score;
  }, [formData.password]);

  const strengthLabels = ['', 'Faible', 'Moyen', 'Bon', 'Excellent'];
  const strengthColors = ['', 'var(--error)', 'var(--warning)', 'var(--motive-300)', 'var(--success)'];

  // ── Validation ──
  const validate = () => {
    const errors = {};
    if (!formData.nom.trim()) errors.nom = 'Le nom est requis';
    if (formData.nom.length > 100) errors.nom = 'Le nom ne peut pas dépasser 100 caractères';
    if (!formData.prenom.trim()) errors.prenom = 'Le prénom est requis';
    if (formData.prenom.length > 100) errors.prenom = 'Le prénom ne peut pas dépasser 100 caractères';
    if (!formData.email.trim()) errors.email = "L'email est requis";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email))
      errors.email = 'Format email invalide';
    if (formData.email.length > 150) errors.email = "L'email ne peut pas dépasser 150 caractères";
    if (!formData.password) errors.password = 'Le mot de passe est requis';
    else if (formData.password.length < 8)
      errors.password = 'Minimum 8 caractères';
    if (formData.password !== formData.confirmPassword)
      errors.confirmPassword = 'Les mots de passe ne correspondent pas';

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // ── Submit ──
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!validate()) return;

    setLoading(true);
    try {
      await register({
        nom: formData.nom,
        prenom: formData.prenom,
        email: formData.email,
        password: formData.password,
      });
      setSuccess('Compte créé avec succès ! Redirection vers la connexion...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Erreur lors de la création du compte. L'email existe peut-être déjà.";
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-layout">
      {/* Background Orbs */}
      <div className="auth-bg-orb auth-bg-orb--1" />
      <div className="auth-bg-orb auth-bg-orb--2" />
      <div className="auth-bg-orb auth-bg-orb--3" />

      {/* ── Branding Panel ── */}
      <div className="auth-branding">
        <div className="auth-branding__inner">
          <div className="auth-branding__logo">
            <div className="auth-branding__logo-icon">📚</div>
            <span className="auth-branding__logo-text">Platforme Étude</span>
          </div>
          <h1 className="auth-branding__tagline">
            Commencez votre parcours{' '}
            <span>d'excellence.</span>
          </h1>
          <p className="auth-branding__subtitle">
            Créez votre compte en quelques secondes et accédez à un espace d'apprentissage
            collaboratif conçu pour booster votre productivité.
          </p>
          <div className="auth-features">
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">🔐</span>
              Sécurité 2FA
            </div>
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">👥</span>
              Communauté active
            </div>
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">📱</span>
              Multi-plateforme
            </div>
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">⚡</span>
              Gratuit
            </div>
          </div>
        </div>
      </div>

      {/* ── Form Panel ── */}
      <div className="auth-form-panel">
        <div className="auth-card">
          <div className="auth-card__header">
            <h2 className="auth-card__title">Créer un compte 🚀</h2>
            <p className="auth-card__description">
              Rejoignez la plateforme et commencez à apprendre.
            </p>
          </div>

          {success && (
            <div className="auth-alert auth-alert--success">
              <span className="auth-alert__icon">✅</span>
              {success}
            </div>
          )}

          {error && (
            <div className="auth-alert auth-alert--error">
              <span className="auth-alert__icon">⚠️</span>
              {error}
            </div>
          )}

          <form className="auth-form" onSubmit={handleSubmit}>
            {/* Nom + Prénom */}
            <div className="auth-form__row">
              <div className="form-group">
                <label className="form-group__label" htmlFor="signup-nom">
                  Nom
                </label>
                <div className="form-group__input-wrapper">
                  <span className="form-group__icon">👤</span>
                  <input
                    id="signup-nom"
                    type="text"
                    name="nom"
                    className={`form-group__input ${fieldErrors.nom ? 'form-group__input--error' : ''}`}
                    placeholder="Dupont"
                    value={formData.nom}
                    onChange={handleChange}
                    maxLength={100}
                    required
                  />
                </div>
                {fieldErrors.nom && (
                  <span className="form-group__error">⚠ {fieldErrors.nom}</span>
                )}
              </div>

              <div className="form-group">
                <label className="form-group__label" htmlFor="signup-prenom">
                  Prénom
                </label>
                <div className="form-group__input-wrapper">
                  <span className="form-group__icon">👤</span>
                  <input
                    id="signup-prenom"
                    type="text"
                    name="prenom"
                    className={`form-group__input ${fieldErrors.prenom ? 'form-group__input--error' : ''}`}
                    placeholder="Jean"
                    value={formData.prenom}
                    onChange={handleChange}
                    maxLength={100}
                    required
                  />
                </div>
                {fieldErrors.prenom && (
                  <span className="form-group__error">⚠ {fieldErrors.prenom}</span>
                )}
              </div>
            </div>

            {/* Email */}
            <div className="form-group">
              <label className="form-group__label" htmlFor="signup-email">
                Adresse email
              </label>
              <div className="form-group__input-wrapper">
                <span className="form-group__icon">✉️</span>
                <input
                  id="signup-email"
                  type="email"
                  name="email"
                  className={`form-group__input ${fieldErrors.email ? 'form-group__input--error' : ''}`}
                  placeholder="votre@email.com"
                  value={formData.email}
                  onChange={handleChange}
                  autoComplete="email"
                  maxLength={150}
                  required
                />
              </div>
              {fieldErrors.email && (
                <span className="form-group__error">⚠ {fieldErrors.email}</span>
              )}
            </div>

            {/* Password */}
            <div className="form-group">
              <label className="form-group__label" htmlFor="signup-password">
                Mot de passe
              </label>
              <div className="form-group__input-wrapper">
                <span className="form-group__icon">🔒</span>
                <input
                  id="signup-password"
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  className={`form-group__input ${fieldErrors.password ? 'form-group__input--error' : ''}`}
                  placeholder="Minimum 8 caractères"
                  value={formData.password}
                  onChange={handleChange}
                  autoComplete="new-password"
                  minLength={8}
                  required
                />
                <button
                  type="button"
                  className="form-group__toggle-password"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Masquer' : 'Afficher'}
                >
                  {showPassword ? '🙈' : '👁️'}
                </button>
              </div>
              {fieldErrors.password && (
                <span className="form-group__error">⚠ {fieldErrors.password}</span>
              )}

              {/* Password Strength Meter */}
              {formData.password && (
                <div className="password-strength">
                  <div className="password-strength__bars">
                    {[1, 2, 3, 4].map((level) => (
                      <div
                        key={level}
                        className={`password-strength__bar ${
                          passwordStrength >= level
                            ? `password-strength__bar--active strength-${passwordStrength}`
                            : ''
                        }`}
                      />
                    ))}
                  </div>
                  <span
                    className="password-strength__text"
                    style={{ color: strengthColors[passwordStrength] }}
                  >
                    {strengthLabels[passwordStrength]}
                  </span>
                </div>
              )}
            </div>

            {/* Confirm Password */}
            <div className="form-group">
              <label className="form-group__label" htmlFor="signup-confirm">
                Confirmer le mot de passe
              </label>
              <div className="form-group__input-wrapper">
                <span className="form-group__icon">🔒</span>
                <input
                  id="signup-confirm"
                  type={showConfirm ? 'text' : 'password'}
                  name="confirmPassword"
                  className={`form-group__input ${fieldErrors.confirmPassword ? 'form-group__input--error' : ''}`}
                  placeholder="Retapez votre mot de passe"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  autoComplete="new-password"
                  required
                />
                <button
                  type="button"
                  className="form-group__toggle-password"
                  onClick={() => setShowConfirm(!showConfirm)}
                  aria-label={showConfirm ? 'Masquer' : 'Afficher'}
                >
                  {showConfirm ? '🙈' : '👁️'}
                </button>
              </div>
              {fieldErrors.confirmPassword && (
                <span className="form-group__error">
                  ⚠ {fieldErrors.confirmPassword}
                </span>
              )}
            </div>

            <button
              type="submit"
              className="btn btn--warm btn--full"
              disabled={loading}
            >
              {loading ? (
                <>
                  <div className="btn__spinner" />
                  Création en cours...
                </>
              ) : (
                <>Créer mon compte 🚀</>
              )}
            </button>
          </form>

          <div className="auth-divider">ou</div>

          <p className="auth-link">
            Vous avez déjà un compte ?{' '}
            <Link to="/login">Se connecter</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default SignupPage;
