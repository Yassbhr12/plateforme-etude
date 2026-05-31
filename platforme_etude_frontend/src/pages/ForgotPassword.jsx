import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  AlertCircle,
  ArrowLeft,
  ArrowRight,
  CheckCircle2,
  GraduationCap,
  KeyRound,
  Lock,
  Mail,
  ShieldCheck,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { requestPasswordReset, resetPassword } from '../api/authService';
import './Login.css';
import './ForgotPassword.css';

const createEmptyCode = () => ['', '', '', '', '', ''];

export default function ForgotPassword() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const codeRefs = useRef([]);

  const [step, setStep] = useState(1);
  const [email, setEmail] = useState('');
  const [code, setCode] = useState(createEmptyCode);
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [timer, setTimer] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (user) navigate('/dashboard', { replace: true });
  }, [user, navigate]);

  useEffect(() => {
    if (timer <= 0) return undefined;
    const interval = setInterval(() => setTimer((value) => value - 1), 1000);
    return () => clearInterval(interval);
  }, [timer]);

  const extractMessage = (err, fallback) => (
    err.response?.data?.message || err.response?.data?.error || fallback
  );

  const handleRequestCode = async (event) => {
    event.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const data = await requestPasswordReset(email);
      setSuccess(data.message || 'Si cette adresse existe, un code a ete envoye.');
      setStep(2);
      setTimer(60);
      setCode(createEmptyCode());
      setTimeout(() => codeRefs.current[0]?.focus(), 50);
    } catch (err) {
      setError(extractMessage(err, "Impossible d'envoyer le code pour le moment"));
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (event) => {
    event.preventDefault();
    const fullCode = code.join('');

    if (fullCode.length !== 6) {
      setError('Veuillez entrer le code complet');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Les mots de passe ne correspondent pas');
      return;
    }

    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const data = await resetPassword(email, fullCode, newPassword);
      setSuccess(data.message || 'Mot de passe mis a jour avec succes.');
      setTimeout(() => navigate('/login', { replace: true }), 1200);
    } catch (err) {
      setError(extractMessage(err, 'Code invalide ou mot de passe refuse'));
    } finally {
      setLoading(false);
    }
  };

  const resendCode = async () => {
    if (timer > 0) return;
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const data = await requestPasswordReset(email);
      setSuccess(data.message || 'Un nouveau code a ete envoye si cette adresse existe.');
      setCode(createEmptyCode());
      setTimer(60);
      setTimeout(() => codeRefs.current[0]?.focus(), 50);
    } catch (err) {
      setError(extractMessage(err, "Impossible de renvoyer le code pour le moment"));
    } finally {
      setLoading(false);
    }
  };

  const handleCodeChange = (index, value) => {
    if (!/^\d*$/.test(value)) return;
    const nextCode = [...code];
    nextCode[index] = value.slice(-1);
    setCode(nextCode);
    if (value && index < 5) {
      codeRefs.current[index + 1]?.focus();
    }
  };

  const handleCodeKeyDown = (index, event) => {
    if (event.key === 'Backspace' && !code[index] && index > 0) {
      codeRefs.current[index - 1]?.focus();
    }
  };

  const handleCodePaste = (event) => {
    event.preventDefault();
    const pasted = event.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const nextCode = createEmptyCode();
    for (let index = 0; index < 6; index += 1) {
      nextCode[index] = pasted[index] || '';
    }
    setCode(nextCode);
    const nextEmpty = nextCode.findIndex((digit) => !digit);
    codeRefs.current[nextEmpty === -1 ? 5 : nextEmpty]?.focus();
  };

  return (
    <div className="login-page">
      <div className="login-page__left">
        <div className="login-page__left-content">
          <div className="login-page__brand">
            <div className="login-page__logo">
              <GraduationCap />
            </div>
            <h1 className="login-page__brand-name">Platforme Etude</h1>
          </div>

          <h2 className="login-page__headline">
            Recuperez votre acces,<br />sans compromettre la securite.
          </h2>
          <p className="login-page__desc">
            Le code de reinitialisation est temporaire, a usage unique, et votre nouveau mot de passe reste protege.
          </p>

          <div className="login-page__features">
            <div className="login-page__feature">
              <div className="login-page__feature-icon"><Mail /></div>
              <div>
                <strong>Code par email</strong>
                <span>Verification rapide depuis votre adresse</span>
              </div>
            </div>
            <div className="login-page__feature">
              <div className="login-page__feature-icon"><ShieldCheck /></div>
              <div>
                <strong>Code temporaire</strong>
                <span>Expiration automatique apres quelques minutes</span>
              </div>
            </div>
            <div className="login-page__feature">
              <div className="login-page__feature-icon"><KeyRound /></div>
              <div>
                <strong>Sessions fermees</strong>
                <span>Les anciens tokens sont revoques apres le changement</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="login-page__right">
        <div className="login-page__form-wrapper">
          <Link to="/login" className="forgot-password__back-link">
            <ArrowLeft />
            Retour a la connexion
          </Link>

          {step === 1 ? (
            <>
              <div className="login-page__form-header">
                <div className="login-page__2fa-icon">
                  <KeyRound />
                </div>
                <h2>Mot de passe oublie</h2>
                <p>Entrez votre adresse email pour recevoir un code de reinitialisation</p>
              </div>

              {error && (
                <div className="alert alert-error">
                  <AlertCircle />
                  <span>{error}</span>
                </div>
              )}

              {success && (
                <div className="alert alert-success">
                  <CheckCircle2 />
                  <span>{success}</span>
                </div>
              )}

              <form onSubmit={handleRequestCode} className="login-form">
                <div className="form-group">
                  <label className="form-label" htmlFor="forgot-email">Adresse email</label>
                  <div className="login-form__input-wrapper">
                    <Mail className="login-form__input-icon" />
                    <input
                      id="forgot-email"
                      type="email"
                      className="form-input login-form__input-with-icon"
                      placeholder="votre@email.com"
                      value={email}
                      onChange={(event) => setEmail(event.target.value)}
                      required
                      autoComplete="email"
                    />
                  </div>
                </div>

                <button type="submit" className="btn btn-primary btn-lg w-full" disabled={loading}>
                  {loading ? 'Envoi en cours...' : 'Recevoir le code'}
                  {!loading && <ArrowRight />}
                </button>
              </form>
            </>
          ) : (
            <>
              <div className="login-page__form-header">
                <div className="login-page__2fa-icon">
                  <ShieldCheck />
                </div>
                <h2>Nouveau mot de passe</h2>
                <p>Entrez le code envoye a <strong>{email}</strong></p>
              </div>

              {error && (
                <div className="alert alert-error">
                  <AlertCircle />
                  <span>{error}</span>
                </div>
              )}

              {success && (
                <div className="alert alert-success">
                  <CheckCircle2 />
                  <span>{success}</span>
                </div>
              )}

              <form onSubmit={handleResetPassword} className="login-form">
                <div className="login-form__code-group">
                  <label className="form-label">Code de reinitialisation</label>
                  <div className="login-form__code-inputs" onPaste={handleCodePaste}>
                    {code.map((digit, index) => (
                      <input
                        key={index}
                        ref={(element) => { codeRefs.current[index] = element; }}
                        type="text"
                        inputMode="numeric"
                        maxLength={1}
                        className="login-form__code-digit"
                        value={digit}
                        onChange={(event) => handleCodeChange(index, event.target.value)}
                        onKeyDown={(event) => handleCodeKeyDown(index, event)}
                        autoFocus={index === 0}
                      />
                    ))}
                  </div>
                </div>

                <div className="forgot-password__resend">
                  {timer > 0 ? (
                    <span>Renvoyer le code dans <strong>{timer}s</strong></span>
                  ) : (
                    <button type="button" className="btn btn-ghost btn-sm" onClick={resendCode} disabled={loading}>
                      Renvoyer le code
                    </button>
                  )}
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="forgot-new-password">Nouveau mot de passe</label>
                  <div className="login-form__input-wrapper">
                    <Lock className="login-form__input-icon" />
                    <input
                      id="forgot-new-password"
                      type="password"
                      className="form-input login-form__input-with-icon"
                      placeholder="Minimum 8 caracteres"
                      value={newPassword}
                      onChange={(event) => setNewPassword(event.target.value)}
                      required
                      minLength={8}
                      autoComplete="new-password"
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label className="form-label" htmlFor="forgot-confirm-password">Confirmer le mot de passe</label>
                  <div className="login-form__input-wrapper">
                    <Lock className="login-form__input-icon" />
                    <input
                      id="forgot-confirm-password"
                      type="password"
                      className="form-input login-form__input-with-icon"
                      placeholder="Repetez le mot de passe"
                      value={confirmPassword}
                      onChange={(event) => setConfirmPassword(event.target.value)}
                      required
                      minLength={8}
                      autoComplete="new-password"
                    />
                  </div>
                </div>

                <button type="submit" className="btn btn-primary btn-lg w-full" disabled={loading}>
                  {loading ? 'Mise a jour...' : 'Changer le mot de passe'}
                </button>

                <button type="button" className="btn btn-ghost w-full" onClick={() => { setStep(1); setError(''); setSuccess(''); }}>
                  Modifier l'adresse email
                </button>
              </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
