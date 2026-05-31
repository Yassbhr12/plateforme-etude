import { useEffect, useMemo, useState } from 'react';
import { Bell, Eye, MailCheck, MailWarning, Search, Trash2, X } from 'lucide-react';
import { deleteAdminNotification, getAllNotifications } from '../../api/adminService';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import EmptyState from '../../components/ui/EmptyState';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import StatCard from '../../components/ui/StatCard';
import { formatDateTime, matchesSearch, readLabel, statusClass, textValue } from './adminUtils';
import './AdminCommon.css';

const READ_FILTERS = [
  { key: 'all', label: 'Toutes' },
  { key: 'unread', label: 'Non lues' },
  { key: 'read', label: 'Lues' },
];

export default function AdminNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [readFilter, setReadFilter] = useState('all');
  const [selected, setSelected] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function loadNotifications() {
    setLoading(true);
    setError('');
    try {
      setNotifications(await getAllNotifications());
    } catch (err) {
      console.error('Failed to load admin notifications:', err);
      setError("Impossible de charger les notifications.");
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => loadNotifications(), 0);
    return () => window.clearTimeout(timer);
  }, []);

  const filtered = useMemo(() => {
    return notifications.filter((notification) => {
      const readOk =
        readFilter === 'all' ||
        (readFilter === 'read' && notification.lue) ||
        (readFilter === 'unread' && !notification.lue);
      return readOk && matchesSearch(notification, search);
    });
  }, [notifications, search, readFilter]);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    setError('');
    try {
      await deleteAdminNotification(deleteTarget.id);
      setNotifications((prev) => prev.filter((n) => n.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) {
      console.error('Failed to delete notification:', err);
      setError("La notification n'a pas pu etre supprimee.");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="page flex justify-center" style={{ paddingTop: 80 }}>
        <LoadingSpinner size="lg" text="Chargement des notifications..." />
      </div>
    );
  }

  return (
    <div className="page">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="admin-kpi-grid">
        <StatCard icon={<Bell />} label="Notifications" value={notifications.length} color="primary" />
        <StatCard icon={<MailWarning />} label="Non lues" value={notifications.filter((n) => !n.lue).length} color="warning" />
        <StatCard icon={<MailCheck />} label="Lues" value={notifications.filter((n) => n.lue).length} color="success" />
        <StatCard icon={<Bell />} label="Types" value={new Set(notifications.map((n) => n.type)).size} color="accent" />
      </div>

      <div className="admin-toolbar">
        <div className="admin-search">
          <Search />
          <input
            className="form-input"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Rechercher une notification..."
          />
        </div>
        <div className="admin-filter-row">
          {READ_FILTERS.map((item) => (
            <button
              key={item.key}
              className={`admin-filter-chip ${readFilter === item.key ? 'active' : ''}`}
              onClick={() => setReadFilter(item.key)}
            >
              {item.label}
            </button>
          ))}
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState icon={<Bell />} title="Aucune notification trouvee" description="Ajustez la recherche ou le filtre" />
      ) : (
        <div className="card admin-table-card">
          <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
            <table className="table">
              <thead>
                <tr>
                  <th>Utilisateur</th>
                  <th>Message</th>
                  <th>Type</th>
                  <th>Date</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((notification) => (
                  <tr key={notification.id}>
                    <td>
                      <div>{notification.userNom || '-'}</div>
                      <span className="text-sm text-secondary">{notification.userEmail}</span>
                    </td>
                    <td className="text-sm truncate" style={{ maxWidth: 360 }}>{notification.message || '-'}</td>
                    <td><span className={`badge ${statusClass(notification.type)}`}>{notification.type || '-'}</span></td>
                    <td className="text-sm">{formatDateTime(notification.dateEnvoi)}</td>
                    <td><span className={`badge ${notification.lue ? 'badge-success' : 'badge-warning'}`}>{readLabel(notification.lue)}</span></td>
                    <td>
                      <div className="admin-actions">
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(notification)} title="Details">
                          <Eye />
                        </button>
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(notification)} title="Supprimer">
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
              <h3>Details de la notification</h3>
              <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(null)}>
                <X />
              </button>
            </div>
            <div className="admin-modal__body">
              <div className="admin-detail-grid">
                {[
                  ['ID', selected.id],
                  ['Utilisateur', selected.userNom],
                  ['Email', selected.userEmail],
                  ['Type', selected.type],
                  ['Date', formatDateTime(selected.dateEnvoi)],
                  ['Statut', readLabel(selected.lue)],
                ].map(([label, value]) => (
                  <div className="admin-detail-field" key={label}>
                    <span>{label}</span>
                    <strong>{textValue(value)}</strong>
                  </div>
                ))}
                <div className="admin-detail-field admin-detail-field--wide">
                  <span>Message</span>
                  <strong>{textValue(selected.message)}</strong>
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
        title="Supprimer la notification"
        message="Supprimer cette notification ?"
        loading={saving}
      />
    </div>
  );
}
