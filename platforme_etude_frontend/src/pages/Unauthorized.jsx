import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft, Home } from 'lucide-react';
import './Unauthorized.css';

export default function Unauthorized() {
  return (
    <div className="unauthorized-page">
      <div className="unauthorized-page__card">
        <div className="unauthorized-page__icon">
          <ShieldAlert />
        </div>
        <h1>Accès non autorisé</h1>
        <p>
          Vous n'avez pas les permissions nécessaires pour accéder à cette page.
          Contactez un administrateur si vous pensez qu'il s'agit d'une erreur.
        </p>
        <div className="unauthorized-page__actions">
          <Link to="/dashboard" className="btn btn-primary">
            <Home />
            Retour au dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}
