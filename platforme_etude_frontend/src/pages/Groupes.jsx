import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyAdminGroupes, createGroupe, updateGroupe, deleteGroupe } from '../api/groupeService';
import { getReceivedInvitations, acceptInvitation, refuseInvitation, createInvitation } from '../api/invitationService';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import EmptyState from '../components/ui/EmptyState';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import StatusBadge from '../components/ui/StatusBadge';
import { Users, Plus, Edit3, Trash2, MessageCircle, UserPlus, Mail, Check, X, AlertCircle } from 'lucide-react';
import './Groupes.css';

export default function Groupes() {
  const navigate = useNavigate();
  const [groupes, setGroupes] = useState([]);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [inviteModal, setInviteModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ nom: '', description: '' });
  const [inviteForm, setInviteForm] = useState({ receiverEmail: '', groupeEtudeId: '' });
  const [tab, setTab] = useState('groupes');

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [g, inv] = await Promise.allSettled([getMyAdminGroupes(), getReceivedInvitations()]);
      setGroupes(g.status === 'fulfilled' ? g.value : []);
      setInvitations(inv.status === 'fulfilled' ? inv.value : []);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const openCreate = () => {
    setEditing(null);
    setForm({ nom: '', description: '' });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (g) => {
    setEditing(g);
    setForm({ nom: g.nom, description: g.description || '' });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editing) {
        const updated = await updateGroupe(editing.id, form);
        setGroupes(groupes.map((g) => g.id === updated.id ? updated : g));
      } else {
        const created = await createGroupe(form);
        setGroupes([...groupes, created]);
      }
      setModalOpen(false);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Erreur');
    } finally { setSaving(false); }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    try {
      await deleteGroupe(deleteTarget.id);
      setGroupes(groupes.filter((g) => g.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) { console.error(err); }
    finally { setSaving(false); }
  };

  const handleInvite = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createInvitation({ receiverEmail: inviteForm.receiverEmail, groupeEtudeId: Number(inviteForm.groupeEtudeId) });
      setInviteModal(false);
      setInviteForm({ receiverEmail: '', groupeEtudeId: '' });
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Erreur');
    } finally { setSaving(false); }
  };

  const handleAccept = async (id) => {
    try {
      await acceptInvitation(id);
      setInvitations(invitations.map((inv) => inv.id === id ? { ...inv, statut: 'ACCEPTEE' } : inv));
      loadData();
    } catch (err) { console.error(err); }
  };

  const handleRefuse = async (id) => {
    try {
      await refuseInvitation(id);
      setInvitations(invitations.map((inv) => inv.id === id ? { ...inv, statut: 'REFUSEE' } : inv));
    } catch (err) { console.error(err); }
  };

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement..." /></div>;

  const pendingInvitations = invitations.filter((inv) => inv.statut === 'EN_ATTENTE');

  return (
    <div className="page">
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Groupes d'étude</h1>
            <p>Collaborez avec d'autres étudiants</p>
          </div>
          <div className="flex gap-2">
            <button className="btn btn-secondary" onClick={() => { setInviteForm({ receiverEmail: '', groupeEtudeId: groupes[0]?.id || '' }); setError(''); setInviteModal(true); }}>
              <UserPlus /> Inviter
            </button>
            <button className="btn btn-primary" onClick={openCreate}><Plus /> Nouveau groupe</button>
          </div>
        </div>
      </div>

      <div className="tabs">
        <button className={`tab ${tab === 'groupes' ? 'active' : ''}`} onClick={() => setTab('groupes')}>Mes groupes</button>
        <button className={`tab ${tab === 'invitations' ? 'active' : ''}`} onClick={() => setTab('invitations')}>
          Invitations {pendingInvitations.length > 0 && <span className="badge badge-warning" style={{ marginLeft: 6 }}>{pendingInvitations.length}</span>}
        </button>
      </div>

      {tab === 'groupes' && (
        groupes.length === 0 ? (
          <EmptyState icon={<Users />} title="Aucun groupe" description="Créez ou rejoignez un groupe d'étude." action={<button className="btn btn-primary" onClick={openCreate}><Plus /> Nouveau groupe</button>} />
        ) : (
          <div className="grid-cards">
            {groupes.map((g) => (
              <div key={g.id} className="card groupes__card">
                <div className="card-body">
                  <div className="matieres__card-header">
                    <div className="groupes__card-icon"><Users /></div>
                    <div className="flex gap-2">
                      <button className="btn btn-ghost btn-icon btn-sm" onClick={() => navigate(`/groupes/${g.id}/chat`)} title="Chat"><MessageCircle /></button>
                      <button className="btn btn-ghost btn-icon btn-sm" onClick={() => openEdit(g)}><Edit3 /></button>
                      <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(g)}><Trash2 /></button>
                    </div>
                  </div>
                  <h3 style={{ marginBottom: 6 }}>{g.nom}</h3>
                  {g.description && <p className="text-sm text-secondary" style={{ marginBottom: 12 }}>{g.description}</p>}
                  <div className="groupes__members">
                    <Users style={{ width: 14, height: 14 }} />
                    <span>{g.users?.length || 0} membre{(g.users?.length || 0) > 1 ? 's' : ''}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )
      )}

      {tab === 'invitations' && (
        invitations.length === 0 ? (
          <EmptyState icon={<Mail />} title="Aucune invitation" description="Vous n'avez reçu aucune invitation." />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Groupe</th>
                  <th>Envoyé par</th>
                  <th>Date</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {invitations.map((inv) => (
                  <tr key={inv.id}>
                    <td className="font-medium">{inv.groupeEtudeNom}</td>
                    <td className="text-sm">{inv.senderNom} ({inv.senderEmail})</td>
                    <td className="text-sm text-secondary">{new Date(inv.dateEnvoi).toLocaleDateString('fr-FR')}</td>
                    <td><StatusBadge status={inv.statut} /></td>
                    <td>
                      {inv.statut === 'EN_ATTENTE' && (
                        <div className="flex gap-2">
                          <button className="btn btn-primary btn-sm" onClick={() => handleAccept(inv.id)}><Check /> Accepter</button>
                          <button className="btn btn-secondary btn-sm" onClick={() => handleRefuse(inv.id)}><X /> Refuser</button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Modifier le groupe' : 'Nouveau groupe'} size="sm">
        {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Nom du groupe</label>
            <input className="form-input" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} required maxLength={120} />
          </div>
          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea className="form-textarea" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} maxLength={1000} rows={3} />
          </div>
          <div className="modal__footer" style={{ padding: '16px 0 0', border: 'none' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Annuler</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? '...' : 'Enregistrer'}</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={inviteModal} onClose={() => setInviteModal(false)} title="Inviter un membre" size="sm">
        {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}
        <form onSubmit={handleInvite}>
          <div className="form-group">
            <label className="form-label">Groupe</label>
            <select className="form-select" value={inviteForm.groupeEtudeId} onChange={(e) => setInviteForm({ ...inviteForm, groupeEtudeId: e.target.value })} required>
              <option value="">Sélectionner un groupe</option>
              {groupes.map((g) => <option key={g.id} value={g.id}>{g.nom}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Email du membre</label>
            <input type="email" className="form-input" value={inviteForm.receiverEmail} onChange={(e) => setInviteForm({ ...inviteForm, receiverEmail: e.target.value })} required placeholder="email@example.com" />
          </div>
          <div className="modal__footer" style={{ padding: '16px 0 0', border: 'none' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setInviteModal(false)}>Annuler</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? '...' : 'Envoyer l\'invitation'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Supprimer le groupe" message={`Supprimer "${deleteTarget?.nom}" ?`} loading={saving} />
    </div>
  );
}
