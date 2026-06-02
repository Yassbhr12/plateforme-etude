import { Link } from 'react-router-dom';
import { Home, ArrowLeft, Search } from 'lucide-react';
import './NotFound.css';

export default function NotFound() {
  return (
    <div className="not-found">
      <div className="not-found__content">
        <div className="not-found__icon">
          <Search />
        </div>
        <h1 className="not-found__code">404</h1>
        <h2 className="not-found__title">Page introuvable</h2>
        <p className="not-found__desc">
          La page que vous recherchez n'existe pas ou a été déplacée.
        </p>
        <div className="not-found__actions">
          <Link to="/dashboard" className="btn btn-primary btn-lg">
            <Home /> Retour au dashboard
          </Link>
          <button
            type="button"
            className="btn btn-secondary btn-lg"
            onClick={() => window.history.back()}
          >
            <ArrowLeft /> Page précédente
          </button>
        </div>
      </div>
    </div>
  );
}
