import { useEffect, useMemo, useState } from 'react';
import {
  CalendarClock,
  CheckCircle2,
  Clock,
  Eye,
  Lock,
  Search,
  Trash2,
  Users,
  X,
} from 'lucide-react';
import { deleteAdminSession, getAllSessions } from '../../api/adminService';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import EmptyState from '../../components/ui/EmptyState';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import StatCard from '../../components/ui/StatCard';
import StatusBadge from '../../components/ui/StatusBadge';
import { formatDateTime, matchesSearch, textValue } from './adminUtils';
import './AdminCommon.css';

const STATUS_FILTERS = [
  { key: 'all', label: 'Toutes' },
  { key: 'PLANIFIEE', label: 'Planifiees' },
  { key: 'TERMINEE', label: 'Terminees' },
  { key: 'ANNULEE', label: 'Annulees' },
];

export default function AdminSessions() {
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [status, setStatus] = useState('all');
  const [selected, setSelected] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadSessions() {
    setLoading(true);
    setError('');
    try {
      setSessions(await getAllSessions());
    } catch (err) {
      console.error('Failed to load admin sessions:', err);
      setError("Impossible de charger les sessions.");
      setSessions([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => loadSessions(), 0);
    return () => window.clearTimeout(timer);
  }, []);

  const filtered = useMemo(() => {
    return sessions.filter((session) => {
      const statusOk = status === 'all' || session.statut === status;
      return statusOk && matchesSearch(session, search);
    });
  }, [sessions, search, status]);

  const stats = {
    planned: sessions.filter((s) => s.statut === 'PLANIFIEE').length,
    completed: sessions.filter((s) => s.statut === 'TERMINEE').length,
    cancelled: sessions.filter((s) => s.statut === 'ANNULEE').length,
    privateCount: sessions.filter((s) => s.privee).length,
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    setError('');
    try {
      await deleteAdminSession(deleteTarget.id);
      setSessions((prev) => prev.filter((s) => s.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) {
      console.error('Failed to delete session:', err);
      setError("La session n'a pas pu etre supprimee.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="page flex justify-center" style={{ paddingTop: 80 }}>
        <LoadingSpinner size="lg" text="Chargement des sessions..." />
      </div>
    );
  }

  return (
    <div className="page">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="admin-kpi-grid">
        <StatCard icon={<CalendarClock />} label="Sessions" value={sessions.length} color="primary" />
        <StatCard icon={<Clock />} label="Planifiees" value={stats.planned} color="primary" />
        <StatCard icon={<CheckCircle2 />} label="Terminees" value={stats.completed} color="success" />
        <StatCard icon={<Lock />} label="Privees" value={stats.privateCount} color="accent" />
      </div>

      <div className="admin-toolbar">
        <div className="admin-search">
          <Search />
          <input
            className="form-input"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Rechercher une session..."
          />
        </div>
        <div className="admin-filter-row">
          {STATUS_FILTERS.map((item) => (
            <button
              key={item.key}
              className={`admin-filter-chip ${status === item.key ? 'active' : ''}`}
              onClick={() => setStatus(item.key)}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState icon={<CalendarClock />} title="Aucune session trouvee" description="Ajustez la recherche ou le filtre" />
      ) : (
        <div className="card admin-table-card">
          <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
            <table className="table">
              <thead>
                <tr>
                  <th>Titre</th>
                  <th>Utilisateur</th>
                  <th>Matiere</th>
                  <th>Groupe</th>
                  <th>Date debut</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((session) => (
                  <tr key={session.id}>
                    <td className="font-medium">{session.titre || '-'}</td>
                    <td>
                      <div>{session.userNom || '-'}</div>
                      <span className="text-sm text-secondary">{session.userEmail}</span>
                    </td>
                    <td>{session.matiereNom ? <span className="badge badge-primary">{session.matiereNom}</span> : '-'}</td>
                    <td>{session.groupeEtudeNom || '-'}</td>
                    <td className="text-sm">{formatDateTime(session.dateDebut)}</td>
                    <td><StatusBadge status={session.statut} /></td>
                    <td>
                      <div className="admin-actions">
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(session)} title="Details">
                          <Eye />
                        </button>
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(session)} title="Supprimer">
                          <Trash2 />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {selected && (
        <div className="overlay" onClick={() => setSelected(null)}>
          <div className="admin-modal" onClick={(e) => e.stopPropagation()}>
            <div className="admin-modal__header">
              <h3>Details de la session</h3>
              <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(null)}>
                <X />
              </button>
            </div>
            <div className="admin-modal__body">
              <div className="admin-detail-grid">
                {[
                  ['ID', selected.id],
                  ['Titre', selected.titre],
                  ['Utilisateur', selected.userNom],
                  ['Email utilisateur', selected.userEmail],
                  ['Matiere', selected.matiereNom],
                  ['Groupe', selected.groupeEtudeNom],
                  ['Date debut', formatDateTime(selected.dateDebut)],
                  ['Date fin', formatDateTime(selected.dateFin)],
                  ['Duree max', selected.dureeMax ? `${selected.dureeMax} min` : '-'],
                  ['Privee', textValue(selected.privee)],
                ].map(([label, value]) => (
                  <div className="admin-detail-field" key={label}>
                    <span>{label}</span>
                    <strong>{textValue(value)}</strong>
                  </div>
                ))}
                <div className="admin-detail-field admin-detail-field--wide">
                  <span>Statut</span>
                  <strong>{selected.statut || '-'}</strong>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Supprimer la session"
        message={`Supprimer "${deleteTarget?.titre || 'cette session'}" ?`}
        loading={saving}
      />
    </div>
  );
}
