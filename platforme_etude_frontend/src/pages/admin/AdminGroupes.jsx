import { useEffect, useMemo, useState } from 'react';
import { Eye, Search, Trash2, UserRound, UsersRound, X } from 'lucide-react';
import { deleteAdminGroupe, getAllGroupes } from '../../api/adminService';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import EmptyState from '../../components/ui/EmptyState';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import StatCard from '../../components/ui/StatCard';
import { matchesSearch, textValue } from './adminUtils';
import './AdminCommon.css';

export default function AdminGroupes() {
  const [groupes, setGroupes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadGroupes() {
    setLoading(true);
    setError('');
    try {
      setGroupes(await getAllGroupes());
    } catch (err) {
      console.error('Failed to load admin groupes:', err);
      setError("Impossible de charger les groupes.");
      setGroupes([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => loadGroupes(), 0);
    return () => window.clearTimeout(timer);
  }, []);

  const filtered = useMemo(
    () => groupes.filter((groupe) => matchesSearch(groupe, search)),
    [groupes, search]
  );

  const totalMembers = groupes.reduce((total, groupe) => total + (groupe.users?.length || 0), 0);
  const withAdmin = groupes.filter((groupe) => groupe.adminId).length;

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    setError('');
    try {
      await deleteAdminGroupe(deleteTarget.id);
      setGroupes((prev) => prev.filter((g) => g.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) {
      console.error('Failed to delete groupe:', err);
      setError("Le groupe n'a pas pu etre supprime.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="page flex justify-center" style={{ paddingTop: 80 }}>
        <LoadingSpinner size="lg" text="Chargement des groupes..." />
      </div>
    );
  }

  return (
    <div className="page">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="admin-kpi-grid">
        <StatCard icon={<UsersRound />} label="Groupes" value={groupes.length} color="accent" />
        <StatCard icon={<UserRound />} label="Membres cumules" value={totalMembers} color="primary" />
        <StatCard icon={<UserRound />} label="Avec admin" value={withAdmin} color="success" />
        <StatCard icon={<UsersRound />} label="Sans membres" value={groupes.filter((g) => !g.users?.length).length} color="warning" />
      </div>

      <div className="admin-toolbar">
        <div className="admin-search">
          <Search />
          <input
            className="form-input"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Rechercher un groupe..."
          />
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState icon={<UsersRound />} title="Aucun groupe trouve" description="Ajustez votre recherche" />
      ) : (
        <div className="card admin-table-card">
          <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
            <table className="table">
              <thead>
                <tr>
                  <th>Groupe</th>
                  <th>Admin</th>
                  <th>Membres</th>
                  <th>Description</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((groupe) => (
                  <tr key={groupe.id}>
                    <td>
                      <div className="font-medium">{groupe.nom || '-'}</div>
                      <span className="text-sm text-secondary">ID: {groupe.id}</span>
                    </td>
                    <td>
                      <div>{groupe.adminNom || '-'}</div>
                      <span className="text-sm text-secondary">{groupe.adminEmail}</span>
                    </td>
                    <td><span className="badge badge-accent">{groupe.users?.length || 0}</span></td>
                    <td className="text-sm text-secondary truncate" style={{ maxWidth: 320 }}>{groupe.description || '-'}</td>
                    <td>
                      <div className="admin-actions">
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(groupe)} title="Details">
                          <Eye />
                        </button>
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(groupe)} title="Supprimer">
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
              <h3>Details du groupe</h3>
              <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(null)}>
                <X />
              </button>
            </div>
            <div className="admin-modal__body">
              <div className="admin-detail-grid">
                <div className="admin-detail-field">
                  <span>ID</span>
                  <strong>{selected.id}</strong>
                </div>
                <div className="admin-detail-field">
                  <span>Nom</span>
                  <strong>{textValue(selected.nom)}</strong>
                </div>
                <div className="admin-detail-field">
                  <span>Admin</span>
                  <strong>{textValue(selected.adminNom)}</strong>
                </div>
                <div className="admin-detail-field">
                  <span>Email admin</span>
                  <strong>{textValue(selected.adminEmail)}</strong>
                </div>
                <div className="admin-detail-field admin-detail-field--wide">
                  <span>Description</span>
                  <strong>{textValue(selected.description)}</strong>
                </div>
                <div className="admin-detail-field admin-detail-field--wide">
                  <span>Membres</span>
                  <strong>
                    {selected.users?.length
                      ? selected.users.map((u) => `${u.prenom || ''} ${u.nom || ''}`.trim() || u.email).join(', ')
                      : '-'}
                  </strong>
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
        title="Supprimer le groupe"
        message={`Supprimer "${deleteTarget?.nom || 'ce groupe'}" ?`}
        loading={saving}
      />
    </div>
  );
}
