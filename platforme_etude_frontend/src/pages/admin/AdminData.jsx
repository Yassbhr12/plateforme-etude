import { createElement, useEffect, useMemo, useState } from 'react';
import {
  BookOpen,
  CheckSquare,
  Clock,
  Eye,
  Flag,
  MessageCircle,
  Search,
  Send,
  Target,
  Trash2,
  X,
} from 'lucide-react';
import {
  deleteAdminCommentaire,
  deleteAdminDisponibilite,
  deleteAdminInvitation,
  deleteAdminMatiere,
  deleteAdminMessage,
  deleteAdminObjectif,
  getAllCommentaires,
  getAllDisponibilites,
  getAllInvitations,
  getAllMatieres,
  getAllMessages,
  getAllObjectifs,
} from '../../api/adminService';
import ConfirmDialog from '../../components/ui/ConfirmDialog';
import EmptyState from '../../components/ui/EmptyState';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { dayName, formatDate, formatDateTime, matchesSearch, statusClass, textValue } from './adminUtils';
import './AdminCommon.css';

const SECTIONS = [
  {
    key: 'matieres',
    label: 'Matieres',
    icon: BookOpen,
    loader: getAllMatieres,
    deleter: deleteAdminMatiere,
    columns: [
      { key: 'nom', label: 'Matiere', render: (item) => item.nom || '-' },
      { key: 'userNom', label: 'Utilisateur', render: (item) => item.userNom || '-' },
      { key: 'userEmail', label: 'Email', render: (item) => item.userEmail || '-' },
      { key: 'priorite', label: 'Priorite', render: (item) => <span className="badge badge-warning">{item.priorite ?? '-'}</span> },
    ],
  },
  {
    key: 'objectifs',
    label: 'Objectifs',
    icon: Target,
    loader: getAllObjectifs,
    deleter: deleteAdminObjectif,
    columns: [
      { key: 'matiereNom', label: 'Matiere', render: (item) => item.matiereNom || '-' },
      { key: 'userNom', label: 'Utilisateur', render: (item) => item.userNom || '-' },
      { key: 'semaine', label: 'Semaine', render: (item) => formatDate(item.semaine) },
      { key: 'heuresCibles', label: 'Heures', render: (item) => <span className="badge badge-accent">{item.heuresCibles ?? '-'}</span> },
    ],
  },
  {
    key: 'disponibilites',
    label: 'Disponibilites',
    icon: Clock,
    loader: getAllDisponibilites,
    deleter: deleteAdminDisponibilite,
    columns: [
      { key: 'jourSemaine', label: 'Jour', render: (item) => dayName(item.jourSemaine) },
      { key: 'heureDebut', label: 'Debut', render: (item) => item.heureDebut || '-' },
      { key: 'heureFin', label: 'Fin', render: (item) => item.heureFin || '-' },
      { key: 'userNom', label: 'Utilisateur', render: (item) => item.userNom || '-' },
    ],
  },
  {
    key: 'invitations',
    label: 'Invitations',
    icon: Send,
    loader: getAllInvitations,
    deleter: deleteAdminInvitation,
    columns: [
      { key: 'groupeEtudeNom', label: 'Groupe', render: (item) => item.groupeEtudeNom || '-' },
      { key: 'senderNom', label: 'Expediteur', render: (item) => item.senderNom || '-' },
      { key: 'receiverNom', label: 'Recepteur', render: (item) => item.receiverNom || '-' },
      { key: 'statut', label: 'Statut', render: (item) => <span className={`badge ${statusClass(item.statut)}`}>{item.statut || '-'}</span> },
    ],
  },
  {
    key: 'messages',
    label: 'Messages',
    icon: MessageCircle,
    loader: getAllMessages,
    deleter: deleteAdminMessage,
    columns: [
      { key: 'contenu', label: 'Message', render: (item) => <span className="truncate" style={{ maxWidth: 360, display: 'block' }}>{item.contenu || '-'}</span> },
      { key: 'userNom', label: 'Utilisateur', render: (item) => item.userNom || '-' },
      { key: 'groupeEtudeNom', label: 'Groupe', render: (item) => item.groupeEtudeNom || '-' },
      { key: 'dateEnvoi', label: 'Date', render: (item) => formatDateTime(item.dateEnvoi) },
    ],
  },
  {
    key: 'commentaires',
    label: 'Commentaires',
    icon: Flag,
    loader: getAllCommentaires,
    deleter: deleteAdminCommentaire,
    columns: [
      { key: 'contenu', label: 'Commentaire', render: (item) => <span className="truncate" style={{ maxWidth: 360, display: 'block' }}>{item.contenu || '-'}</span> },
      { key: 'userNom', label: 'Utilisateur', render: (item) => item.userNom || '-' },
      { key: 'sessionEtudeTitre', label: 'Session', render: (item) => item.sessionEtudeTitre || '-' },
      { key: 'id', label: 'ID', render: (item) => item.id },
    ],
  },
];

