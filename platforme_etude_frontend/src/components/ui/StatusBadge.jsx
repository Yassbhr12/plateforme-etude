import './StatusBadge.css';

const STATUT_CONFIG = {
  PLANIFIEE: { label: 'Planifiée', variant: 'primary' },
  TERMINEE: { label: 'Terminée', variant: 'success' },
  ANNULEE: { label: 'Annulée', variant: 'neutral' },
  EN_ATTENTE: { label: 'En attente', variant: 'warning' },
  ACCEPTEE: { label: 'Acceptée', variant: 'success' },
  REFUSEE: { label: 'Refusée', variant: 'error' },
  RAPPEL_SESSION: { label: 'Rappel', variant: 'primary' },
  INVITATION_GROUPE: { label: 'Invitation', variant: 'accent' },
  OBJECTIF_ATTEINT: { label: 'Objectif atteint', variant: 'success' },
};

export default function StatusBadge({ status }) {
  const config = STATUT_CONFIG[status] || { label: status, variant: 'neutral' };
  return (
    <span className={`badge badge-${config.variant}`}>
      {config.label}
    </span>
  );
}
