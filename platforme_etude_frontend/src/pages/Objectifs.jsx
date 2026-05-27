import { useState, useEffect } from 'react';
import { getObjectifs, getObjectifsByWeek, createObjectif, updateObjectif, deleteObjectif } from '../api/objectifService';
import { getMatieres } from '../api/matiereService';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import EmptyState from '../components/ui/EmptyState';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Target, Plus, Edit3, Trash2, AlertCircle, BookOpen } from 'lucide-react';
import './Objectifs.css';

export default function Objectifs() {
  const [objectifs, setObjectifs] = useState([]);
  const [matieres, setMatieres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ semaine: '', heuresCibles: 5, matiereId: '' });

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try {
      const [o, m] = await Promise.all([getObjectifs(), getMatieres()]);
      setObjectifs(o);
      setMatieres(m);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const openCreate = () => {
    setEditing(null);
    const today = new Date();
    const day = today.getDay();
    const monday = new Date(today);
    monday.setDate(today.getDate() - (day === 0 ? 6 : day - 1));
    setForm({ semaine: monday.toISOString().split('T')[0], heuresCibles: 5, matiereId: matieres[0]?.id || '' });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (o) => {
    setEditing(o);
    setForm({ semaine: o.semaine, heuresCibles: o.heuresCibles, matiereId: o.matiereId });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      const payload = { ...form, matiereId: Number(form.matiereId), heuresCibles: Number(form.heuresCibles) };
      if (editing) {
        const updated = await updateObjectif(editing.id, payload);
        setObjectifs(objectifs.map((o) => (o.id === updated.id ? updated : o)));
      } else {
        const created = await createObjectif(payload);
        setObjectifs([...objectifs, created]);
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
      await deleteObjectif(deleteTarget.id);
      setObjectifs(objectifs.filter((o) => o.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) { console.error(err); }
    finally { setSaving(false); }
  };

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement des objectifs..." /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Objectifs hebdomadaires</h1>
            <p>Définissez vos objectifs d'heures d'étude par matière et par semaine</p>
          </div>
          <button className="btn btn-primary" onClick={openCreate}><Plus /> Nouvel objectif</button>
        </div>
      </div>

      {objectifs.length === 0 ? (
        <EmptyState icon={<Target />} title="Aucun objectif" description="Définissez vos premiers objectifs hebdomadaires." action={<button className="btn btn-primary" onClick={openCreate}><Plus /> Nouvel objectif</button>} />
      ) : (
        <div className="grid-cards">
          {objectifs.map((o) => (
            <div key={o.id} className="card objectifs__card">
              <div className="card-body">
                <div className="matieres__card-header">
                  <div className="objectifs__card-icon"><Target /></div>
                  <div className="matieres__card-actions" style={{ opacity: 1 }}>
                    <button className="btn btn-ghost btn-icon btn-sm" onClick={() => openEdit(o)}><Edit3 /></button>
                    <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(o)}><Trash2 /></button>
                  </div>
                </div>
                <div className="objectifs__card-matiere">
                  <BookOpen style={{ width: 14, height: 14 }} />
                  <span>{o.matiereNom}</span>
                </div>
                <div className="objectifs__card-hours">
                  <span className="objectifs__card-value">{o.heuresCibles}h</span>
                  <span className="text-sm text-secondary">par semaine</span>
                </div>
                <div className="objectifs__card-week">
                  Semaine du {new Date(o.semaine).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long' })}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Modifier l\'objectif' : 'Nouvel objectif'} size="sm">
        {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Matière</label>
            <select className="form-select" value={form.matiereId} onChange={(e) => setForm({ ...form, matiereId: e.target.value })} required>
              <option value="">Sélectionner</option>
              {matieres.map((m) => <option key={m.id} value={m.id}>{m.nom}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Heures cibles par semaine</label>
            <input type="number" className="form-input" value={form.heuresCibles} onChange={(e) => setForm({ ...form, heuresCibles: e.target.value })} min={1} max={168} required />
          </div>
          <div className="form-group">
            <label className="form-label">Semaine (date du lundi)</label>
            <input type="date" className="form-input" value={form.semaine} onChange={(e) => setForm({ ...form, semaine: e.target.value })} required />
          </div>
          <div className="modal__footer" style={{ padding: '16px 0 0', border: 'none' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Annuler</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Enregistrement...' : 'Enregistrer'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Supprimer l'objectif" message="Supprimer cet objectif hebdomadaire ?" loading={saving} />
    </div>
  );
}
