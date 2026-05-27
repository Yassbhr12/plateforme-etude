import { useState, useEffect } from 'react';
import { getDisponibilites, createDisponibilite, updateDisponibilite, deleteDisponibilite } from '../api/disponibiliteService';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import EmptyState from '../components/ui/EmptyState';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { Clock, Plus, Edit3, Trash2, AlertCircle } from 'lucide-react';
import './Disponibilites.css';

const JOURS = ['', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];

export default function Disponibilites() {
  const [dispos, setDispos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({ jourSemaine: 1, heureDebut: '08:00', heureFin: '10:00' });

  useEffect(() => { loadData(); }, []);

  const loadData = async () => {
    try { setDispos(await getDisponibilites()); }
    catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const openCreate = () => {
    setEditing(null);
    setForm({ jourSemaine: 1, heureDebut: '08:00', heureFin: '10:00' });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (d) => {
    setEditing(d);
    setForm({ jourSemaine: d.jourSemaine, heureDebut: d.heureDebut?.slice(0, 5) || '08:00', heureFin: d.heureFin?.slice(0, 5) || '10:00' });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = { ...form, jourSemaine: Number(form.jourSemaine) };
      if (editing) {
        const updated = await updateDisponibilite(editing.id, payload);
        setDispos(dispos.map((d) => d.id === updated.id ? updated : d));
      } else {
        const created = await createDisponibilite(payload);
        setDispos([...dispos, created]);
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
      await deleteDisponibilite(deleteTarget.id);
      setDispos(dispos.filter((d) => d.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) { console.error(err); }
    finally { setSaving(false); }
  };

  const grouped = JOURS.slice(1).map((jour, i) => ({
    jour,
    num: i + 1,
    slots: dispos.filter((d) => d.jourSemaine === i + 1).sort((a, b) => (a.heureDebut || '').localeCompare(b.heureDebut || '')),
  }));

  if (loading) return <div className="page"><LoadingSpinner size="lg" text="Chargement..." /></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Disponibilités</h1>
            <p>Définissez vos créneaux horaires disponibles pour étudier</p>
          </div>
          <button className="btn btn-primary" onClick={openCreate}><Plus /> Ajouter un créneau</button>
        </div>
      </div>

      {dispos.length === 0 ? (
        <EmptyState icon={<Clock />} title="Aucune disponibilité" description="Ajoutez vos créneaux horaires pour planifier vos sessions." action={<button className="btn btn-primary" onClick={openCreate}><Plus /> Ajouter</button>} />
      ) : (
        <div className="dispo__grid">
          {grouped.map(({ jour, num, slots }) => (
            <div key={num} className={`dispo__day-card card ${slots.length === 0 ? 'dispo__day-card--empty' : ''}`}>
              <div className="card-body">
                <h3 className="dispo__day-title">{jour}</h3>
                {slots.length === 0 ? (
                  <p className="text-sm text-secondary" style={{ marginTop: 8 }}>Aucun créneau</p>
                ) : (
                  <div className="dispo__slots">
                    {slots.map((s) => (
                      <div key={s.id} className="dispo__slot">
                        <span className="dispo__slot-time">{s.heureDebut?.slice(0, 5)} - {s.heureFin?.slice(0, 5)}</span>
                        <div className="dispo__slot-actions">
                          <button className="btn btn-ghost btn-icon btn-sm" onClick={() => openEdit(s)}><Edit3 /></button>
                          <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(s)}><Trash2 /></button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Modifier le créneau' : 'Nouveau créneau'} size="sm">
        {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Jour de la semaine</label>
            <select className="form-select" value={form.jourSemaine} onChange={(e) => setForm({ ...form, jourSemaine: e.target.value })} required>
              {JOURS.slice(1).map((j, i) => <option key={i + 1} value={i + 1}>{j}</option>)}
            </select>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
            <div className="form-group">
              <label className="form-label">Heure de début</label>
              <input type="time" className="form-input" value={form.heureDebut} onChange={(e) => setForm({ ...form, heureDebut: e.target.value })} required />
            </div>
            <div className="form-group">
              <label className="form-label">Heure de fin</label>
              <input type="time" className="form-input" value={form.heureFin} onChange={(e) => setForm({ ...form, heureFin: e.target.value })} required />
            </div>
          </div>
          <div className="modal__footer" style={{ padding: '16px 0 0', border: 'none' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Annuler</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? '...' : 'Enregistrer'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Supprimer le créneau" message="Supprimer ce créneau de disponibilité ?" loading={saving} />
    </div>
  );
}
