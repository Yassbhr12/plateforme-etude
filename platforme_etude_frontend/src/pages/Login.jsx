import { useState, useEffect, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login, validateCode } from '../api/authService';
import { GraduationCap, Mail, Lock, ArrowRight, ShieldCheck, AlertCircle, BookOpen, Users, Target } from 'lucide-react';
import './Login.css';

const getRedirectPath = (role) => (role === 'ROLE_ADMIN' ? '/admin/dashboard' : '/dashboard');

export default function Login() {
  const { saveUser, user } = useAuth();
  const navigate = useNavigate();

  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [code, setCode] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [timer, setTimer] = useState(60);

  const codeRefs = useRef([]);

  useEffect(() => {
    if (user) navigate(getRedirectPath(user.role), { replace: true });
  }, [user, navigate]);

  useEffect(() => {
    let interval;
    if (step === 2 && timer > 0) {
      interval = setInterval(() => setTimer((t) => t - 1), 1000);
    }
    return () => clearInterval(interval);
  }, [step, timer]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      setStep(2);
      setTimer(60);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Email ou mot de passe incorrect');
    } finally {
      setLoading(false);
    }
  };

  const handleCodeChange = (index, value) => {
    if (!/^\d*$/.test(value)) return;
    const newCode = [...code];
    newCode[index] = value.slice(-1);
    setCode(newCode);
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
    const newCode = [...code];
    for (let i = 0; i < 6; i++) {
      newCode[i] = pasted[i] || '';
    }
    setCode(newCode);
    const nextEmpty = newCode.findIndex((c) => !c);
    codeRefs.current[nextEmpty === -1 ? 5 : nextEmpty]?.focus();
  };

  const handleValidate = async (e) => {
    e.preventDefault();
    const fullCode = code.join('');
    if (fullCode.length !== 6) {
      setError('Veuillez entrer le code complet');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const data = await validateCode(email, fullCode);
      saveUser(data.response);
      navigate(getRedirectPath(data.response.role), { replace: true });
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Code de validation invalide');
    } finally {
      setLoading(false);
    }
  };

  const resendCode = async () => {
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      setTimer(60);
      setCode(['', '', '', '', '', '']);
    } catch {
      setError('Erreur lors du renvoi du code');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-page__left">
        <div className="login-page__left-content">
          <div className="login-page__brand">
            <div className="login-page__logo">
              <GraduationCap />
            </div>
            <h1 className="login-page__brand-name">Platforme Étude</h1>
          </div>

          <h2 className="login-page__headline">
            Organisez vos études,<br />atteignez vos objectifs.
          </h2>
          <p className="login-page__desc">
            Une plateforme collaborative pour planifier vos sessions, suivre vos objectifs et étudier en groupe.
          </p>

          <div className="login-page__features">
            <div className="login-page__feature">
              <div className="login-page__feature-icon"><BookOpen /></div>
              <div>
                <strong>Sessions planifiées</strong>
                <span>Organisez et suivez vos heures d'étude</span>
              </div>
            </div>
            <div className="login-page__feature">
              <div className="login-page__feature-icon"><Target /></div>
              <div>
                <strong>Objectifs hebdomadaires</strong>
                <span>Fixez et atteignez vos objectifs par matière</span>
              </div>
            </div>
            <div className="login-page__feature">
              <div className="login-page__feature-icon"><Users /></div>
              <div>
                <strong>Groupes d'étude</strong>
                <span>Collaborez avec vos camarades</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="login-page__right">
        <div className="login-page__form-wrapper">
          {step === 1 ? (
            <>
              <div className="login-page__form-header">
                <h2>Connexion</h2>
                <p>Entrez vos identifiants pour accéder à votre espace</p>
              </div>

              {error && (
                <div className="alert alert-error">
                  <AlertCircle />
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleLogin} className="login-form">
                <div className="form-group">
                  <label className="form-label" htmlFor="login-email">Adresse email</label>
                  <div className="login-form__input-wrapper">
                    <Mail className="login-form__input-icon" />
                    <input
                      id="login-email"
                      type="email"
                      className="form-input login-form__input-with-icon"
                      placeholder="votre@email.com"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      required
                      autoComplete="email"
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="login-password">Mot de passe</label>
                  <div className="login-form__input-wrapper">
                    <Lock className="login-form__input-icon" />
                    <input
                      id="login-password"
                      type="password"
                      className="form-input login-form__input-with-icon"
                      placeholder="Votre mot de passe"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      minLength={8}
                      autoComplete="current-password"
                    />
                  </div>
                </div>

                <div className="login-form__forgot-row">
                  <Link to="/forgot-password">Mot de passe oublié ?</Link>
                </div>

                <button type="submit" className="btn btn-primary btn-lg w-full" disabled={loading}>
                  {loading ? 'Connexion en cours...' : 'Se connecter'}
                  {!loading && <ArrowRight />}
                </button>
              </form>

              <p className="login-page__signup-link">
                Pas encore de compte ? <Link to="/signup">Créer un compte</Link>
              </p>
            </>
          ) : (
            <>
              <div className="login-page__form-header">
                <div className="login-page__2fa-icon">
                  <ShieldCheck />
                </div>
                <h2>Vérification 2FA</h2>
                <p>Un code de vérification a été envoyé à <strong>{email}</strong></p>
              </div>

              {error && (
                <div className="alert alert-error">
                  <AlertCircle />
                  <span>{error}</span>
                </div>
              )}

              <form onSubmit={handleValidate} className="login-form">
                <div className="login-form__code-group">
                  <label className="form-label">Code de vérification</label>
                  <div className="login-form__code-inputs" onPaste={handleCodePaste}>
                    {code.map((digit, i) => (
                      <input
                        key={i}
                        ref={(el) => (codeRefs.current[i] = el)}
                        type="text"
                        inputMode="numeric"
                        maxLength={1}
                        className="login-form__code-digit"
                        value={digit}
                        onChange={(e) => handleCodeChange(i, e.target.value)}
                        onKeyDown={(e) => handleCodeKeyDown(i, e)}
                        autoFocus={i === 0}
                      />
                    ))}
                  </div>
                </div>

                <div className="login-form__timer">
                  {timer > 0 ? (
                    <span className="login-form__timer-text">
                      Code expire dans <strong>{Math.floor(timer / 60)}:{String(timer % 60).padStart(2, '0')}</strong>
                    </span>
                  ) : (
                    <button type="button" className="btn btn-ghost btn-sm" onClick={resendCode} disabled={loading}>
                      Renvoyer le code
                    </button>
                  )}
                </div>

                <button type="submit" className="btn btn-primary btn-lg w-full" disabled={loading}>
                  {loading ? 'Vérification...' : 'Valider le code'}
                </button>

                <button type="button" className="btn btn-ghost w-full" onClick={() => { setStep(1); setError(''); setCode(['','','','','','']); }}>
                  Retour à la connexion
                </button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
