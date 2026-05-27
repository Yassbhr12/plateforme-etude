import { useState, useEffect } from 'react';
import { getSessions, createSession, updateSession, cancelSession, markSessionDone, deleteSession } from '../api/sessionService';
import { getMatieres } from '../api/matiereService';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import EmptyState from '../components/ui/EmptyState';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import StatusBadge from '../components/ui/StatusBadge';
import { CalendarClock, Plus, Edit3, Trash2, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import './Sessions.css';

export default function Sessions() {
  const [sessions, setSessions] = useState([]);
  const [matieres, setMatieres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('all');
  const [form, setForm] = useState({ titre: '', dateDebut: '', dateFin: '', dureeMax: 60, matiereId: '' });

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [s, m] = await Promise.all([getSessions(), getMatieres()]);
      setSessions(s);
      setMatieres(m);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const openCreate = () => {
    setEditing(null);
    setForm({ titre: '', dateDebut: '', dateFin: '', dureeMax: 60, matiereId: matieres[0]?.id || '' });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (s) => {
    setEditing(s);
    setForm({
      titre: s.titre,
      dateDebut: s.dateDebut?.slice(0, 16) || '',
      dateFin: s.dateFin?.slice(0, 16) || '',
      dureeMax: s.dureeMax,
      matiereId: s.matiereId,
    });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      const payload = { ...form, matiereId: Number(form.matiereId), dureeMax: Number(form.dureeMax) };
      if (editing) {
        const updated = await updateSession(editing.id, payload);
        setSessions(sessions.map((s) => (s.id === updated.id ? updated : s)));
      } else {
        const created = await createSession(payload);
        setSessions([...sessions, created]);
      }
      setModalOpen(false);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Erreur lors de la sauvegarde');
    } finally { setSaving(false); }
  };

  const handleDone = async (id) => {
    try {
      await markSessionDone(id);
      setSessions(sessions.map((s) => s.id === id ? { ...s, statut: 'TERMINEE' } : s));
    } catch (err) { console.error(err); }
  };

  const handleCancel = async (id) => {
    try {
      await cancelSession(id);
      setSessions(sessions.map((s) => s.id === id ? { ...s, statut: 'ANNULEE' } : s));
    } catch (err) { console.error(err); }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    try {
      await deleteSession(deleteTarget.id);
      setSessions(sessions.filter((s) => s.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) { console.error(err); }
    finally { setSaving(false); }
  };

  const filtered = filter === 'all' ? sessions : sessions.filter((s) => s.statut === filter);

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement des sessions..." /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Sessions d'étude</h1>
            <p>Planifiez et suivez vos sessions d'étude</p>
          </div>
          <button className="btn btn-primary" onClick={openCreate}><Plus /> Nouvelle session</button>
        </div>
      </div>

      <div className="tabs">
        {[
          { key: 'all', label: 'Toutes' },
          { key: 'PLANIFIEE', label: 'Planifiées' },
          { key: 'TERMINEE', label: 'Terminées' },
          { key: 'ANNULEE', label: 'Annulées' },
        ].map(({ key, label }) => (
          <button key={key} className={`tab ${filter === key ? 'active' : ''}`} onClick={() => setFilter(key)}>{label}</button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <EmptyState icon={<CalendarClock />} title="Aucune session" description="Créez votre première session d'étude." action={<button className="btn btn-primary" onClick={openCreate}><Plus /> Nouvelle session</button>} />
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Titre</th>
                <th>Matière</th>
                <th>Début</th>
                <th>Fin</th>
                <th>Durée</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((s) => (
                <tr key={s.id}>
                  <td className="font-medium">{s.titre}</td>
                  <td><span className="badge badge-primary">{s.matiereNom}</span></td>
                  <td className="text-sm text-secondary">{s.dateDebut ? new Date(s.dateDebut).toLocaleString('fr-FR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '-'}</td>
                  <td className="text-sm text-secondary">{s.dateFin ? new Date(s.dateFin).toLocaleString('fr-FR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' }) : '-'}</td>
                  <td className="text-sm">{s.dureeMax} min</td>
                  <td><StatusBadge status={s.statut} /></td>
                  <td>
                    <div className="flex gap-2">
                      {s.statut === 'PLANIFIEE' && (
                        <>
                          <button className="btn btn-ghost btn-icon btn-sm" onClick={() => handleDone(s.id)} title="Marquer terminée"><CheckCircle /></button>
                          <button className="btn btn-ghost btn-icon btn-sm" onClick={() => handleCancel(s.id)} title="Annuler"><XCircle /></button>
                          <button className="btn btn-ghost btn-icon btn-sm" onClick={() => openEdit(s)} title="Modifier"><Edit3 /></button>
                        </>
                      )}
                      <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(s)} title="Supprimer"><Trash2 /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Modifier la session' : 'Nouvelle session'}>
        {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Titre</label>
            <input className="form-input" value={form.titre} onChange={(e) => setForm({ ...form, titre: e.target.value })} required maxLength={200} placeholder="ex: Révision algèbre" />
          </div>
          <div className="form-group">
            <label className="form-label">Matière</label>
            <select className="form-select" value={form.matiereId} onChange={(e) => setForm({ ...form, matiereId: e.target.value })} required>
              <option value="">Sélectionner une matière</option>
              {matieres.map((m) => <option key={m.id} value={m.id}>{m.nom}</option>)}
            </select>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Date de début</label>
              <input type="datetime-local" className="form-input" value={form.dateDebut} onChange={(e) => setForm({ ...form, dateDebut: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Date de fin</label>
              <input type="datetime-local" className="form-input" value={form.dateFin} onChange={(e) => setForm({ ...form, dateFin: e.target.value })} required />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Durée max (minutes)</label>
            <input type="number" className="form-input" value={form.dureeMax} onChange={(e) => setForm({ ...form, dureeMax: e.target.value })} min={1} max={180} required />
          </div>
          <div className="modal__footer" style={{ padding: '16px 0 0', border: 'none' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Annuler</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Enregistrement...' : 'Enregistrer'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Supprimer la session" message={`Supprimer "${deleteTarget?.titre}" ?`} loading={saving} />
    </div>
  );
}
