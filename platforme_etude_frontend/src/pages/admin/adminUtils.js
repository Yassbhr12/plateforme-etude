export const formatDateTime = (value) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return date.toLocaleString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const formatDate = (value) => {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
};

export const dayName = (day) => {
  const days = {
    1: 'Lundi',
    2: 'Mardi',
    3: 'Mercredi',
    4: 'Jeudi',
    5: 'Vendredi',
    6: 'Samedi',
    7: 'Dimanche',
  };
  return days[day] || '-';
};

export const readLabel = (value) => (value ? 'Lue' : 'Non lue');

export const roleLabel = (role) => (role === 'ROLE_ADMIN' ? 'Admin' : 'Etudiant');

export const statusClass = (status) => {
  const normalized = String(status || '').toUpperCase();
  if (['TERMINEE', 'ACCEPTEE', 'ACTIF', 'LUE', 'ROLE_ADMIN'].includes(normalized)) return 'badge-success';
  if (['PLANIFIEE', 'EN_ATTENTE', 'RAPPEL_SESSION', 'INVITATION_GROUPE'].includes(normalized)) return 'badge-primary';
  if (['ANNULEE', 'REFUSEE', 'INACTIF'].includes(normalized)) return 'badge-warning';
  if (['OBJECTIF_ATTEINT'].includes(normalized)) return 'badge-accent';
  return 'badge-neutral';
};

export const textValue = (value) => {
  if (value === null || value === undefined || value === '') return '-';
  if (typeof value === 'boolean') return value ? 'Oui' : 'Non';
  return String(value);
};

export const matchesSearch = (item, query) => {
  const q = query.trim().toLowerCase();
  if (!q) return true;
  return Object.values(item || {}).some((value) =>
    value !== null && value !== undefined && String(value).toLowerCase().includes(q)
  );
};

export const sum = (items, getValue) =>
  items.reduce((total, item) => total + Number(getValue(item) || 0), 0);
