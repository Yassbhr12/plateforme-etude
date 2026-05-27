import { useState, useEffect } from 'react';
import { getMatieres, createMatiere, updateMatiere, deleteMatiere } from '../api/matiereService';
import Modal from '../components/ui/Modal';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import EmptyState from '../components/ui/EmptyState';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { BookOpen, Plus, Edit3, Trash2, Star, AlertCircle } from 'lucide-react';
import './Matieres.css';

export default function Matieres() {
  const [matieres, setMatieres] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingMatiere, setEditingMatiere] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [form, setForm] = useState({ nom: '', priorite: 3 });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => { loadMatieres(); }, []);

  const loadMatieres = async () => {
    try {
      const data = await getMatieres();
      setMatieres(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const openCreate = () => {
    setEditingMatiere(null);
    setForm({ nom: '', priorite: 3 });
    setError('');
    setModalOpen(true);
  };

  const openEdit = (m) => {
    setEditingMatiere(m);
    setForm({ nom: m.nom, priorite: m.priorite });
    setError('');
    setModalOpen(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      if (editingMatiere) {
        const updated = await updateMatiere(editingMatiere.id, form);
        setMatieres(matieres.map((m) => (m.id === updated.id ? updated : m)));
      } else {
        const created = await createMatiere(form);
        setMatieres([...matieres, created]);
      }
      setModalOpen(false);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.error || 'Erreur lors de la sauvegarde');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    try {
      await deleteMatiere(deleteTarget.id);
      setMatieres(matieres.filter((m) => m.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) {
      console.error(err);
    } finally {
      setSaving(false);
    }
  };

  const renderPriority = (p) => {
    return (
      <div className="matieres__priority">
        {[1, 2, 3, 4, 5].map((i) => (
          <Star key={i} className={`matieres__star ${i <= p ? 'filled' : ''}`} />
        ))}
      </div>
    );
  };

  if (loading) {
    return <div className="page"><LoadingSpinner size="lg" text="Chargement des matières..." /></div>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Matières</h1>
            <p>Gérez vos matières d'étude et leur priorité</p>
          </div>
          <button className="btn btn-primary" onClick={openCreate}>
            <Plus /> Ajouter une matière
          </button>
        </div>
      </div>

      {matieres.length === 0 ? (
        <EmptyState
          icon={<BookOpen />}
          title="Aucune matière"
          description="Commencez par ajouter vos matières d'étude pour organiser vos sessions."
          action={<button className="btn btn-primary" onClick={openCreate}><Plus /> Ajouter une matière</button>}
        />
      ) : (
        <div className="grid-cards">
          {matieres.map((m) => (
            <div key={m.id} className="card matieres__card">
              <div className="card-body">
                <div className="matieres__card-header">
                  <div className="matieres__card-icon"><BookOpen /></div>
                  <div className="matieres__card-actions">
                    <button className="btn btn-ghost btn-icon btn-sm" onClick={() => openEdit(m)} title="Modifier">
                      <Edit3 />
                    </button>
                    <button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(m)} title="Supprimer">
                      <Trash2 />
                    </button>
                  </div>
                </div>
                <h3 className="matieres__card-title">{m.nom}</h3>
                <div className="matieres__card-priority">
                  <span className="text-sm text-secondary">Priorité</span>
                  {renderPriority(m.priorite)}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editingMatiere ? 'Modifier la matière' : 'Nouvelle matière'} size="sm">
        {error && <div className="alert alert-error"><AlertCircle /><span>{error}</span></div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Nom de la matière</label>
            <input className="form-input" value={form.nom} onChange={(e) => setForm({ ...form, nom: e.target.value })} required maxLength={120} placeholder="ex: Mathématiques" />
          </div>
          <div className="form-group">
            <label className="form-label">Priorité (1-5)</label>
            <div className="matieres__priority-input">
              {[1, 2, 3, 4, 5].map((i) => (
                <button key={i} type="button" className={`matieres__priority-btn ${i <= form.priorite ? 'active' : ''}`} onClick={() => setForm({ ...form, priorite: i })}>
                  <Star />
                </button>
              ))}
            </div>
          </div>
          <div className="modal__footer" style={{ padding: '16px 0 0', border: 'none' }}>
            <button type="button" className="btn btn-secondary" onClick={() => setModalOpen(false)}>Annuler</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Enregistrement...' : 'Enregistrer'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Supprimer la matière"
        message={`Êtes-vous sûr de vouloir supprimer "${deleteTarget?.nom}" ? Cette action est irréversible.`}
        loading={saving}
      />
    </div>
  );
}