export default function AdminData() {
  const [activeKey, setActiveKey] = useState(SECTIONS[0].key);
  const [dataByKey, setDataByKey] = useState({});
  const [loadingKey, setLoadingKey] = useState('');
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const activeSection = SECTIONS.find((section) => section.key === activeKey) || SECTIONS[0];
  const rows = useMemo(() => dataByKey[activeKey] || [], [activeKey, dataByKey]);

  async function loadSection(section) {
    setLoadingKey(section.key);
    setError('');
    try {
      const data = await section.loader();
      setDataByKey((prev) => ({ ...prev, [section.key]: data }));
    } catch (err) {
      console.error(`Failed to load admin ${section.key}:`, err);
      setError(`Impossible de charger ${section.label.toLowerCase()}.`);
      setDataByKey((prev) => ({ ...prev, [section.key]: [] }));
    } finally {
      setLoadingKey('');
    }
  }

  useEffect(() => {
    if (!dataByKey[activeKey]) {
      const timer = window.setTimeout(() => loadSection(activeSection), 0);
      return () => window.clearTimeout(timer);
    }
    return undefined;
  }, [activeKey, activeSection, dataByKey]);

  const filteredRows = useMemo(
    () => rows.filter((item) => matchesSearch(item, search)),
    [rows, search]
  );

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    setError('');
    try {
      await activeSection.deleter(deleteTarget.id);
      setDataByKey((prev) => ({
        ...prev,
        [activeKey]: (prev[activeKey] || []).filter((item) => item.id !== deleteTarget.id),
      }));
      setDeleteTarget(null);
    } catch (err) {
      console.error(`Failed to delete admin ${activeKey}:`, err);
      setError("La suppression n'a pas pu etre effectuee.");
    } finally {
      setSaving(false);
    }
  };

  const isLoading = loadingKey === activeKey;

  return (
    <div className="page">
      {error && <div className="alert alert-error">{error}</div>}

      <div className="tabs admin-page-tabs">
        {SECTIONS.map(({ key, label, icon }) => (
          <button
            key={key}
            className={`tab ${activeKey === key ? 'active' : ''}`}
            onClick={() => {
              setActiveKey(key);
              setSearch('');
            }}
          >
            {createElement(icon, { style: { width: 16, height: 16, marginRight: 6, verticalAlign: -3 } })}
            {label}
          </button>
        ))}
      </div>

      <div className="admin-toolbar">
        <div className="admin-search">
          <Search />
          <input
            className="form-input"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder={`Rechercher dans ${activeSection.label.toLowerCase()}...`}
          />
        </div>
        <button className="btn btn-secondary btn-sm" onClick={() => loadSection(activeSection)} disabled={isLoading}>
          Actualiser
        </button>
      </div>

      {isLoading ? (
        <LoadingSpinner size="lg" text={`Chargement de ${activeSection.label.toLowerCase()}...`} />
      ) : filteredRows.length === 0 ? (
        <EmptyState icon={<CheckSquare />} title="Aucune donnee trouvee" description="Ajustez la recherche ou changez d'onglet" />
      ) : (
        <div className="card admin-table-card">
          <div className="table-container" style={{ border: 'none', borderRadius: 0 }}>
            <table className="table">
              <thead>
                <tr>
                  {activeSection.columns.map((column) => (
                    <th key={column.key}>{column.label}</th>
                  ))}
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredRows.map((item) => (
                  <tr key={item.id}>
                    {activeSection.columns.map((column) => (
                      <td key={column.key}>{column.render(item)}</td>
                    ))}
                    <td>
                      <div className="admin-actions">
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(item)} title="Details">
                          <Eye />
                        </button>
                        <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(item)} title="Supprimer">
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
              <h3>Details</h3>
              <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setSelected(null)}>
                <X />
              </button>
            </div>
            <div className="admin-modal__body">
              <div className="admin-detail-grid">
                {Object.entries(selected).map(([key, value]) => (
                  <div
                    className={`admin-detail-field ${String(value).length > 80 ? 'admin-detail-field--wide' : ''}`}
                    key={key}
                  >
                    <span>{key}</span>
                    <strong>{textValue(Array.isArray(value) ? value.length : value)}</strong>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Supprimer"
        message={`Supprimer cet element de ${activeSection.label.toLowerCase()} ?`}
        loading={saving}
      />
    </div>
  );
}
