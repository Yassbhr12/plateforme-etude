import { useState, useEffect } from 'react';
import { getUsers, deleteUser, getAllMatieres, deleteAdminMatiere, getAllSessions, deleteAdminSession, getAllGroupes, deleteAdminGroupe, getAllNotifications, deleteAdminNotification } from '../api/adminService';
import ConfirmDialog from '../components/ui/ConfirmDialog';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import EmptyState from '../components/ui/EmptyState';
import StatusBadge from '../components/ui/StatusBadge';
import { ShieldCheck, Users, BookOpen, CalendarClock, UsersRound, Bell, Trash2, Database } from 'lucide-react';
import './Admin.css';

const TABS = [
  { key: 'users', label: 'Utilisateurs', icon: Users },
  { key: 'matieres', label: 'Matières', icon: BookOpen },
  { key: 'sessions', label: 'Sessions', icon: CalendarClock },
  { key: 'groupes', label: 'Groupes', icon: UsersRound },
  { key: 'notifications', label: 'Notifications', icon: Bell },
];

export default function Admin() {
  const [tab, setTab] = useState('users');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => { loadTab(); }, [tab]);

  const loadTab = async () => {
    setLoading(true);
    try {
      switch (tab) {
        case 'users': setData(await getUsers()); break;
        case 'matieres': setData(await getAllMatieres()); break;
        case 'sessions': setData(await getAllSessions()); break;
        case 'groupes': setData(await getAllGroupes()); break;
        case 'notifications': setData(await getAllNotifications()); break;
      }
    } catch (err) { console.error(err); setData([]); }
    finally { setLoading(false); }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setSaving(true);
    try {
      switch (tab) {
        case 'users': await deleteUser(deleteTarget.id); break;
        case 'matieres': await deleteAdminMatiere(deleteTarget.id); break;
        case 'sessions': await deleteAdminSession(deleteTarget.id); break;
        case 'groupes': await deleteAdminGroupe(deleteTarget.id); break;
        case 'notifications': await deleteAdminNotification(deleteTarget.id); break;
      }
      setData(data.filter((d) => d.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) { console.error(err); }
    finally { setSaving(false); }
  };

  const renderTable = () => {
    if (loading) return <LoadingSpinner size="md" text="Chargement..." />;
    if (data.length === 0) return <EmptyState icon={<Database />} title="Aucune donnée" />;

    switch (tab) {
      case 'users':
        return (
          <table className="table">
            <thead><tr><th>ID</th><th>Nom</th><th>Prénom</th><th>Email</th><th>Rôle</th><th>Actif</th><th></th></tr></thead>
            <tbody>
              {data.map((u) => (
                <tr key={u.id}>
                  <td className="text-sm text-secondary">{u.id}</td>
                  <td className="font-medium">{u.nom}</td>
                  <td>{u.prenom}</td>
                  <td className="text-sm">{u.email}</td>
                  <td><span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-accent' : 'badge-primary'}`}>{u.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</span></td>
                  <td><span className={`badge ${u.actif ? 'badge-success' : 'badge-error'}`}>{u.actif ? 'Actif' : 'Inactif'}</span></td>
                  <td><button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(u)}><Trash2 /></button></td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      case 'matieres':
        return (
          <table className="table">
            <thead><tr><th>ID</th><th>Nom</th><th>Priorité</th><th>Utilisateur</th><th></th></tr></thead>
            <tbody>
              {data.map((m) => (
                <tr key={m.id}>
                  <td className="text-sm text-secondary">{m.id}</td>
                  <td className="font-medium">{m.nom}</td>
                  <td>{m.priorite}/5</td>
                  <td className="text-sm">{m.userNom} ({m.userEmail})</td>
                  <td><button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(m)}><Trash2 /></button></td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      case 'sessions':
        return (
          <table className="table">
            <thead><tr><th>ID</th><th>Titre</th><th>Matière</th><th>Statut</th><th>Utilisateur</th><th></th></tr></thead>
            <tbody>
              {data.map((s) => (
                <tr key={s.id}>
                  <td className="text-sm text-secondary">{s.id}</td>
                  <td className="font-medium">{s.titre}</td>
                  <td><span className="badge badge-primary">{s.matiereNom}</span></td>
                  <td><StatusBadge status={s.statut} /></td>
                  <td className="text-sm">{s.userNom}</td>
                  <td><button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(s)}><Trash2 /></button></td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      case 'groupes':
        return (
          <table className="table">
            <thead><tr><th>ID</th><th>Nom</th><th>Admin</th><th>Membres</th><th></th></tr></thead>
            <tbody>
              {data.map((g) => (
                <tr key={g.id}>
                  <td className="text-sm text-secondary">{g.id}</td>
                  <td className="font-medium">{g.nom}</td>
                  <td className="text-sm">{g.adminNom}</td>
                  <td>{g.users?.length || 0}</td>
                  <td><button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(g)}><Trash2 /></button></td>
                </tr>
              ))}
            </tbody>
          </table>
        );
      case 'notifications':
        return (
          <table className="table">
            <thead><tr><th>ID</th><th>Message</th><th>Type</th><th>Lue</th><th>Utilisateur</th><th></th></tr></thead>
            <tbody>
              {data.map((n) => (
                <tr key={n.id}>
                  <td className="text-sm text-secondary">{n.id}</td>
                  <td className="text-sm" style={{ maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{n.message}</td>
                  <td><StatusBadge status={n.type} /></td>
                  <td><span className={`badge ${n.lue ? 'badge-success' : 'badge-warning'}`}>{n.lue ? 'Lue' : 'Non lue'}</span></td>
                  <td className="text-sm">{n.userNom}</td>
                  <td><button className="btn btn-ghost btn-icon btn-sm" onClick={() => setDeleteTarget(n)}><Trash2 /></button></td>
                </tr>
              ))}
            </tbody>
          </table>
        );
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div className="page-header-row">
          <div>
            <h1>Administration</h1>
            <p>Gérez toutes les données de la plateforme</p>
          </div>
          <span className="badge badge-accent"><ShieldCheck style={{ width: 14, height: 14 }} /> Admin</span>
        </div>
      </div>

      <div className="tabs">
        {TABS.map(({ key, label, icon: Icon }) => (
          <button key={key} className={`tab ${tab === key ? 'active' : ''}`} onClick={() => setTab(key)}>
            <Icon style={{ width: 16, height: 16, marginRight: 6 }} />
            {label} {!loading && tab === key && `(${data.length})`}
          </button>
        ))}
      </div>

      <div className="table-container">
        {renderTable()}
      </div>

      <ConfirmDialog isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} onConfirm={handleDelete} title="Supprimer" message="Cette action est irréversible. Continuer ?" loading={saving} />
    </div>
  );
}
