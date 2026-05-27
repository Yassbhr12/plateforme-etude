import { AlertTriangle } from 'lucide-react';
import Modal from './Modal';
import './ConfirmDialog.css';

export default function ConfirmDialog({ isOpen, onClose, onConfirm, title, message, confirmText = 'Supprimer', danger = true, loading = false }) {
  return (
    <Modal isOpen={isOpen} onClose={onClose} title={title || 'Confirmation'} size="sm">
      <div className="confirm-dialog">
        <div className={`confirm-dialog__icon ${danger ? 'danger' : ''}`}>
          <AlertTriangle />
        </div>
        <p className="confirm-dialog__message">{message || 'Êtes-vous sûr de vouloir continuer ?'}</p>
        <div className="confirm-dialog__actions">
          <button className="btn btn-secondary" onClick={onClose} disabled={loading}>
            Annuler
          </button>
          <button
            className={`btn ${danger ? 'btn-danger' : 'btn-primary'}`}
            onClick={onConfirm}
            disabled={loading}
          >
            {loading ? 'En cours...' : confirmText}
          </button>
        </div>
      </div>
    </Modal>
  );
}
