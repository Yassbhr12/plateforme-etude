import { useState, useRef, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login, validateCode } from '../../api/authService';
import { useAuth } from '../../context/AuthContext';
import '../../App.css';

const TIMER_DURATION = 180; // 3 minutes — matches backend expiration

const LoginPage = () => {
  const navigate = useNavigate();
  const { loginSuccess } = useAuth();

  // ── State ──
  const [step, setStep] = useState(1); // 1 = credentials, 2 = 2FA
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [code, setCode] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [timer, setTimer] = useState(TIMER_DURATION);
  const [timerActive, setTimerActive] = useState(false);

  const codeRefs = useRef([]);

  // ── Timer ──
  useEffect(() => {
    let interval;
    if (timerActive && timer > 0) {
      interval = setInterval(() => setTimer((t) => t - 1), 1000);
    } else if (timer === 0) {
      setTimerActive(false);
    }
    return () => clearInterval(interval);
  }, [timerActive, timer]);

  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s.toString().padStart(2, '0')}`;
  };

  // ── Step 1: Submit credentials ──
  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!email || !password) {
      setError('Veuillez remplir tous les champs.');
      return;
    }

    setLoading(true);
    try {
      const data = await login(email, password);
      setSuccess(data.message || 'Code de validation envoyé à votre email.');
      setStep(2);
      setTimer(TIMER_DURATION);
      setTimerActive(true);
      // Focus first code input
      setTimeout(() => codeRefs.current[0]?.focus(), 100);
    } catch (err) {
      const msg =
        err.response?.data?.error ||
        err.response?.data?.['error : '] ||
        'Email ou mot de passe incorrect.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Code input handling ──
  const handleCodeChange = (index, value) => {
    if (!/^\d*$/.test(value)) return;

    const newCode = [...code];
    newCode[index] = value.slice(-1);
    setCode(newCode);

    // Auto-focus next input
    if (value && index < 5) {
      codeRefs.current[index + 1]?.focus();
    }
  };

  const handleCodeKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !code[index] && index > 0) {
      codeRefs.current[index - 1]?.focus();
    }
  };

  const handleCodePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    if (pasted.length > 0) {
      const newCode = [...code];
      for (let i = 0; i < 6; i++) {
        newCode[i] = pasted[i] || '';
      }
      setCode(newCode);
      const focusIndex = Math.min(pasted.length, 5);
      codeRefs.current[focusIndex]?.focus();
    }
  };

  // ── Step 2: Validate 2FA code ──
  const handleValidateCode = useCallback(async () => {
    const fullCode = code.join('');
    if (fullCode.length !== 6) {
      setError('Veuillez entrer le code complet à 6 chiffres.');
      return;
    }

    if (timer <= 0) {
      setError('Le code a expiré. Veuillez vous reconnecter.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = await validateCode(email, fullCode);
      const authResponse = data.response;
      loginSuccess(authResponse);
      navigate('/dashboard');
    } catch (err) {
      const msg =
        err.response?.data?.error || 'Code de validation incorrect.';
      setError(msg);
      setCode(['', '', '', '', '', '']);
      codeRefs.current[0]?.focus();
    } finally {
      setLoading(false);
    }
  }, [code, email, timer, loginSuccess, navigate]);

  // Auto-submit when all 6 digits entered
  useEffect(() => {
    if (code.every((d) => d !== '') && step === 2) {
      handleValidateCode();
    }
  }, [code, step, handleValidateCode]);

  // ── Resend code ──
  const handleResend = async () => {
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      setSuccess('Un nouveau code a été envoyé.');
      setTimer(TIMER_DURATION);
      setTimerActive(true);
      setCode(['', '', '', '', '', '']);
      codeRefs.current[0]?.focus();
    } catch (err) {
      setError('Erreur lors du renvoi du code.');
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
            Étudiez plus intelligemment,{' '}
            <span>pas plus longtemps.</span>
          </h1>
          <p className="auth-branding__subtitle">
            Rejoignez une communauté d'apprenants motivés. Organisez vos sessions,
            suivez vos objectifs et collaborez en temps réel.
          </p>
          <div className="auth-features">
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">🧘</span>
              Sessions guidées
            </div>
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">🎯</span>
              Objectifs hebdos
            </div>
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">📊</span>
              Suivi de progrès
            </div>
            <div className="auth-feature-pill">
              <span className="auth-feature-pill__icon">🚀</span>
              Groupes d'étude
            </div>
          </div>
        </div>
      </div>

      {/* ── Form Panel ── */}
      <div className="auth-form-panel">
        <div className="auth-card">
          {/* Step indicator */}
          <div className="auth-steps">
            <div className="auth-step">
              <div
                className={`auth-step__circle ${
                  step >= 1
                    ? step > 1
                      ? 'auth-step__circle--done'
                      : 'auth-step__circle--active'
                    : ''
                }`}
              >
                {step > 1 ? '✓' : '1'}
              </div>
            </div>
            <div className={`auth-step__line ${step >= 2 ? 'auth-step__line--active' : ''}`} />
            <div className="auth-step">
              <div
                className={`auth-step__circle ${
                  step >= 2 ? 'auth-step__circle--active' : ''
                }`}
              >
                2
              </div>
            </div>
          </div>

          {/* ── Step 1: Credentials ── */}
          {step === 1 && (
            <>
              <div className="auth-card__header">
                <h2 className="auth-card__title">Bon retour parmi nous 👋</h2>
                <p className="auth-card__description">
                  Connectez-vous pour reprendre vos sessions d'étude.
                </p>
              </div>

              {error && (
                <div className="auth-alert auth-alert--error">
                  <span className="auth-alert__icon">⚠️</span>
                  {error}
                </div>
              )}

              <form className="auth-form" onSubmit={handleLoginSubmit}>
                <div className="form-group">
                  <label className="form-group__label" htmlFor="login-email">
                    Adresse email
                  </label>
                  <div className="form-group__input-wrapper">
                    <span className="form-group__icon">✉️</span>
                    <input
                      id="login-email"
                      type="email"
                      className="form-group__input"
                      placeholder="votre@email.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      autoComplete="email"
                      required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-group__label" htmlFor="login-password">
                    Mot de passe
                  </label>
                  <div className="form-group__input-wrapper">
                    <span className="form-group__icon">🔒</span>
                    <input
                      id="login-password"
                      type={showPassword ? 'text' : 'password'}
                      className="form-group__input"
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      autoComplete="current-password"
                      minLength={8}
                      required
                    />
                    <button
                      type="button"
                      className="form-group__toggle-password"
                      onClick={() => setShowPassword(!showPassword)}
                      aria-label={showPassword ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
                    >
                      {showPassword ? '🙈' : '👁️'}
                    </button>
                  </div>
                </div>

                <button
                  type="submit"
                  className="btn btn--primary btn--full"
                  disabled={loading}
                >
                  {loading ? (
                    <>
                      <div className="btn__spinner" />
                      Connexion en cours...
                    </>
                  ) : (
                    <>Se connecter 🔑</>
                  )}
                </button>
              </form>

              <div className="auth-divider">ou</div>

              <p className="auth-link">
                Vous n'avez pas de compte ?{' '}
                <Link to="/signup">Créer un compte</Link>
              </p>
            </>
          )}

          {/* ── Step 2: 2FA Validation ── */}
          {step === 2 && (
            <>
              <div className="auth-card__header">
                <h2 className="auth-card__title">Vérification 2FA 🔐</h2>
                <p className="auth-card__description">
                  Un code à 6 chiffres a été envoyé à{' '}
                  <strong style={{ color: 'var(--focus-400)' }}>{email}</strong>
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

              <div className="code-input-group" onPaste={handleCodePaste}>
                {code.map((digit, index) => (
                  <input
                    key={index}
                    ref={(el) => (codeRefs.current[index] = el)}
                    type="text"
                    inputMode="numeric"
                    maxLength={1}
                    className={`code-input-group__input ${
                      digit ? 'code-input-group__input--filled' : ''
                    }`}
                    value={digit}
                    onChange={(e) => handleCodeChange(index, e.target.value)}
                    onKeyDown={(e) => handleCodeKeyDown(index, e)}
                    autoFocus={index === 0}
                  />
                ))}
              </div>

              {/* Timer */}
              <div className="auth-timer">
                {timer > 0 ? (
                  <>
                    ⏱️ Code expire dans{' '}
                    <span className="auth-timer__countdown">
                      {formatTime(timer)}
                    </span>
                  </>
                ) : (
                  <>
                    <span className="auth-timer__countdown auth-timer__countdown--expired">
                      Code expiré
                    </span>
                    <button
                      className="auth-timer__resend"
                      onClick={handleResend}
                      disabled={loading}
                    >
                      Renvoyer le code
                    </button>
                  </>
                )}
              </div>

              <div className="auth-form" style={{ marginTop: '1rem' }}>
                <button
                  className="btn btn--primary btn--full"
                  onClick={handleValidateCode}
                  disabled={loading || code.some((d) => d === '')}
                >
                  {loading ? (
                    <>
                      <div className="btn__spinner" />
                      Vérification...
                    </>
                  ) : (
                    <>Valider le code ✓</>
                  )}
                </button>

                <button
                  className="btn btn--outline btn--full"
                  onClick={() => {
                    setStep(1);
                    setCode(['', '', '', '', '', '']);
                    setError('');
                    setSuccess('');
                    setTimerActive(false);
                  }}
                >
                  ← Retour à la connexion
                </button>
              </div>

              {timer > 0 && (
                <p className="auth-link" style={{ marginTop: '1rem' }}>
                  Vous n'avez pas reçu le code ?{' '}
                  <button
                    className="auth-timer__resend"
                    onClick={handleResend}
                    disabled={loading}
                    style={{ display: 'inline' }}
                  >
                    Renvoyer
                  </button>
                </p>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
